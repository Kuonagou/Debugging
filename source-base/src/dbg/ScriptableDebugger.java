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
import dbg.commands.Step;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ScriptableDebugger {

    private Class debugClass;
    private VirtualMachine vm;
    private CommandManager commandManager = new CommandManager();
    private List<StackFrame> executionHistory = new ArrayList<>();
    private int PC = 0;

    public VirtualMachine connectAndLaunchVM() throws IOException, IllegalConnectorArgumentsException, VMStartException {
        LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();
        Map<String, Connector.Argument> arguments = launchingConnector.defaultArguments();
        arguments.get("main").setValue(debugClass.getName());
        VirtualMachine vm = launchingConnector.launch(arguments);
        return vm;
    }
    public void attachTo(Class debuggeeClass) {

        this.debugClass = debuggeeClass;
        try {
            vm = connectAndLaunchVM();
            enableClassPrepareRequest(vm);
            startDebugger();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalConnectorArgumentsException e) {
            e.printStackTrace();
        } catch (VMStartException e) {
            e.printStackTrace();
            System.out.println(e);
        } catch (VMDisconnectedException e) {
            System.out.println("Virtual Machine is disconnected: " + e);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enableClassPrepareRequest(VirtualMachine vm) {
        ClassPrepareRequest classPrepareRequest = vm.eventRequestManager().createClassPrepareRequest();
        classPrepareRequest.addClassFilter(debugClass.getName()) ;
        classPrepareRequest.enable();
    }

    public void startDebugger() throws Exception {
        EventSet eventSet = null;
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
                    setBreakPoint(debugClass.getName() , (25) ) ;
                }
                if(event instanceof BreakpointEvent | event instanceof StepEvent) {
                    readInput((LocatableEvent) event);
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
               reconnue =  commandManager.executeCommand(vm, event, command);
            }

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

}
