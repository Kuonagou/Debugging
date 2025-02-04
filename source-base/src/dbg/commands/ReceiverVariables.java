package dbg.commands;

import com.sun.jdi.*;
import com.sun.jdi.event.LocatableEvent;

import java.util.Map;

public class ReceiverVariables implements Icommande{
    @Override
    public void execute(VirtualMachine vm, LocatableEvent event) throws IncompatibleThreadStateException {
            StackFrame frame = event.thread().frame(0);
            ObjectReference receiver = frame.thisObject();
            if (receiver != null) {
                Map<com.sun.jdi.Field, Value> fields = receiver.getValues(receiver.referenceType().fields());
                for (Map.Entry<Field, Value> entry : fields.entrySet()) {
                    System.out.println(entry.getKey().name() + " → " + entry.getValue());
                }
            }
    }
}
