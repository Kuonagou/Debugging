package dbg.commands;

import com.sun.jdi.*;
import com.sun.jdi.event.LocatableEvent;

import java.util.List;
import java.util.Map;

public class ReceiverVariables implements Icommande {
    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) throws IncompatibleThreadStateException {
        StackFrame frame = event.thread().frame(0);
        ObjectReference receiver = frame.thisObject();
        if (receiver != null) {
            ReferenceType type = receiver.referenceType();
            List<Field> fields = type.fields();
            if (!fields.isEmpty()) {
                System.out.println("Variables:");
                for (Field field : fields) {
                    Value value = receiver.getValue(field);
                    System.out.println(field.name() + " -> " + value);
                }
            } else {
                System.out.println("Il n'y a pas de variables d'instances dans le receveur courant.");
            }
        }
    }
}
