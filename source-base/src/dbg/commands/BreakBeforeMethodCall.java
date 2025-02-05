package dbg.commands;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.BreakpointRequest;
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
    public void execute(VirtualMachine vm, LocatableEvent event) throws AbsentInformationException {
        for( ReferenceType targetClass : vm.allClasses()) {
            if(targetClass.name().equals("dbg.JDISimpleDebuggee")){
                for ( Method method : targetClass.allMethods()){
                    if(method.name().equals(methodName)){
                        Location location = method.location();
                        BreakpointRequest bpReq = vm. eventRequestManager().createBreakpointRequest(location);
                        System.out.println("Breakpoint ajouté avant l'appel de la méthode: "+method.name()+"  "+ location);
                        bpReq.enable();
                    }
                }

            }
        }
    }
}
