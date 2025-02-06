package dbg;

import com.sun.jdi.*;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.StepRequest;
import dbg.commands.CommandManager;
import dbg.commands.Icommande;
import dbg.commands.StepBack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.*;

public class ScriptableDebugger {

    private Icommande commande;
    private Class debugClass;
    private VirtualMachine vm;
    private CommandManager commandManager = new CommandManager();
    private List<StackFrame> executionHistory = new ArrayList<>();

    // Track all executed lines
    private List<Integer> executedLines = new ArrayList<>();
    private Location currentLocation;
    private Integer tour = 0;

    public VirtualMachine connectAndLaunchVM() throws IOException, IllegalConnectorArgumentsException, VMStartException {
        LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();
        Map<String, Connector.Argument> arguments = launchingConnector.defaultArguments();
        arguments.get("main").setValue(debugClass.getName());
        VirtualMachine vm = launchingConnector.launch(arguments);
        return vm;
    }
    public void attachTo(Class debuggeeClass) {
        this.debugClass = debuggeeClass;
        int maxRetries = 1;

        for (int retry = 0; retry < maxRetries; retry++) {
            try {
                vm = connectAndLaunchVM();
                enableClassPrepareRequest(vm);
                startDebugger();
                break; // Success, exit retry loop
            } catch (VMDisconnectedException e) {
                System.out.println("VM Disconnected. Attempt " + (retry + 1) + " of " + maxRetries);

                if (retry == maxRetries - 1) {
                    System.out.println("Failed to reconnect after " + maxRetries + " attempts");
                    e.printStackTrace();
                    break;
                }

                // Optional: Add a small delay before retrying
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void enableClassPrepareRequest(VirtualMachine vm) {
        ClassPrepareRequest classPrepareRequest = vm.eventRequestManager().createClassPrepareRequest();
        classPrepareRequest.addClassFilter(debugClass.getName()) ;
        classPrepareRequest.enable();
    }

    private void startDebugger() throws Exception {
        EventSet eventSet;
        while ((eventSet = vm.eventQueue().remove()) != null) {
            for (Event event : eventSet) {
                System.out.println(event.toString());
                if (event instanceof VMDisconnectEvent ) {
                    System.out.println("End of program");
                    InputStreamReader reader = new InputStreamReader(vm.process().getInputStream());
                    OutputStreamWriter writer = new OutputStreamWriter(System.out);
                    try {
                        reader.transferTo(writer);
                        writer.flush();
                    } catch (IOException e) {
                        System.out.println("Target VM input stream reading error.");
                    }
                }
                if(event instanceof ClassPrepareEvent) { // break point à l'instanciation
                    setBreakPoint(debugClass.getName() , 6 ) ;
                    setBreakPoint(debugClass.getName() , (17) ) ;
                }

                if ( event instanceof BreakpointEvent) {

                    commandManager.executeCommand(vm, (BreakpointEvent) event, "step over");
                    //readInput((LocatableEvent) event);
                }
                if (event instanceof StepEvent) {
                    LocatableEvent stepEvent = (LocatableEvent) event;
                    Location location = stepEvent.location();

                    // Track the current line
                    if (location.method().declaringType().name().equals(debugClass.getName())) {
                        int lineNumber = location.lineNumber();
                        if (lineNumber > 0) {
                            executedLines.add(lineNumber);
                            currentLocation = location;
                            System.out.println("Executed line: " + lineNumber);
                        }
                    }
                    commandManager.executeCommand(vm, (StepEvent) event, "step over");
                    //readInput((LocatableEvent) event);
                }

                vm.resume();
            }
        }
    }

    private void setBreakPoint(String className, int lineNumber) throws AbsentInformationException {
        for( ReferenceType targetClass : vm.allClasses()) {
            if(targetClass.name().equals(className)){
                Location location = targetClass.locationsOfLine(lineNumber).get(0) ;
                BreakpointRequest bpReq = vm. eventRequestManager().createBreakpointRequest(location);
                bpReq.enable();
            }
        }
    }

    private void readInput(LocatableEvent event) throws Exception {
        try {
            boolean reconnue = false;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while(!reconnue){
                System.out.print("Entrez une commande > ");
                String command = reader.readLine();
                if(!command.equals("step back")){
                    reconnue =  commandManager.executeCommand(vm, event, command);
                }else {
                    commande = new StepBack();
                    commande.execute(vm,event);
                }

            }

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

}
