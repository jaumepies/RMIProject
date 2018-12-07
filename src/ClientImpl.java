import java.io.*;
import java.rmi.*;
import java.rmi.server.*;


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
