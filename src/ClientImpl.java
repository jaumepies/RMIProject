import java.io.*;
import java.rmi.*;
import java.rmi.server.*;


public class ClientImpl extends UnicastRemoteObject
        implements CallbackClientInterface {

    final public static int BUF_SIZE = 1024 * 64;

    public ClientImpl() throws RemoteException {
        super( );
    }

    public String notifyMe(String message){
        String returnMessage = "Call back received: " + message;
        System.out.println(returnMessage);
        return returnMessage;
    }

    public File getFile(String fileName){

        File file = new File("./sharedData/"+fileName);
        if(file.exists()) {
            return  file;
        }
        return null;
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
                System.out.println("El fitxer no existeix");
                //e.printStackTrace();
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
}// end ClientImpl class
