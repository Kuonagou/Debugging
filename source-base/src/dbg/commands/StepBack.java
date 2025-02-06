package dbg.commands;


import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;

public class StepBack implements Icommande {

    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) throws Exception, IncompatibleThreadStateException, AbsentInformationException {

    }
}

