package dbg.commands;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;

public class Frame implements Icommande{
    /**
     *  frame : renvoie et imprime la frame courante
     */
    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) throws IncompatibleThreadStateException {
            StackFrame frame = event.thread().frame(0);
        System.out.println("Current frame: " + frame.location().method() +
                " at line " + frame.location().lineNumber());
    }
}
