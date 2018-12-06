import java.io.*;
import java.rmi.*;
import java.rmi.server.*;

/**
 * This class implements the remote interface
 * CallbackClientInterface.
 * @author M. L. Liu
 */

public class ClientImpl extends UnicastRemoteObject
        implements CallbackClientInterface {


    public ClientImpl() throws RemoteException {
        super( );
    }

    public String notifyMe(String message){
        String returnMessage = "Call back received: " + message;
        System.out.println(returnMessage);
        return returnMessage;
    }

}// end ClientImpl class
