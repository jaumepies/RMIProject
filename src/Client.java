
import org.json.simple.parser.ParseException;
import java.io.*;
import java.net.MalformedURLException;
import java.rmi.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client {

    static InputStreamReader is = new InputStreamReader(System.in);
    static BufferedReader br = new BufferedReader(is);
    static CallbackClientInterface callbackObj;
    static boolean isFinished = false;
    static boolean endExecution = false;
    static boolean jump = false;

    static String currentUserName;
    static final String FILE_INFO = "./fileInfo.json";
    final public static int BUF_SIZE = 1024 * 64;

    static String strIP;
    static String portNum;


    public static void main(String args[]) {
        try {
            boolean isConnected;
            do {
                //while the client is not connected, check the server info
                isConnected = entryServerInfo();
            }
            while (!isConnected);
            //do the lookup to the server with a IP and a port
            String registryURL = "rmi://" + strIP + ":" + portNum + "/some";
            CallbackServerInterface h = (CallbackServerInterface)Naming.lookup(registryURL);

            System.out.println("Lookup completed " );
            System.out.println("Server said " + h.sayHello());
            //show to the user which options he has.
            while(!endExecution) {
                checkUserOption(h);

            }
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

    private static boolean entryServerInfo() {
        try {
            //while the IP or the port number ara null, read the input.
            do {
                //read the IP, or if the server is local, read localhost
                System.out.println("Enter the Remote RMIServer IP or [localhost]:");
                strIP = br.readLine();
                System.out.println("Enter the RMIregistry port number:");
                portNum = br.readLine();
                if (strIP.equals("")) {
                    System.out.println("You must enter an IP");
                }
                if (portNum.equals("")) {
                    System.out.println("You must enter a port number");
                }
            }while (strIP.equals("") || portNum.equals(""));
            String registryURL = "rmi://" + strIP + ":" + portNum + "/some";
            //try to do a lookup to the server
            Naming.lookup(registryURL);
            return true;
            //if it its impossible to connect, show a message and return FALSE
            //to show an other time the input messages
        } catch (NotBoundException ignore) {
            System.out.println("Impossible to connect to server");
            return false;
        } catch (RemoteException ignore) {
            System.out.println("Impossible to connect to server");
            return false;
        } catch (MalformedURLException e) {
            System.out.println("Impossible to connect to server");
            return false;
        } catch (IOException e) {
            System.out.println("Impossible to connect to server");
            return false;
        }

    }

    private static void checkUserOption(CallbackServerInterface h) throws InterruptedException {

        boolean correctOption = false;
        String option = "";

        while (!correctOption) {
            //Check if you press R before in the Login menu or Register menu
            if(!jump) {
                isFinished = false;
                System.out.println("\nChoose your option:");
                System.out.println("Login[L] New User[N] Exit[E]");

                try {
                    option = br.readLine();
                } catch (IOException e) {
                }

                correctOption = isCorrectUserOption(option, h);
            } else {
                return;
            }
        }

    }

    private static boolean isCorrectUserOption(String option, CallbackServerInterface h) throws InterruptedException {

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
                System.out.println("Incorrect option");
                return false;
        }
    }

    private static void registNewUser(CallbackServerInterface h) {

        try {
            String userName, password, confirmation, opt;
            ArrayList<String> topicList = new ArrayList<>();

            do {
                do{
                    System.out.println("Enter user name / Return[R]");
                    //Get the username
                    userName = br.readLine();
                    if(userName.equals("R")) {
                        checkUserOption(h);
                        //This boolean is to check if you introduce R
                        jump = true;
                        return;
                    }
                    if (userName.length() == 0) {
                        System.out.println("Username is empty");
                        registNewUser(h);
                    } else {

                        //Check if the user exists
                        if (!h.checkCorrectUserName(userName.trim())) {
                            System.out.println("This name already exists!");
                        }
                    }
                }while(!h.checkCorrectUserName(userName));



                System.out.println("Enter the password:");
                password = br.readLine();

                System.out.println("Confirm the password:");
                confirmation = br.readLine();

                //Check if password is too short
                if( password.length() < 4){
                    System.out.println("Error, the password is too short. Minimum 4 characters");
                }

                //Check if the password if the same
                else if (!password.equals(confirmation))
                    System.out.println("Error, the password confirmation must be the same!!:");
            }
            while (!password.equals(confirmation) || password.length() < 4);

            do {
                System.out.println("You want to subscribe to any topic? Yes[Y]/No[N]");
                opt = br.readLine();
                if (opt.equals("Y")) {
                    System.out.println("Introduce a list of topics(Example: animal, videogame, horror):");
                    //Get the topics
                    String strTopics = br.readLine();
                    String[] topicSplited = strTopics.split(",");
                    //Convert to ArrayList
                    for (String elem : topicSplited) {
                        topicList.add(elem.trim());
                    }
                } else if (!opt.equals("N")) {
                    System.out.println("Incorrect option");
                }
            }while(!opt.equals("N") && !opt.equals("Y"));

            int lastid = h.getLastIdFromUsers();
            //Create a new user
            User newUser = new User(userName.trim(), password.trim(), lastid+1);
            //Set his topic List
            newUser.setSubscriptionList(topicList);
            System.out.println(h.registerNewUser(newUser));


        } catch(RemoteException e1)
        {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private static void logUser(CallbackServerInterface h) throws InterruptedException {

        try {
            String userName, password;
            boolean correctUser;
            do{
                System.out.println("Enter user name / Return[R]");
                //Get the username
                userName = br.readLine();
                if(userName.equals("R")) {
                    checkUserOption(h);
                    //This boolean is to check if you introduce R
                    jump = true;
                    return;
                }
                System.out.println("Enter the password:");
                //Get the password
                password = br.readLine();
                //Check if the user login is correct
                correctUser = h.checkCorrectUser(userName, password);
                if(!correctUser){
                    System.out.println("User name or password incorrect!");
                    System.out.println("Try it again...");
                }
            }while(!correctUser);

            if(correctUser){
                callbackObj = new ClientImpl();
                // register for callback
                h.registerForCallback(callbackObj, userName);
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
            System.out.println("Download[D] Upload[U] Search[S] Remove[R] Change[C] Manage Subscriptions[M] Log Out[L]");

            String option = "";
            try {
                option = br.readLine(); //Get the option
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

            case "C": //Change
                changeOption(h);
                return true;

            case "M": //Manage Subscription
                manageSubscriptionOption(h);
                return true;


            case "L": //Log Out
                try {
                    h.unregisterForCallback(callbackObj, currentUserName);
                    isFinished = true;
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                System.out.println("Incorrect option");
                return false;
        }
    }

    private static void uploadOption(CallbackServerInterface h) {
        boolean isCorrectFile = false;

        while(!isCorrectFile){
            System.out.println("Enter the filename to upload / Return[R]");
            try{
                //Get the filename
                String fileNameUp = br.readLine();

                if (fileNameUp.equals("")){
                    System.out.println("The file is empty");

                } else if(fileNameUp.equals("R")) {
                    //Return to the menu
                    isCorrectFile = true;
                    checkCorrectOption(h);
                }
                else {

                    //Get the file to upload
                    File file = getFile(fileNameUp);

                    if(file == null) {
                        System.out.println("The file does not exists");
                    } else {
                        //Create a byte array to send
                        byte[] fileBytes = fileToBytes(file);
                        //Get the Destination path
                        File fileDest = new File("./Server/"+fileNameUp);
                        //Copy the name to check if it changes
                        String copyName = fileNameUp;
                        while(fileDest.exists()) {
                            copyName += "1";
                            fileDest = new File("./Server/"+copyName);
                        }
                        //Get the title of the file
                        String fileTitleUp = getTitle();
                        //Get the topicList of the file
                        ArrayList<String> fileTopicListUp = getTopicList();

                        if(copyName != fileNameUp) {
                            System.out.println("The file already exists and it has been modified to "+ copyName);
                        }

                        //Get the id user
                        int idUser = h.getIdFromUser(currentUserName);
                        //Upload the file to the server
                        System.out.println(h.upload(fileBytes, fileDest, fileTitleUp, fileTopicListUp, idUser));
                        isCorrectFile = true;
                    }
                }

            } catch (IOException e){
            }
        }

    }

    private static String getTitle() {

        String title = "";
        try{
            do{
                System.out.println("Enter the title of the file:");
                title = br.readLine();
            }while(title.equals(""));
        }catch(IOException e){

        }
        return title;
    }

    private static ArrayList<String> getTopicList() {
        ArrayList<String> topicList = new ArrayList<>();
        String strTopics;
        try{
            System.out.println("Enter the topic of the file:");
            strTopics = br.readLine();
            //Add the topics to Arraylist
            String[] topicSplited = strTopics.split(",");
            for (String elem: topicSplited) {
                topicList.add(elem.trim());
            }
        }catch(IOException e){

        }
        return topicList;
    }

    private static byte[] fileToBytes(File file) {

        //Converting the file to bytes
        byte[] bytes = new byte[BUF_SIZE];

        try {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                System.out.println("The file does not exists");
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[BUF_SIZE];
            try {
                for (int readNum; (readNum = fis.read(buf)) != -1;) {
                    //Writes to this byte array output stream
                    bos.write(buf, 0, readNum);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            bytes = bos.toByteArray();
            bos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    private static File getFile(String fileNameUp) {
        //Get the file from Client's folder
        File file = new File("./Client/"+fileNameUp);
        if(file.exists()) {
            return  file;
        }
        return null;
    }

    private static void downloadOption(CallbackServerInterface h) {

        boolean isCorrectTitle = false;
        while (!isCorrectTitle) {
            System.out.println("Enter the title to download / Return[R]");
            try{
                //Get the title
                String fileTitle = br.readLine();
                if (fileTitle.equals("")) {
                    System.out.println("The title is empty");
                } else if(fileTitle.equals("R")) {
                    //Return to menu
                    isCorrectTitle = true;
                    checkCorrectOption(h);
                }
                else {
                    //Check the size of the files with this title
                    if (h.getFilesWithTitles(fileTitle).size() == 0) {
                        System.out.println("Title not found");
                    } else {
                        isCorrectTitle = true;

                        //Get the files with this titles
                        ArrayList<String> listWithTitles = h.selectFile(h.getFilesWithTitles(fileTitle));

                        System.out.println("Return to menu[R]");
                        //Show the titles
                        for (String str : listWithTitles) {
                            System.out.println(str);
                        }
                        String idFile = br.readLine();

                        String checkIdFile = "["+idFile+"]";
                        boolean isCorrectId = false;

                        //Check if id is in the list shown
                        for (String str : listWithTitles) {
                            if(str.contains(checkIdFile)){
                                isCorrectId = true;
                            }
                        }

                        if (idFile.equals("R")) {
                            //Return to menu
                            checkCorrectOption(h);
                        } else if(isCorrectId) {
                            //The id is in the list shown
                            //Get the filename
                            String fileName = h.getFileName(idFile);
                            //Download the filename
                            System.out.println(h.downloadFileString(fileName));

                        } else {
                            System.out.println("Invalid id");
                        }

                    }
                }

            } catch (IOException e) {
                System.out.println("Error");

            } catch (ParseException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void searchOption(CallbackServerInterface h) {
        boolean isCorrectDescription = false;
        while (!isCorrectDescription) {
            System.out.println("Enter the description to search / Return[R]");
            try{
                //Get the description
                String fileDescription = br.readLine();

                if (fileDescription.equals("")) {
                    System.out.println("The title is empty");
                } else if(fileDescription.equals("R")) {
                    //Return to menu
                    isCorrectDescription = true;
                    checkCorrectOption(h);
                }
                else {

                    //Check the size of the files with the description
                    if (h.getFilesWithTitles(fileDescription).size() == 0) {
                        System.out.println("Description not found");
                    } else {
                        isCorrectDescription = true;
                        System.out.println("Select the title to search");

                        //Get the titles with the description
                        ArrayList<String> selectTitleArray = h.selectFile(h.getFilesWithTitles(fileDescription));
                        System.out.println("Return to menu[R]");
                        //Show the titles
                        for (String title : selectTitleArray) {
                            System.out.println(title);
                        }
                        String idFile = br.readLine();

                        String checkIdFile = "[" + idFile + "]";
                        boolean isCorrectId = false;

                        //Check if the id introduced is valid
                        for (String str : selectTitleArray) {
                            if (str.contains(checkIdFile)) {
                                isCorrectId = true;
                            }
                        }

                        if (idFile.equals("R")) {
                            checkCorrectOption(h);
                        } else if (isCorrectId) {
                            //Show the info of the file
                            ArrayList<String> infoTitle = h.showFileInfo(h.getFilesList(), idFile);
                            for (String info : infoTitle) {
                                System.out.println(info);
                            }
                        } else {
                            System.out.println("Invalid id");
                        }
                    }
                }

            } catch (IOException e) {

            } catch (ParseException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void deleteOption(CallbackServerInterface h) {
        boolean isCorrectTitle = false;
        while (!isCorrectTitle) {
            System.out.println("Enter the title to remove");
            try{
                //Get the title to delete
                String fileTitle = br.readLine();

                //Check the size of the files with this title
                if(h.getFilesWithTitles(fileTitle).size() == 0) {
                    System.out.println("Title not found");
                }
                else{
                    isCorrectTitle = true;
                    System.out.println("Select the title to remove");

                    //Get the titles and their description
                    ArrayList<String> selectTitleArray = h.selectFileWithDescription(h.getFilesWithTitles(fileTitle));
                    System.out.println("Return to menu[R]");
                    //Show the titles with their description
                    for(String title: selectTitleArray) {
                        System.out.println(title);
                    }
                    String idFile = br.readLine();

                    String checkIdFile = "["+idFile+"]";
                    boolean isCorrectId = false;

                    //Check if the id is valid
                    for (String str : selectTitleArray) {
                        if(str.contains(checkIdFile)){
                            isCorrectId = true;
                        }
                    }

                    if(idFile.equals("R")) {
                        checkCorrectOption(h);
                    } else if(isCorrectId) {
                        //Check if are you sure
                        System.out.println("Are you sure? \n Yes[Y] / No[N]");
                        String sure = br.readLine();
                        if(sure.equals("Y")) {
                            //Delete the title
                            String deleteInfo = h.deleteFileInfo(h.getFilesList(), idFile, currentUserName);

                            System.out.println(deleteInfo);
                        }
                        else{
                            //Go to the delete menu
                            deleteOption(h);
                        }
                    } else {
                        System.out.println("Invalid id");
                    }
                }

            } catch (IOException e) {

            } catch (ParseException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void changeOption(CallbackServerInterface h){

        try {
            boolean isCorrectOption = false;
            while (!isCorrectOption) {
                System.out.println("Change Title[T] Change Description[D] Return to menu[R]");
                //Get the change option
                String option = br.readLine();
                switch (option){
                    case "R":
                        //Go to the menu
                        checkCorrectOption(h);
                        isCorrectOption = true;
                        break;

                    case "T":
                        //Go to change title
                        changeTitle(h);
                        isCorrectOption = true;
                        break;

                    case "D":
                        //Go to change description
                        changeDescription(h);
                        isCorrectOption = true;
                        break;

                    default:
                        //Invalid option
                        System.out.println("Incorrect option");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void changeTitle(CallbackServerInterface h) {
        boolean isCorrectTitle = false;
        while (!isCorrectTitle) {
            System.out.println("Enter the title to modify");
            try{
                //Get the title to modify
                String fileTitle = br.readLine();

                if(h.getFilesWithTitles(fileTitle).size() == 0) {
                    System.out.println("Title not found");
                }
                else{
                    isCorrectTitle = true;

                    String newTitle ="";
                    //Get the list of the files with this title
                    ArrayList<String> listWithTitles = h.selectFile(h.getFilesWithTitles(fileTitle));
                    System.out.println("Return to menu[R]");
                    //Show titles
                    for(String str: listWithTitles) {
                        System.out.println(str);
                    }
                    String idFile = br.readLine();
                    if (idFile.equals("R")){
                        checkCorrectOption(h);
                    } else{
                        System.out.println("Enter the new title");
                        //Get the new title
                         newTitle = br.readLine();
                    }
                    //Get the old title
                    String oldTitle = h.getName(idFile);
                    //Change old to new title
                    System.out.println(h.changeFileTitle(oldTitle, newTitle, currentUserName));
                }

            } catch (IOException e) {

            } catch (ParseException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void changeDescription(CallbackServerInterface h) {
        boolean isCorrectDescription = false;
        while (!isCorrectDescription) {
            System.out.println("Enter the topic description to modify");
            try{
                //Get the description to modify
                String fileDescription = br.readLine();

                if(h.getFilesWithTitles(fileDescription).size() == 0) {
                    System.out.println("Topic description not found");
                }
                else{
                    isCorrectDescription = true;

                    List<String> newDescriptionList = new ArrayList<>();
                    String newDescription = "";
                    String strings[] = {};
                    //Get the files with the description to modify
                    ArrayList<String> listWithDescription = h.selectFile(h.getFilesWithTitles(fileDescription));
                    System.out.println("Return to menu[R]");
                    for(String str: listWithDescription) {
                        System.out.println(str);
                    }
                    String idFile = br.readLine();
                    if (idFile.equals("R")){
                        checkCorrectOption(h);
                    } else{
                        System.out.println("Enter the topic description");
                        //Get the new description
                        newDescription = br.readLine();
                        strings = newDescription.split(",");

                    }
                    //Convert description string to arraylist
                    newDescriptionList = Arrays.asList(strings);
                    ArrayList<String> newDescriptionArrayList = new ArrayList<>(newDescriptionList);
                    //Get old description
                    ArrayList<String> oldDescription = h.getTopicDescription(idFile);
                    //Change old to new description
                    System.out.println(h.changeFileDecription(oldDescription, newDescriptionArrayList, currentUserName));
                }

            } catch (IOException e) {

            } catch (ParseException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void manageSubscriptionOption(CallbackServerInterface h) {
        try {
            boolean isCorrectOption = false;
            while (!isCorrectOption) {
                System.out.println("You are subscribed at: " + h.getSubscriptions(currentUserName));
                System.out.println("Add subscription[A] Delete subscription[D] Return to menu[R]");
                //Get the Subscription Option
                String option = br.readLine();
                switch (option){
                    case "R":
                        //Go to the menu
                        checkCorrectOption(h);
                        isCorrectOption = true;
                        break;

                    case "A":
                        //Go to add subscription
                        addSubscription(h);
                        isCorrectOption = true;
                        break;

                    case "D":
                        //Go to delete subscription
                        deleteSubscription(h);
                        isCorrectOption = true;
                        break;

                    default:
                        System.out.println("Incorrect option");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void deleteSubscription(CallbackServerInterface h) {
        System.out.println("Delete the subscription");
        try {
            //Get delete subscription
            String deleteSubscription = br.readLine();
            if(deleteSubscription.equals("")) {
                System.out.println("No subscription has been deleted");
            } else {

                String[] strings = deleteSubscription.split(",");

                ArrayList<String> deleteSubscriptionArrayList = new ArrayList<>();

                //Convert string to ArrayList
                for (String str : strings) {
                    deleteSubscriptionArrayList.add(str.trim());
                }

                //Delete the subscription
                System.out.println(h.deleteSubscription(deleteSubscriptionArrayList, currentUserName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static void addSubscription(CallbackServerInterface h) {
        System.out.println("Add the new subscription");
        try {
            //Get add subscription
            String newSubscription = br.readLine();

            if (newSubscription.equals("")) {
                System.out.println("No subscription has been added");
            } else {
                String[] strings = newSubscription.split(",");

                ArrayList<String> newSubscriptionArrayList = new ArrayList<>();

                //Convert string to ArrayList
                for (String str : strings) {
                    newSubscriptionArrayList.add(str.trim());
                }

                //Add new subscription
                System.out.println(h.addSubscription(newSubscriptionArrayList, currentUserName));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}//end class