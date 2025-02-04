package dbg.commands;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.BreakpointRequest;

import java.util.List;

public class BreakPoints implements Icommande{
    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) throws AbsentInformationException {
        List<BreakpointRequest> breakpoints = vm.eventRequestManager().breakpointRequests();
        if (breakpoints.isEmpty()) {
            System.out.println("Pas de breakpoints.");
        } else {
            for (BreakpointRequest bp : breakpoints) {
                System.out.println(bp.location().sourceName() + ":" + bp.location().lineNumber());
            }
        }
    }
}
