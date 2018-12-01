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

    static InputStreamReader is = new InputStreamReader(System.in);
    static BufferedReader br = new BufferedReader(is);
    static CallbackClientInterface callbackObj;
    static boolean isFinished = false;


    public static void main(String args[]) {
        try {
            int RMIPort;
            String hostName;

            System.out.println("Enter the RMIRegistry host namer:");
            hostName = br.readLine();
            System.out.println("Enter the RMIregistry port number:");
            String portNum = br.readLine();
            RMIPort = Integer.parseInt(portNum);
            /*System.out.println(
                    "Enter how many seconds to stay registered:");
            String timeDuration = br.readLine();
            int time = Integer.parseInt(timeDuration);*/
            String registryURL ="rmi://localhost:" + portNum + "/callback";
            // find the remote object and cast it to an
            //   interface object
            CallbackServerInterface h = (CallbackServerInterface)Naming.lookup(registryURL);
            System.out.println("Lookup completed " );
            System.out.println("Server said " + h.sayHello());
            callbackObj = new ClientImpl();
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
            while(!isFinished){
                checkCorrectOption(h);
            }
            System.out.println("Execution finished");


        } // end try
        catch (Exception e) {
            System.out.println(
                    "Exception in Client: " + e);
        } // end catch
    } //end main

    private static void checkCorrectOption(CallbackServerInterface h) {

        boolean correctOption = false;

        while(!correctOption){

            System.out.println("\nChoose your option:");
            System.out.println("Download[D] Upload[U] Search[S] Delete[R] Exit[E]");

            String option = "";
            try {
                option = br.readLine();
            } catch (IOException e) {
            }

            correctOption= isCorrectOption(option, h);

        }
    }

    private static boolean isCorrectOption(String option, CallbackServerInterface h) {

        switch (option){

            case "D": //Download
                downloadOption(h);
                return true;

            case "U": //Upload
                uploadOption(h);
                return true;

            case "S": //Search
                searchOption(h);
                return true;

            case "R": //Delete
                deleteOption(h);
                return true;

            case "E": //Exit
                try {
                    h.unregisterForCallback(callbackObj);
                    isFinished = true;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                System.out.println("Incorrect option\n");
                return false;
        }
    }

    private static void uploadOption(CallbackServerInterface h) {
        boolean isCorrectFile = false;

        while(!isCorrectFile){
            System.out.println("Enter the filename to upload");
            try{
                //Get the file name
                String fileNameUp = br.readLine();
                File file = callbackObj.getFile(fileNameUp);

                if(file == null) {
                    System.out.println("The file does not exist");
                } else {
                    //Create a byte array to send
                    byte[] fileBytes = callbackObj.fileToBytes(file);
                    //Get the Destination path
                    File fileDest = new File("./receivedData/"+fileNameUp);
                    String copyName = fileNameUp;
                    while(fileDest.exists()) {
                        copyName += "1";
                        fileDest = new File("./receivedData/"+copyName);
                    }
                    String fileTitleUp = getTitle();
                    String fileTagUp = getTag();

                    if(copyName != fileNameUp) {
                        System.out.println("The file already exists and it has been modified to "+ copyName);
                    }

                    //Upload the file to the server
                    System.out.println(h.upload(fileBytes, fileDest, fileTitleUp, fileTagUp));
                    isCorrectFile = true;
                }

            } catch (IOException e){
            }
        }

    }

    private static String getTitle() {
        String title = "";
        try{
            do{
                System.out.println("Enter the title of file:");
                title = br.readLine();
            }while(title.equals(""));
        }catch(IOException e){

        }
        return title;
    }

    private static String getTag() {
        String tag = "";
        try{
            System.out.println("Enter the tag of file:");
            tag = br.readLine();
        }catch(IOException e){

        }
        return tag;
    }

    private static void downloadOption(CallbackServerInterface h) {

        boolean isCorrectFile = false;
        while (!isCorrectFile) {
            System.out.println("Enter the filename to download");
            try{
                String fileNameDwn = br.readLine();
                String copyName = fileNameDwn;
                File fileDestDwn = new File("./sharedData/"+fileNameDwn);

                while(fileDestDwn.exists()) {
                    copyName += "1";
                    fileDestDwn = new File("./sharedData/"+copyName);
                }
                if(copyName != fileNameDwn) {
                    System.out.println("The file already exists and it has been modified to "+ copyName);
                }

                byte[] downfileBytes = h.download(fileNameDwn);

                if(downfileBytes == null) {
                    System.out.println("The file does not exist");
                } else {
                    FileOutputStream fileOuputStream = new FileOutputStream(fileDestDwn);

                    if(downfileBytes.length != 0) {
                        fileOuputStream.write(downfileBytes);
                        fileOuputStream.close();
                        System.out.println("File: " + fileDestDwn + " downloaded correctly.");
                    }
                    else {
                        System.out.println("Download error!!!!");
                    }
                    isCorrectFile = true;
                }

            } catch (IOException e) {

            }
        }
    }

    private static void searchOption(CallbackServerInterface h) {
    }

    private static void deleteOption(CallbackServerInterface h) {
    }

}//end class