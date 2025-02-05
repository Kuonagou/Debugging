package dbg.commands;

import com.sun.jdi.*;
import com.sun.jdi.event.LocatableEvent;

import java.util.List;
import java.util.Map;

public class Temporaries implements Icommande{
    /**
     *  renvoie et imprime la liste des variables temporaires de la frame
     * courante, sous la forme de couples nom = valeur.
     */
    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) throws IncompatibleThreadStateException, AbsentInformationException {
        StackFrame frame = event.thread().frame(0);
        Map<LocalVariable, Value> variables = frame.getValues(frame.visibleVariables());

        System.out.println("Variables Locales:");
        for (Map.Entry<LocalVariable, Value> entry : variables.entrySet()) {
            System.out.println(entry.getKey().name() + " -> " + entry.getValue());
        }
    }
}
