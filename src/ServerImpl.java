import com.sun.corba.se.impl.copyobject.JavaStreamObjectCopierImpl;

import java.io.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.Vector;

/**
 * This class implements the remote interface
 * CallbackServerInterface.
 * @author M. L. Liu
 */

public class ServerImpl extends UnicastRemoteObject
        implements CallbackServerInterface {

    private Vector clientList;
    final public static int BUF_SIZE = 1024 * 64;


    public ServerImpl() throws RemoteException {
        super( );
        clientList = new Vector();
    }

    public String sayHello( )
            throws java.rmi.RemoteException {
        return("hello");
    }

    public synchronized void registerForCallback(
            CallbackClientInterface callbackClientObject)
            throws java.rmi.RemoteException{
        // store the callback object into the vector
        if (!(clientList.contains(callbackClientObject))) {
            clientList.addElement(callbackClientObject);
            System.out.println("Registered new client ");
            doCallbacks();
        } // end if
    }

    // This remote method allows an object client to
// cancel its registration for callback
// @param id is an ID for the client; to be used by
// the server to uniquely identify the registered client.
    public synchronized void unregisterForCallback(
            CallbackClientInterface callbackClientObject)
            throws java.rmi.RemoteException{
        if (clientList.removeElement(callbackClientObject)) {
            System.out.println("Unregistered client ");
        } else {
            System.out.println(
                    "unregister: clientwasn't registered.");
        }
    }

    private synchronized void doCallbacks( ) throws java.rmi.RemoteException{
        // make callback to each registered client
        System.out.println(
                "**************************************\n"
                        + "Callbacks initiated ---");
        for (int i = 0; i < clientList.size(); i++){
            System.out.println("doing "+ i +"-th callback\n");
            // convert the vector object to a callback object
            CallbackClientInterface nextClient = (CallbackClientInterface)clientList.elementAt(i);
            // invoke the callback method
            nextClient.notifyMe("Number of registered clients="
                    +  clientList.size());
        }// end for
        System.out.println("********************************\n" +
                "Server completed callbacks ---");
    } // doCallbacks

    public File getFileToDownload(String fileName){

        File file = new File("./receivedData/"+fileName);
        if(!file.exists()) {
            return null;
        }
        return file;
    }

    @Override
    public byte[] fileToBytes(File file) {
        /*
        FileInputStream fileInputStream = new FileInputStream(file);
        ByteArrayOutputStream bOutputStream = new ByteArrayOutputStream();
        bOutputStream.toByteArray(fileInputStream);
        */
        byte[] bytes = new byte[BUF_SIZE];

        try {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //create FileInputStream which obtains input bytes from a file in a file system
            //FileInputStream is meant for reading streams of raw bytes such as image data. For reading streams of characters, consider using FileReader.

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[BUF_SIZE];
            try {
                for (int readNum; (readNum = fis.read(buf)) != -1;) {
                    //Writes to this byte array output stream
                    bos.write(buf, 0, readNum);
                    // System.out.println("read " + readNum + " bytes,");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            bytes = bos.toByteArray();
            bos.close(); // should be inside a finally block


        } catch (IOException e) {
            // handle IOException
            e.printStackTrace();
        }

        return bytes;
    }

    @Override
    public String upload(byte[] bytes, File fileDest, String name, String tag) {

        try {
            FileOutputStream fileOuputStream = new FileOutputStream(fileDest);
            DataObject fileInfo = new DataObject(name, tag);
            FileWriter filewr = new FileWriter("./"+fileDest.getName()+".json");
            fileOuputStream.write(bytes);
            fileOuputStream.close();

            filewr.write(fileInfo.createJSONObject().toString());
            filewr.flush();
            filewr.close();
            System.out.println("File: " + fileDest.getName() + " uploaded correctly.");
            return "File: " + fileDest.getName() + " uploaded correctly.";
        }  catch (IOException e) {
            e.printStackTrace();
            return "Upload error!!!!";
        }
    }

    @Override
    public byte[]  download( String name) {
        //byte[] bytes = new byte[BUF_SIZE];
        File fileSource = getFileToDownload(name);

        if(fileSource == null) {
            return null;
        }
        byte[] fileBytes = fileToBytes(fileSource);

        System.out.println("File: " + fileSource.getName() + " downloaded correctly.");

        return fileBytes;

    }
}// end ServerImpl class
