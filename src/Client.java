import java.io.*;
import java.nio.Buffer;
import java.rmi.*;

/**
 * This class represents the object client for a
 * distributed object of class ServerImpl,
 * which implements the remote interface 
 * CallbackServerInterface.  It also accepts callback
 * from the server.
 *
 *
 *
 * @author M. L. Liu
 */

public class Client {

    public static void main(String args[]) {
        try {
            int RMIPort;
            String hostName;
            InputStreamReader is =
                    new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(is);
            System.out.println(
                    "Enter the RMIRegistry host namer:");
            hostName = br.readLine();
            System.out.println(
                    "Enter the RMIregistry port number:");
            String portNum = br.readLine();
            RMIPort = Integer.parseInt(portNum);
            /*System.out.println(
                    "Enter how many seconds to stay registered:");
            String timeDuration = br.readLine();
            int time = Integer.parseInt(timeDuration);*/
            String registryURL =
                    "rmi://localhost:" + portNum + "/callback";
            // find the remote object and cast it to an
            //   interface object
            CallbackServerInterface h =
                    (CallbackServerInterface)Naming.lookup(registryURL);
            System.out.println("Lookup completed " );
            System.out.println("Server said " + h.sayHello());
            CallbackClientInterface callbackObj =
                    new ClientImpl();
            // register for callback
            h.registerForCallback(callbackObj);
            System.out.println("Registered for callback.");
            /*try {
                Thread.sleep(time * 1000);
            }
            catch (InterruptedException ex){ // sleep over
            }
            h.unregisterForCallback(callbackObj);
            System.out.println("Unregistered for callback.");*/

            checkCorrectOption();



            System.out.println("Enter the filename to upload");
            //Get the file name
            String fileNameUp = br.readLine();
            File file = callbackObj.getFile(fileNameUp);

            //Create a byte array to send
            byte[] fileBytes = callbackObj.fileToBytes(file);
            //Get the Destination path
            File fileDest = new File("./receivedData/"+fileNameUp);
            //Upload the file to the server
            System.out.println(h.upload(fileBytes, fileDest));

            System.out.println("Enter the filename to download");
            String fileNameDwn = br.readLine();
            File fileDestDwn = new File("./sharedData/"+fileNameDwn);
            byte[] downfileBytes = h.download(fileNameDwn);

            FileOutputStream fileOuputStream = new FileOutputStream(fileDestDwn);

            if(downfileBytes.length != 0) {
                fileOuputStream.write(downfileBytes);
                fileOuputStream.close();
                System.out.println("File: " + fileDestDwn + " downloaded correctly.");
            }
            else {
                System.out.println("Download error!!!!");
            }



        } // end try
        catch (Exception e) {
            System.out.println(
                    "Exception in Client: " + e);
        } // end catch
    } //end main

    private static void checkCorrectOption() {

        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);

        boolean correctOption = false;

        while(!correctOption){

            System.out.println("\nChoose your option:");
            System.out.println("Download[D] Upload[U] Search[S] Delete[R]");

            String option = "";
            try {
                option = br.readLine();
            } catch (IOException e) {
            }

            correctOption= isCorrectOption(option);

        }
    }

    private static boolean isCorrectOption(String option) {
        switch (option){

            case "D": //Download
                downloadOption();
                return true;

            case "U": //Upload
                uploadOption();
                return true;

            case "S": //Search
                searchOption();
                return true;

            case "R": //Delete
                deleteOption();
                return true;

            default:
                System.out.println("Incorrect option\n");
                return false;
        }
    }

    private static void uploadOption() {
    }

    private static void downloadOption() {
    }

    private static void searchOption() {
    }

    private static void deleteOption() {
    }

}//end class