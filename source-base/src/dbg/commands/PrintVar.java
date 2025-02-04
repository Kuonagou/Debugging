package dbg.commands;

import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;

public class PrintVar implements Icommande{
    private String varName;

    public PrintVar(String varName) {
        this.varName = varName;
    }

    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) {
        try {
            StackFrame frame = event.thread().frame(0);
            LocalVariable var = frame.visibleVariableByName(varName);
            Value value = frame.getValue(var);
            System.out.println(varName + " → " + value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
