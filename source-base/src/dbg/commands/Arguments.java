package dbg.commands;

import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;

import java.util.List;

public class Arguments implements Icommande{
    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) {
        try {
            StackFrame frame = event.thread().frame(0);
            List<LocalVariable> arguments = frame.location().method().arguments();
            for (LocalVariable arg : arguments) {
                Value value = frame.getValue(arg);
                System.out.println(arg.name() + " → " + value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
