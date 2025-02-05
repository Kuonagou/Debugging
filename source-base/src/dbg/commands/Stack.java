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
        System.out.println("Appel de la stack:");
        for (int i = 0; i < frames.size(); i++) {
            StackFrame frame = frames.get(i);
            System.out.printf("%d: %s.%s (line %d)\n",
                    i,
                    frame.location().declaringType().name(),
                    frame.location().method().name(),
                    frame.location().lineNumber()
            );
        }
    }
}
