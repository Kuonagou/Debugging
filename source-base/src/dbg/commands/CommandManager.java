package dbg.commands;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class CommandManager {


    private Icommande commande;


    public CommandManager() {}


    public void executeCommand(VirtualMachine vm, LocatableEvent event, String command) throws IOException, IncompatibleThreadStateException, AbsentInformationException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


        switch (command) {
            case "step":
                commande = new Step();
                commande.execute(vm,event);
                break;
            case "step over":
                commande = new StepOver();
                commande.execute(vm,event);
                break;
            case "continue":
                commande = new Continue();
                commande.execute(vm,event);
                break;
            case "frame":
                commande = new Frame();
                commande.execute(vm,event);
                break;
            case "temporaries":
                commande = new Temporaries();
                commande.execute(vm,event);
                break;
            case "stack":
                commande = new Stack();
                commande.execute(vm,event);
                break;
            case "receiver":
                commande = new Receiver();
                commande.execute(vm,event);
                break;
            case "sender":
                commande = new Sender();
                commande.execute(vm,event);
                break;
            case "receiver variables":
                commande = new ReceiverVariables();
                commande.execute(vm,event);
                break;
            case "method":
                commande = new Method();
                commande.execute(vm,event);
                break;
            case "arguments":
                commande = new Arguments();
                commande.execute(vm,event);
                break;
            case "print var":
                System.out.print("Entrez le nom de du fichier > ");
                String name = reader.readLine();
                commande = new PrintVar(name);
                commande.execute(vm,event);
                break;
            case "break":
                System.out.print("Entrez le nom de la variable > ");
                String fileName1 = reader.readLine();
                System.out.print("Entrez le numero de la ligne > ");
                String lineNumber1 = reader.readLine();
                commande = new Break(fileName1, Integer.parseInt(lineNumber1));
                commande.execute(vm,event);
                break;
            case "break point":
                commande = new BreakPoints();
                commande.execute(vm,event);
                break;
            case "break once":
                System.out.print("Entrez le nom de la variable > ");
                String fileName2 = reader.readLine();
                System.out.print("Entrez le numero de la ligne > ");
                String lineNumber2 = reader.readLine();
                commande = new BreakOnce(fileName2, Integer.parseInt(lineNumber2));
                commande.execute(vm,event);
                break;
            case "break on count":
                System.out.print("Entrez le nom de la variable > ");
                String fileName3 = reader.readLine();
                System.out.print("Entrez le numero de la ligne > ");
                String lineNumber3 = reader.readLine();
                System.out.print("Entrez le nombre d'iterations > ");
                String count = reader.readLine();
                commande = new BreakOnCount(fileName3, Integer.parseInt(lineNumber3), Integer.parseInt(count));
                commande.execute(vm,event);
                break;
            case "break before method call":
                System.out.print("Entrez le nom de la methode > ");
                String methodName = reader.readLine();
                commande = new BreakBeforeMethodCall(methodName);
                commande.execute(vm,event);
                break;
            default:
                vm.resume();
                break;
        }
    }
}
