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
    static final String FILE_INFO = "fileInfo.json";



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
            while(!isFinished) {
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

        while(!option.equals("E") ){

            System.out.println("\nChoose your option:");
            System.out.println("Login[L] New User[N] Exit[E]");

            try {
                option = br.readLine();
            } catch (IOException e) {
            }

            isCorrectUserOption(option, h);

        }
    }

    private static void isCorrectUserOption(String option, CallbackServerInterface h) {

        switch (option){
            case "L": //Login
                logUser(h);
                break;
            case "N": //New user
                registNewUser(h);
                break;
            case "E": //Exit
                try {
                    h.unregisterForCallback(callbackObj);
                    isFinished = true;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("Incorrect option\n");
        }
    }

    private static void registNewUser(CallbackServerInterface h) {
        try {
            String userName, password, confirmation;
            ArrayList<String> topicList = new ArrayList<String>();

            do{
                System.out.println("Enter user name:\n");
                userName = br.readLine();

                if (!h.checkCorrectUserName(userName)){
                    System.out.println("This name already exists!\n");
                }

            }while(!h.checkCorrectUserName(userName));

            do {
                System.out.println("Enter the password:\n");
                password = br.readLine();

                System.out.println("Confirm the password:\n");
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
            User newUser = new User(userName, password);
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
                searchOption();
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


    private static void downloadOptionANTIC(CallbackServerInterface h) {
        /*
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
        }*/
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
                    if(h.getFilesWithTitles(fileTitle).size() == 1) {
                        System.out.println(h.downloadFile((JSONObject) h.getFilesWithTitles(fileTitle).get(0)));
                    } else {
                        ArrayList<String> arrayString = h.selectFile(h.getFilesWithTitles(fileTitle));
                        for(String str: arrayString) {
                            System.out.println(str);
                        }
                        String fileInfo = br.readLine();
                        System.out.println(h.downloadFileString(fileInfo));


                    }
                }    /*
                    if(filesWithTitle.size() == 1) {
                        downloadFile((JSONObject) filesWithTitle.get(0), h);
                    } else {
                        JSONObject fileInfo = selectFile(filesWithTitle);
                        downloadFile(fileInfo, h);
                    }
                */

            } catch (IOException e) {

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private static void downloadFile(JSONObject jsonObject, CallbackServerInterface h) throws IOException {
/*
        String fileNameDwn = jsonObject.get("FileName").toString();
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
                System.out.println("File: " + copyName + " downloaded correctly.");
            }
            else {
                System.out.println("Download error!!!!");
            }
        }*/

    }

    private static JSONObject selectFile(JSONArray filesWithTitle) throws IOException {
        System.out.println("Choose the correct title");
        for (int i = 0; i < filesWithTitle.size(); i++) {
            Object f = filesWithTitle.get(i);
            JSONObject file = (JSONObject) f;
            System.out.print(file.get("Name") + "[" + i +"] ");
        }
        System.out.println();
        String selectTitle = br.readLine();
        return (JSONObject) filesWithTitle.get(Integer.parseInt(selectTitle));
    }

    private static JSONArray getFilesWithTitles(JSONArray filesList, String fileTitle) {
        JSONArray filesWithTitle = new JSONArray();

        for (Object f : filesList) {
            JSONObject file = (JSONObject) f;
            if(Pattern.compile(Pattern.quote(fileTitle), Pattern.CASE_INSENSITIVE).matcher((CharSequence)
                    file.get("FileName")).find() || Pattern.compile(Pattern.quote(fileTitle),
                    Pattern.CASE_INSENSITIVE).matcher((CharSequence) file.get("Tag")).find() ||
                    Pattern.compile(Pattern.quote(fileTitle), Pattern.CASE_INSENSITIVE).matcher((CharSequence)
                            file.get("Name")).find()){
                //System.out.println("Found title");
                filesWithTitle.add(file);
            } else {
                //System.out.println("Title not found");
            }
        }
        return filesWithTitle;
    }

    private static void searchOption() {
        boolean isCorrectDescription = false;
        while (!isCorrectDescription) {
            System.out.println("Enter the description to search");
            try{
                String fileText = br.readLine();

                JSONParser parser = new JSONParser();
                JSONArray filesList = (JSONArray) parser.parse(new FileReader(FILE_INFO));

                JSONArray filesWithText = getFilesWithTitles(filesList, fileText);

                if(filesWithText.size() == 0) {
                    System.out.println("Title not found");
                } else{
                    isCorrectDescription = true;
                    if(filesWithText.size() == 1) {
                        searchFile((JSONObject) filesWithText.get(0));
                    } else {
                        JSONObject fileInfo = selectFile(filesWithText);
                        searchFile(fileInfo);
                    }
                }

            } catch (IOException e) {

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }

    private static void searchFile(JSONObject jsonObject) {

        for (int i = 0; i < jsonObject.size(); i++) {

        }
    }

    private static void deleteOption(CallbackServerInterface h) {
    }

}//end class