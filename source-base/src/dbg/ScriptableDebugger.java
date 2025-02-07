package dbg;

import com.sun.jdi.*;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import dbg.commands.CommandManager;
import dbg.commands.Icommande;
import dbg.commands.StepBack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.*;



public class ScriptableDebugger {

    private class ExecutionState {
        private Location location;
        private int lineNumber;
        private Map<String, Value> variables;
        private long timestamp;
        private String threadName;

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public Map<String, Value> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, Value> variables) {
            this.variables = variables;
        }

        public String getThreadName() {
            return threadName;
        }

        public void setThreadName(String threadName) {
            this.threadName = threadName;
        }

        @Override
        public String toString() {
            return "ExecutionState{" +
                    "location=" + location.toString() +
                    ", lineNumber=" + lineNumber +
                    ", timestamp=" + timestamp +
                    ", threadName='" + threadName + '\'' +
                    '}';
        }
    }

    private Icommande commande;
    private Class debugClass;
    private VirtualMachine vm;
    private CommandManager commandManager = new CommandManager();
    private List<StackFrame> executionHistory = new ArrayList<>();
    private List<ExecutionState> recordedStates = new ArrayList<>();
    private boolean isFirstExecution = true;

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
        int numExecutions = 2; // Nombre d'exécutions souhaitées

        for (int execution = 0; execution < numExecutions; execution++) {
            System.out.println("Starting execution #" + (execution + 1));

            try {
                if (execution == 0) {
                    vm = connectAndLaunchVM();
                    //passage une première fois pour enregistrer
                    enableClassPrepareRequest(vm);
                    recordExecutionStates();
                } else { //passage avec les mêmes info pour debugger
                    vm = connectAndLaunchVM();
                    enableClassPrepareRequest(vm);
                    replayWithRecordedStates();
                }

                // Démarrer le débogueur
                startDebugger();

                // Attendre que l'exécution soit terminée
                waitForVMExit();

            } catch (VMDisconnectedException e) {
                System.out.println("VM Disconnected during execution #" + (execution + 1));
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Error during execution #" + (execution + 1));
                e.printStackTrace();
            } finally {
                if (vm != null) {
                    try {
                        vm.dispose();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Pause entre les exécutions
            if (execution < numExecutions - 1) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void recordExecutionStates() {
        // Configuration des event requests pour l'enregistrement
        EventRequestManager erm = vm.eventRequestManager();

        MethodEntryRequest methodEntryRequest = erm.createMethodEntryRequest();
        methodEntryRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        methodEntryRequest.enable();

        // Autres configurations nécessaires pour l'enregistrement
    }

    private void replayWithRecordedStates() {
        // Configuration des event requests pour la relecture
        EventRequestManager erm = vm.eventRequestManager();

        MethodEntryRequest methodEntryRequest = erm.createMethodEntryRequest();
        methodEntryRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        methodEntryRequest.enable();

        // Utilisation des états enregistrés lors de la première exécution
    }

    private void waitForVMExit() {
        try {
            EventQueue eventQueue = vm.eventQueue();
            while (true) {
                EventSet eventSet = eventQueue.remove();
                for (Event event : eventSet) {
                    if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
                        return;
                    }
                }
                eventSet.resume();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enableMethodEntryRequest(VirtualMachine vm) {
        EventRequestManager erm = vm.eventRequestManager();
        MethodEntryRequest methodEntryRequest = erm.createMethodEntryRequest();
        methodEntryRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        methodEntryRequest.enable();
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
                if (event instanceof VMDisconnectEvent) {
                    handleVMDisconnect();
                    return;
                }

                if (event instanceof ClassPrepareEvent) {
                    setBreakPoint(debugClass.getName(), 6);
                    setBreakPoint(debugClass.getName(), 25);
                }

                if (event instanceof BreakpointEvent || event instanceof StepEvent) {
                    if (isFirstExecution) {
                        handleFirstExecution((LocatableEvent) event);
                    } else {
                        handleSecondExecution((LocatableEvent) event);
                    }
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

    private void handleFirstExecution(LocatableEvent event) throws Exception {
        // Enregistrer l'état actuel
        ExecutionState state = captureExecutionState(event);
        recordedStates.add(state);

        // Continuer l'exécution automatiquement
        commandManager.executeCommand(vm, event, "step over");

        // Tracer l'exécution
        trackExecution(event);
    }

    private void handleSecondExecution(LocatableEvent event) throws Exception {
        // Mode interactif avec le terminal
        System.out.println("\nCurrent location: " + event.location().lineNumber());
        System.out.println("Previous recorded state: " + recordedStates.get(event.location().lineNumber()));
        readInput(event);
    }
    private ExecutionState captureExecutionState(LocatableEvent event) {
        ExecutionState state = new ExecutionState();
        ThreadReference thread = event.thread();
        try {
            state.setLocation(event.location());
            state.setLineNumber(event.location().lineNumber());
            state.setVariables(captureVariables(thread));
            state.setTimestamp(System.currentTimeMillis());
            state.setThreadName(thread.name());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("State : " +state);
        return state;
    }
    private Map<String, Value> captureVariables(ThreadReference thread) {
        Map<String, Value> variables = new HashMap<>();
        try {
            if (thread.frameCount() > 0) {
                StackFrame frame = thread.frame(0);
                List<LocalVariable> localVars = frame.visibleVariables();
                for (LocalVariable var : localVars) {
                    variables.put(var.name(), frame.getValue(var));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return variables;
    }

    private void trackExecution(LocatableEvent event) throws Exception {
        Location location = event.location();
        if (location.method().declaringType().name().equals(debugClass.getName())) {
            int lineNumber = location.lineNumber();
            if (lineNumber > 0) {
                executedLines.add(lineNumber);
                currentLocation = location;
                commandManager.executeCommand(vm,event,"frame");
                System.out.println("Executed line: " + lineNumber + " location : "+location);
            }
        }
    }

    private void handleVMDisconnect() {
        System.out.println("End of program");
        isFirstExecution = false;
        try {
            InputStreamReader reader = new InputStreamReader(vm.process().getInputStream());
            OutputStreamWriter writer = new OutputStreamWriter(System.out);
            reader.transferTo(writer);
            writer.flush();
        } catch (IOException e) {
            System.out.println("Target VM input stream reading error.");
        }
    }


}
