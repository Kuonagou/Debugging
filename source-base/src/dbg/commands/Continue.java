package dbg.commands;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;

public class Continue implements Icommande {
    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) {
        vm.resume();
    }
}
