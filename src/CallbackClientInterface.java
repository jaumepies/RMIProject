import java.rmi.*;

public interface CallbackClientInterface extends Remote{

    public String notifyMe(String message) throws RemoteException;


} // end interface