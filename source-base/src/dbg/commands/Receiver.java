package dbg.commands;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;

public class Receiver implements Icommande{
    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) throws IncompatibleThreadStateException {
            StackFrame frame = event.thread().frame(0);
            ObjectReference receiver = frame.thisObject();
            System.out.println(receiver);
    }
}
