package dbg.commands;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;


public class Continue implements Icommande {
    /**
     * ontinue : continue l’exécution jusqu’au prochain point d’arrêt.
     * La granularité est l’instruction step.
     */
    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) {
        vm.resume();
    }
}
