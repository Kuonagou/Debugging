package dbg.commands;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.StepRequest;

public class Step implements Icommande {
    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) {
        if (event instanceof StepEvent) {
            event.request().disable();
        }
        StepRequest stepRequest = vm.eventRequestManager().createStepRequest(event.thread(),
                StepRequest.STEP_MIN,
                StepRequest.STEP_INTO);
        stepRequest.enable();
    }
}
