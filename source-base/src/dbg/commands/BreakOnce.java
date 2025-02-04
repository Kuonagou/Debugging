package dbg.commands;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.Location;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;


public class BreakOnce implements Icommande{
    private String className;
    private int lineNumber;

    public BreakOnce(String className, int lineNumber) {
        this.className = className;
        this.lineNumber = lineNumber;
    }
    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) throws AbsentInformationException {
        for (ReferenceType targetClass : vm.allClasses()) {
            if (targetClass.name().equals(className)) {
                Location location = targetClass.locationsOfLine(lineNumber).get(0);
                BreakpointRequest bpReq = vm.eventRequestManager().createBreakpointRequest(location);
                bpReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
                bpReq.enable();
                System.out.println("Breakpoint temporaire ajouté à " + className + ":" + lineNumber);
            }
        }
    }
}
