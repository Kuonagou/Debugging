package dbg.commands;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.BreakpointRequest;

import java.util.NoSuchElementException;

public class Break implements Icommande{
    private String className;
    private int lineNumber;

    public Break(String className, int lineNumber) {
        this.className = className;
        this.lineNumber = lineNumber;
    }

    /**
     *  break(String filename, int lineNumber) : installe un point d’arrêt à la ligne
     * lineNumber du fichier fileName.
     */
    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) throws AbsentInformationException {
        try{
            for( ReferenceType targetClass : vm.allClasses()) {
                if(targetClass.name().equals(className)){
                    Location location = targetClass.locationsOfLine(lineNumber).getFirst() ;
                    BreakpointRequest bpReq = vm. eventRequestManager().createBreakpointRequest(location);
                    bpReq.enable();
                }
            }
        } catch(NoSuchElementException e){
            System.out.println("Pas de de code à cette ligne");
        }
    }
}
