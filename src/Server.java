import java.net.InetAddress;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.io.*;


public class Server {
    public static void main(String args[]) {
        InputStreamReader is =
                new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);
        String portNum, registryURL, currentIp;
        try{
            //call for a port number
            System.out.println("Enter the RMIregistry port number:");
            portNum = br.readLine();
            int RMIPortNum = Integer.parseInt(portNum);
            startRegistry(RMIPortNum);
            ServerImpl exportedObj =new ServerImpl();
            InetAddress iAddress = InetAddress.getLocalHost();
            currentIp = iAddress.getHostAddress();
            //get the IP address and show it
            System.out.println("Current IP address: " +currentIp);
            registryURL ="rmi://" + currentIp +":" + portNum + "/some";
            //stay in a rebind position waiting for a connection from clients.
            Naming.rebind(registryURL, exportedObj);
            System.out.println("Callback Server ready.");
        }// end try
        catch (Exception re) {
            System.out.println("Exception in Server.main: " + re);
        } // end catch
    } // end main

    //This method starts a RMI registry on the local host, if
    //it does not already exists at the specified port number.
    private static void startRegistry(int RMIPortNum)
            throws RemoteException{
        try {
            Registry registry =LocateRegistry.getRegistry(RMIPortNum);
            registry.list( );
            // This call will throw an exception
            // if the registry does not already exist
        }
        catch (RemoteException e) {
            // No valid registry at that port.
            Registry registry =LocateRegistry.createRegistry(RMIPortNum);
        }
    } // end startRegistry

} // end class
