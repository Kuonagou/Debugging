package dbg.commands;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.event.LocatableEvent;

public class BreakBeforeMethodCall implements Icommande {
    private String methodName;

    public BreakBeforeMethodCall(String methodName) {
        this.methodName = methodName;
    }

    /**
     * MOYEN MOYEN SUR DE CELLE LA
     * break-before-method-call(String methodName) : configure l’exécution
     * pour s’arrêter au tout début de l’exécution de la méthode methodName.
     */
    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) {
        EventRequestManager requestManager = vm.eventRequestManager();
        MethodEntryRequest methodEntryRequest = requestManager.createMethodEntryRequest();
        methodEntryRequest.addClassFilter("*"); // Filtrer toutes les classes
        methodEntryRequest.setSuspendPolicy(MethodEntryRequest.SUSPEND_ALL);
        methodEntryRequest.enable();

        System.out.println("Breakpoint ajouté avant l'appel de la méthode: " + methodName);
    }
}
