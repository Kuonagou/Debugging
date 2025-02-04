package dbg.commands;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;

import java.util.List;

public class Stack implements Icommande{
    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) throws IncompatibleThreadStateException {
            List<StackFrame> frames = event.thread().frames();
            for (StackFrame frame : frames) {
                System.out.println(frame);
            }
    }
}
