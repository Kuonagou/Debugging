package dbg;

import com.sun.jdi.*;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.*;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import dbg.commands.CommandManager;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import javax.swing.*;
import java.awt.*;

public class ScriptableDebugger {
    private Class debugClass;
    private VirtualMachine vm;
    private JFrame frame;
    private JTextArea outputArea;
    private JButton stepButton, stepOverButton, continueButton, frameButton, temporariesButton, stackButton, receiverButton;
    private CommandManager commandManager = new CommandManager();

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
            createGUI(); // Création de l'IHM
            startDebugger();
        } catch (IOException | IllegalConnectorArgumentsException | VMStartException e) {
            e.printStackTrace();
        } catch (VMDisconnectedException e) {
            appendOutput("Virtual Machine is disconnected: " + e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enableClassPrepareRequest(VirtualMachine vm) {
        ClassPrepareRequest classPrepareRequest = vm.eventRequestManager().createClassPrepareRequest();
        classPrepareRequest.addClassFilter(debugClass.getName());
        classPrepareRequest.enable();
    }

    public void startDebugger() throws Exception {
        EventSet eventSet = null;
        while ((eventSet = vm.eventQueue().remove()) != null) {
            for (Event event : eventSet) {
                appendOutput(event.toString());
                if (event instanceof VMDisconnectEvent) {
                    handleVMDisconnectEvent();
                }
                if (event instanceof ClassPrepareEvent) {
                    setBreakPoint(debugClass.getName(), 6);
                    setBreakPoint(debugClass.getName(), 25);
                }
                if (event instanceof BreakpointEvent || event instanceof StepEvent) {
                    handleLocatableEvent((LocatableEvent) event);
                }
                vm.resume();
            }
        }
    }

    private void setBreakPoint(String className, int lineNumber) throws AbsentInformationException {
        for (ReferenceType targetClass : vm.allClasses()) {
            if (targetClass.name().equals(className)) {
                Location location = targetClass.locationsOfLine(lineNumber).get(0);
                BreakpointRequest bpReq = vm.eventRequestManager().createBreakpointRequest(location);
                bpReq.enable();
            }
        }
    }

    private void handleVMDisconnectEvent() {
        try {
            InputStreamReader reader = new InputStreamReader(vm.process().getInputStream());
            OutputStreamWriter writer = new OutputStreamWriter(System.out);
            reader.transferTo(writer);
            writer.flush();
        } catch (IOException e) {
            appendOutput("Target VM input stream reading error.");
        }
        appendOutput("End of program");
    }

    private void handleLocatableEvent(LocatableEvent event) {
        try {
            String command = JOptionPane.showInputDialog(frame, "Enter a command:");
            if (command != null && !command.isEmpty()) {
                boolean reconnue = commandManager.executeCommand(vm, event, command);
                if (!reconnue) {
                    appendOutput("Command not recognized.");
                }
            }
        } catch (Exception e) {
            appendOutput("Error executing command: " + e.getMessage());
        }
    }

    private void createGUI() {
        frame = new JFrame("Debugger Interface");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(4, 2));

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        frame.setLayout(new BorderLayout());
        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private JButton createButton(String label, ActionListener listener) {
        JButton button = new JButton(label);
        button.addActionListener(listener);
        return button;
    }

    private void executeCommand(String command) {
        try {
            commandManager.executeCommand(vm, null, command);
        } catch (Exception e) {
            appendOutput("Error executing command: " + e.getMessage());
        }
    }

    private void appendOutput(String message) {
        outputArea.append(message + "\n");
    }
}


