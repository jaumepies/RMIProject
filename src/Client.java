import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.UnknownServiceException;
import java.rmi.*;
import java.util.ArrayList;
import java.util.regex.Pattern;


public class Client {

    static InputStreamReader is = new InputStreamReader(System.in);
    static BufferedReader br = new BufferedReader(is);
    static CallbackClientInterface callbackObj;
    static boolean isFinished = false;
    static boolean endExecution = false;

    static String currentUserName;
    static final String FILE_INFO = "./fileInfo.json";
    final public static int BUF_SIZE = 1024 * 64;




    public static void main(String args[]) {
        try {
            int RMIPort;
            String hostName;




            /*System.out.println("Enter the RMIRegistry host namer:");
            hostName = br.readLine();*/
            //ARREGLAR EL PORT -------------------------------------------------------------------
            //System.out.println("Enter the RMIregistry port number:");
            String portNum = "8001"; // br.readLine();
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
            while(!endExecution) {
                checkUserOption(h);
            }
            /*try {
                Thread.sleep(time * 1000);
            }
            catch (InterruptedException ex){ // sleep over
            }
            h.unregisterForCallback(callbackObj);
            System.out.println("Unregistered for callback.");*/
            System.out.println("Execution finished");


        } // end try
        catch (RemoteException e1)
        {
            System.out.println(
                    "Exception in Client: " + e1);
            // end catch
        } catch (Exception e) {
            System.out.println(
                    "Exception in Client: " + e);
        }
    } //end main

    private static void checkUserOption(CallbackServerInterface h) {
        boolean correctOption = false;
        String option = "";

        while(!correctOption){
            isFinished=false;
            System.out.println("\nChoose your option:");
            System.out.println("Login[L] New User[N] Exit[E]");

            try {
                option = br.readLine();
            } catch (IOException e) {
            }

            correctOption = isCorrectUserOption(option, h);

        }
    }

    private static boolean isCorrectUserOption(String option, CallbackServerInterface h) {

        switch (option){
            case "L": //Login
                logUser(h);
                return false;
            case "N": //New user
                registNewUser(h);
                return false;
            case "E": //Exit
                //h.unregisterForCallback(callbackObj);
                endExecution = true;
                return true;
            default:
                System.out.println("Incorrect option\n");
                return false;
        }
    }

    private static void registNewUser(CallbackServerInterface h) {
        try {
            String userName, password, confirmation;
            ArrayList<String> topicList = new ArrayList<String>();

            do{
                System.out.println("Enter user name:");
                userName = br.readLine();

                if (!h.checkCorrectUserName(userName)){
                    System.out.println("This name already exists!\n");
                }

            }while(!h.checkCorrectUserName(userName));

            do {
                System.out.println("Enter the password:");
                password = br.readLine();

                System.out.println("Confirm the password:");
                confirmation = br.readLine();

                if (!password.equals(confirmation))
                    System.out.println("Error, the password confirmation must be the same!!:\n");
            }
            while (!password.equals(confirmation));

            System.out.println("You want to subscribe to any topic? Yes[Y]/No[N]\n");
            String opt = br.readLine();
            if (opt.equals("Y")){
                System.out.println("Introduce a list of topics(Example: animal, videogame, horror):\n");
                String strTopics = br.readLine();
                String[]topicSplited = strTopics.split(",");
                for (String elem: topicSplited) {
                    topicList.add(elem.trim());
                }
            }
            int lastid = h.getLastIdFromUsers();
            User newUser = new User(userName, password, lastid+1);
            newUser.setSubscriptionList(topicList);
            System.out.println(h.registerNewUser(newUser));


        } catch(RemoteException e1)
        {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void logUser(CallbackServerInterface h) {

        try {
            String userName, password;
            boolean correctUser;
            do{
                System.out.println("Enter user name:");
                userName = br.readLine();

                System.out.println("Enter the password:");
                password = br.readLine();
                correctUser = h.checkCorrectUser(userName, password);
                if(!correctUser){
                    System.out.println("User name or password incorrect!");
                    System.out.println("Try it again...");
                }
            }while(!correctUser);

            if(correctUser){
                callbackObj = new ClientImpl();
                // register for callback
                h.registerForCallback(callbackObj);
                System.out.println("User connected and registered for a callback.");

                currentUserName = userName;
                while(!isFinished){
                    checkCorrectOption(h);
                }
            }



        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void checkCorrectOption(CallbackServerInterface h) {

        boolean correctOption = false;

        while(!correctOption){

            System.out.println("\nChoose your option:");
            System.out.println("Download[D] Upload[U] Search[S] Remove[R] Exit[E]");

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

            case "R": //Delete or Remove
                deleteOption(h);
                return true;

            case "M": //Modify
                modifyOption(h);
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
                File file = getFile(fileNameUp);

                if(file == null) {
                    System.out.println("The file does not exist");
                } else {
                    //Create a byte array to send
                    byte[] fileBytes = fileToBytes(file);
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
                    int idUser = h.getIdFromUser(currentUserName);
                    System.out.println(h.upload(fileBytes, fileDest, fileTitleUp, fileTagUp, idUser));
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

    private static byte[] fileToBytes(File file) {
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

    private static File getFile(String fileNameUp) {
        File file = new File("./sharedData/"+fileNameUp);
        if(file.exists()) {
            return  file;
        }
        return null;
    }

    private static void downloadOption(CallbackServerInterface h) {

        boolean isCorrectTitle = false;
        while (!isCorrectTitle) {
            System.out.println("Enter the title to download");
            try{
                String fileTitle = br.readLine();

                if(h.getFilesWithTitles(fileTitle).size() == 0) {
                    System.out.println("Title not found");
                }
                else{
                    isCorrectTitle = true;
                    /*if(h.getFilesWithTitles(fileTitle).size() == 1) {
                        System.out.println(h.downloadFile((JSONObject) h.getFilesWithTitles(fileTitle).get(0)));
                    } else {*/
                        ArrayList<String> listWithTitles = h.selectFile(h.getFilesWithTitles(fileTitle));
                        System.out.println("Return to menu[R]");
                        for(String str: listWithTitles) {
                            System.out.println(str);
                        }
                        String idFile = br.readLine();
                        if (idFile.equals("R")){
                            checkCorrectOption(h);
                        }
                        String fileName = h.getFileName(idFile);
                        System.out.println(h.downloadFileString(fileName));
                    //}
                }

            } catch (IOException e) {

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private static void searchOption(CallbackServerInterface h) {
        boolean isCorrectDescription = false;
        while (!isCorrectDescription) {
            System.out.println("Enter the description to search");
            try{
                String fileDescription = br.readLine();

                if(h.getFilesWithTitles(fileDescription).size() == 0) {
                    System.out.println("Description not found");
                }
                else{
                    isCorrectDescription = true;
                    System.out.println("Select the title to search");

                    ArrayList<String> selectTitleArray = h.selectFile(h.getFilesWithTitles(fileDescription));
                    System.out.println("Return to menu[R]");
                    for(String title: selectTitleArray) {
                        System.out.println(title);
                    }
                    String idFile = br.readLine();

                    if(idFile.equals("R")) {
                        checkCorrectOption(h);
                    }
                    ArrayList<String> infoTitle = h.showFileInfo(h.getFilesList(), idFile);
                    for(String info: infoTitle) {
                        System.out.println(info);
                    }
                }

            } catch (IOException e) {

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private static void deleteOption(CallbackServerInterface h) {
        boolean isCorrectTitle = false;
        while (!isCorrectTitle) {
            System.out.println("Enter the title to remove");
            try{
                String fileTitle = br.readLine();

                if(h.getFilesWithTitles(fileTitle).size() == 0) {
                    System.out.println("Title not found");
                }
                else{
                    isCorrectTitle = true;
                    System.out.println("Select the title to remove");

                    ArrayList<String> selectTitleArray = h.selectFile(h.getFilesWithTitles(fileTitle));
                    System.out.println("Return to menu[R]");
                    for(String title: selectTitleArray) {
                        System.out.println(title);
                    }
                    String idFile = br.readLine();
                    if(idFile.equals("R")) {
                        checkCorrectOption(h);
                    }

                    String deleteInfo = h.deleteFileInfo(h.getFilesList(), idFile);

                    System.out.println(deleteInfo);


                }

            } catch (IOException e) {

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private static void modifyOption(CallbackServerInterface h) {
    }

}//end class