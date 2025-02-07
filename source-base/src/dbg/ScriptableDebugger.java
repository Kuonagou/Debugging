package dbg;

import com.sun.jdi.*;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import dbg.commands.CommandManager;
import dbg.commands.Icommande;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScriptableDebugger {

    private Icommande commande;
    private Class debugClass;
    private VirtualMachine vm;
    private CommandManager commandManager = new CommandManager();
    private boolean askForStepBack = false;
    private int stepBack ;
    private List<Integer> saveBreakPoints;


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
                if(event instanceof ClassPrepareEvent) {
                    if(askForStepBack){
                        System.out.println("J'ajoute mon breakpoint ligne "+stepBack);
                        setBreakPoint(debugClass.getName(),stepBack,true);
                        for (Integer line : saveBreakPoints){
                            System.out.println(line);
                            setBreakPoint(debugClass.getName(),line,line > stepBack);
                            System.out.println(line);

                        }
                    }else{
                        setBreakPoint(debugClass.getName() , 6,true ) ;
                        setBreakPoint(debugClass.getName() , 25 ,true) ;
                    }

                }
                if(event instanceof BreakpointEvent | event instanceof StepEvent) {
                    readInput((LocatableEvent) event);
                }
                vm.resume();
            }
        }
    }

    private void setBreakPoint(String className, int lineNumber,boolean actif) throws AbsentInformationException {
        for( ReferenceType targetClass : vm.allClasses()) {
            if(targetClass.name().equals(className)){
                Location location = targetClass.locationsOfLine(lineNumber).get(0) ;
                BreakpointRequest bpReq = vm. eventRequestManager().createBreakpointRequest(location);
                if(actif){
                    bpReq.enable();
                }else{
                    bpReq.disable();
                }

            }
        }
    }

    private void readInput(LocatableEvent event) throws Exception {
        try {
            int ligneActuelle = event.location().lineNumber();
            System.out.println("Ligne de l'event "+ligneActuelle);
            boolean reconnue = false;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while(!reconnue){
                System.out.print("Entrez une commande > ");
                String command = reader.readLine();

                if(command.equals("step back")){
                    stepBack = ligneActuelle - 1;
                    System.out.println("step back"+stepBack);
                    stepBack();
                }else if (command.equals("step back for")){
                    System.out.print("Nombre de step back > ");
                    command = reader.readLine();
                    stepBack = ligneActuelle - Integer.parseInt(command);
                    System.out.println("step back"+stepBack);
                    stepBack();
                }else{
                    reconnue =  commandManager.executeCommand(vm, event, command);
                }

            }

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private void stepBack() throws AbsentInformationException {
        saveBreakpoints();
        askForStepBack = true;
        //ajouter de gerer qu'on ne sort pas des index prévus
        attachTo(JDISimpleDebuggee.class);
    }

    private void saveBreakpoints() throws AbsentInformationException {
        List<BreakpointRequest> breakpoints = vm.eventRequestManager().breakpointRequests();
        saveBreakPoints = new ArrayList<>();
        if (breakpoints.isEmpty()) {
            System.out.println("Pas de breakpoints.");
        } else {
            for (BreakpointRequest bp : breakpoints) {
                saveBreakPoints.add(bp.location().lineNumber());
            }
        }
    }

}