package dbg.commands;

import com.sun.jdi.*;
import com.sun.jdi.event.LocatableEvent;

import java.util.List;

public class Temporaries implements Icommande{
    /**
     *  renvoie et imprime la liste des variables temporaires de la frame
     * courante, sous la forme de couples nom = valeur.
     */
    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) throws IncompatibleThreadStateException, AbsentInformationException {
            StackFrame frame = event.thread().frame(0);
            List<LocalVariable> variables = frame.visibleVariables();
            for (LocalVariable var : variables) {
                Value value = frame.getValue(var);
                System.out.println(var.name() + " = " + value);
            }
    }
}
