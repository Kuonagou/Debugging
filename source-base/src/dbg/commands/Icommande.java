package dbg.commands;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;

import javax.naming.Context;
import javax.naming.NamingException;

public interface Icommande {
    public void execute(VirtualMachine vm, LocatableEvent event) throws Exception,IncompatibleThreadStateException, AbsentInformationException;
}
