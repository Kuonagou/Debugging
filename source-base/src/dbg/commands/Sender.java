package dbg.commands;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;

import java.util.List;

public class Sender implements Icommande{
    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) throws IncompatibleThreadStateException {
        List<StackFrame> frames = event.thread().frames();
        if (frames.size() > 1) {
            StackFrame callerFrame = frames.get(1);
            ObjectReference sender = callerFrame.thisObject();
            System.out.println("Sender: " + sender);
        } else {
            System.out.println("Sender n'existe pas");
        }
    }
}
