package dbg.commands;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.BreakpointRequest;

public class BreakOnCount implements Icommande{
    private String className;
    private int lineNumber;
    private int count;

    public BreakOnCount(String className, int lineNumber, int count) {
        this.className = className;
        this.lineNumber = lineNumber;
        this.count = count;
    }

    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) throws AbsentInformationException {
        for (ReferenceType targetClass : vm.allClasses()) {
            if (targetClass.name().equals(className)) {
                Location location = targetClass.locationsOfLine(lineNumber).get(0);
                BreakpointRequest bpReq = vm.eventRequestManager().createBreakpointRequest(location);
                bpReq.addCountFilter(count);
                bpReq.enable();
                System.out.println("Breakpoint ajouté à " + className + ":" + lineNumber + " (compteur: " + count + ")");
            }
        }
    }
}
