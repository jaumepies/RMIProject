import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.regexp.internal.RE;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;
import static java.lang.Math.toIntExact;

public class ServerImpl extends UnicastRemoteObject implements CallbackServerInterface {

    static JSONArray arrayJSON;

    static final String FILE_INFO = "fileInfo.json";
    static final String FILE_USERS = "users.json";
    static final String FILE_LASTIDUSER= "lastiduser.json";
    static final String FILE_LASTIDFILE= "lastidfile.json";

    public static final Semaphore semaphore = new Semaphore(1, true);

    private HashMap<Integer, CallbackClientInterface> clientHash;
    final public static int BUF_SIZE = 1024 * 64;

    public ServerImpl() throws RemoteException {
        super( );
        clientHash  = new HashMap<>();
        arrayJSON = new JSONArray();
    }

    public String sayHello( ) throws RemoteException {
        return("hello");
    }

    public synchronized void registerForCallback(CallbackClientInterface callbackClientObject, String userName)
            throws RemoteException, InterruptedException {
            // store the callback object into the clientHash
            semaphore.acquire();

            //get id from username
            Integer idUser = getIdFromUser(userName);
            //store the user key and the callback object in a HashMap
            //to control the user are logged
            if(!clientHash.containsKey(idUser)){
                clientHash.put(idUser, callbackClientObject);
                System.out.println("New client logged ");
                doCallbacks();
                semaphore.release();
            }
            semaphore.release();
    }

    // This remote method allows an object client to
    // cancel its registration for callback
    // @param id is an ID for the client; to be used by
    // the server to uniquely identify the registered client.
    public synchronized void unregisterForCallback(CallbackClientInterface callbackClientObject, String userName)
            throws InterruptedException {

        semaphore.acquire();

        Integer idUser = getIdFromUser(userName);
        if (clientHash.remove(idUser, callbackClientObject)) {
            System.out.println("Client "+ userName +" logged out");
            semaphore.release();
        }
        else {
            System.out.println("client wasn't logged.");
            semaphore.release();
        }
    }

    private synchronized void doCallbacks( ) throws RemoteException{
        // make callback to each logged client
        int index = 0;
        System.out.println("**************************************\n" + "Callbacks initiated ---");
        Set set = clientHash.entrySet();
        Iterator iter = set.iterator();
        while(iter.hasNext()){
            System.out.println("doing "+ index +"-th callback\n");
            Map.Entry mentry = (Map.Entry)iter.next();
            CallbackClientInterface nextClient = (CallbackClientInterface)mentry.getValue();
            nextClient.notifyMe("Number of logged clients=" +  clientHash.size());
            index++;
        }
        System.out.println("********************************\n" + "Server completed callbacks ---");
    } // doCallbacks

    public File getFileToDownload(String fileName) {
        //Get the file from Server's folder with the filename
        File file = new File("./Server/"+fileName);
        if(!file.exists()) {
            return null;
        }
        return file;
    }

    @Override
    public byte[] fileToBytes(File file) {
        //This method is created to convert the file to bytes
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
    public String upload(byte[] bytes, File fileDest, String name, ArrayList<String> topicList, int idUser) {

        try {
            semaphore.acquire();
            FileOutputStream fileOuputStream = new FileOutputStream(fileDest);
            int idFile = getLastIdFromFile();
            //Update to the json the last id file
            updateLastIdFile(idFile);

            //Create a new file with his info
            DataObject fileInfo = new DataObject(name, topicList, fileDest.getName(), idUser, idFile);
            fileOuputStream.write(bytes);
            fileOuputStream.close();

            ArrayDataObject arrayDataObject = new ArrayDataObject();
            ObjectMapper objectMapper = new ObjectMapper();

            //Read from json file
            arrayDataObject = objectMapper.readValue(new File(FILE_INFO), ArrayDataObject.class);

            arrayDataObject.addDataObject(fileInfo);

            //Write to json file
            objectMapper.writeValue(new File(FILE_INFO), arrayDataObject);

            //Callbacks
            doSubcriptionCallbacks(topicList);

            System.out.println("File: " + fileDest.getName() + " uploaded correctly.");
            semaphore.release();
            return "File: " + fileDest.getName() + " uploaded correctly.";
        }  catch (IOException e) {
            e.printStackTrace();
            semaphore.release();
            return "Upload error!!!!";
        } catch (InterruptedException e) {
            e.printStackTrace();
            semaphore.release();
            return "Upload error!!!!";
        }
    }

    @Override
    public byte[]  download( String name) throws InterruptedException {

        //Get the file with the name file
        File fileSource = getFileToDownload(name);

        //If not exists
        if(fileSource == null) {
            return null;
        }
        //Convert the file to bytes
        byte[] fileBytes = fileToBytes(fileSource);

        System.out.println("File: " + fileSource.getName() + " downloaded correctly.");
        return fileBytes;
    }

    @Override
    public JSONArray getFilesWithTitles(String fileTitle) throws IOException, ParseException, InterruptedException {
        semaphore.acquire();

        //Get all the files
        JSONArray filesList = getFilesList();
        if(filesList == null) {
            semaphore.release();
            return new JSONArray();
        } else {
            JSONArray filesWithTitle = new JSONArray();

            for (Object f : filesList) {
                JSONObject file = (JSONObject) f;
                //Add the file to filesList if the text passes as parameter is in the file
                if (Pattern.compile(Pattern.quote(fileTitle), Pattern.CASE_INSENSITIVE).matcher((CharSequence)
                        file.get("fileName")).find() || Pattern.compile(Pattern.quote(fileTitle),
                        Pattern.CASE_INSENSITIVE).matcher(String.valueOf(file.get("topicList"))).find() ||
                        Pattern.compile(Pattern.quote(fileTitle), Pattern.CASE_INSENSITIVE).matcher((CharSequence)
                                file.get("name")).find()) {
                    filesWithTitle.add(file);
                }
            }
            semaphore.release();
            return filesWithTitle;
        }
    }

    public JSONArray getFilesList() throws ParseException, IOException, InterruptedException {
            semaphore.acquire();

            //In this method we get all the files from JSON file
            ObjectMapper mapper = new ObjectMapper();
            JSONParser parser = new JSONParser();

            String stringJSON = mapper.writeValueAsString(parser.parse(new FileReader(FILE_INFO)));

            JSONObject json = (JSONObject) parser.parse(stringJSON);

            semaphore.release();
            return (JSONArray) json.get("arrayListDataObject");

    }

    @Override
    public String downloadFile(JSONObject jsonObject) throws IOException, InterruptedException {
        //Get the string to download the file
        String fileNameDwn = jsonObject.get("fileName").toString();
        return downloadFileString(fileNameDwn);

    }

    public String downloadFileString(String fileNameDwn) throws IOException, InterruptedException {

        String copyName = fileNameDwn;
        File fileDestDwn = new File("./Client/"+fileNameDwn);

        //If file is in Client's folder we will change the name
        while(fileDestDwn.exists()) {
            copyName += "1";
            fileDestDwn = new File("./Client/"+copyName);
        }
        if(copyName != fileNameDwn) {
            System.out.println("The file already exists and it has been modified to "+ copyName);
        }

        //Convert the file to bytes
        byte[] downfileBytes = download(fileNameDwn);

        if(downfileBytes == null) {
            System.out.println("The file does not exists");
        } else {
            FileOutputStream fileOuputStream = new FileOutputStream(fileDestDwn);

            if(downfileBytes.length != 0) {
                //Create the file
                fileOuputStream.write(downfileBytes);
                fileOuputStream.close();
                System.out.println("File: " + copyName + " downloaded correctly.");
            }
            else {
                System.out.println("Download error!!!!");
            }
        }
        return "File: " + fileDestDwn.getName() + " downloaded correctly.";
    }

    @Override
    public ArrayList<String> selectFile(JSONArray filesWithTitle) throws InterruptedException {
        //Get the files with his id
        semaphore.acquire();
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < filesWithTitle.size(); i++) {
            Object f = filesWithTitle.get(i);
            JSONObject file = (JSONObject) f;
            arrayList.add(file.get("name") + "[" + file.get("id") +"] ");
        }
        semaphore.release();
        return arrayList;
    }

    @Override
    public ArrayList<String> selectFileWithDescription(JSONArray filesWithTitle) throws InterruptedException {
        semaphore.acquire();
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < filesWithTitle.size(); i++) {
            Object f = filesWithTitle.get(i);
            JSONObject file = (JSONObject) f;
            //Get the files with his description and his id
            arrayList.add("Title: " + file.get("name") + ", Topic Description: " +
                    file.get("topicList").toString().replace("[","").replace("]","").replace("\"", "")
                    + " [" + file.get("id") +"] ");
        }
        semaphore.release();
        return arrayList;
    }

    @Override
    public boolean checkCorrectUserName(String name) {
        try {
            semaphore.acquire();
            ArrayUsers arrayUsers = new ArrayUsers();
            ObjectMapper objectMapper = new ObjectMapper();

            //Read the JSON users file
            arrayUsers = objectMapper.readValue(new File(FILE_USERS), ArrayUsers.class);

            //Check if the username is in the JSON users file
            if (arrayUsers.exists(name)){
                semaphore.release();
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        semaphore.release();
        return true;
    }

    @Override
    public String registerNewUser(User newUser) throws RemoteException, InterruptedException {
        semaphore.acquire();
        ArrayUsers arrayUsers = new ArrayUsers();
        ObjectMapper objectMapper = new ObjectMapper();

        try {

            //Read the user file
            arrayUsers = objectMapper.readValue(new File(FILE_USERS), ArrayUsers.class);

            //Add the user passed as parameter
            arrayUsers.addUser(newUser);

            //Write the file with the new array users
            objectMapper.writeValue(new File(FILE_USERS), arrayUsers);

            //Store the last id.
            updateLastIdUser(newUser.getUserId());

            System.out.printf("User: "+ newUser.getUserName() + " was registered correctly.");
            semaphore.release();
            return "User: "+ newUser.getUserName() + " was registered correctly.";
        } catch (RemoteException e1){
            e1.printStackTrace();
            semaphore.release();
            return "Error, registering the new user!!!";
        } catch (IOException e) {
            e.printStackTrace();
            semaphore.release();
            return "Error, registering the new user!!!";
        }
    }

    private void updateLastIdUser(int newId)  {
        JSONParser jsonParser = new JSONParser();
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            Object obj = jsonParser.parse(new FileReader(FILE_LASTIDUSER));

            JSONObject jsonObject = (JSONObject) obj;

            //Get the last id User from JSON file
            int lastId = toIntExact((Long) jsonObject.get("lastIdUser"));

            if (lastId != newId){
                jsonObject.put("lastIdUser", newId);

                //Write the new id user in the JSON file
                objectMapper.writeValue(new File(FILE_LASTIDUSER), jsonObject);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void updateLastIdFile(int newId) throws InterruptedException {
        semaphore.acquire();
        JSONParser jsonParser = new JSONParser();
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            Object obj = jsonParser.parse(new FileReader(FILE_LASTIDFILE));

            JSONObject jsonObject = (JSONObject) obj;

            //Get the last id file from JSON file
            int lastId = toIntExact((Long) jsonObject.get("lastIdFile"));
            if (lastId != newId){
                jsonObject.put("lastIdFile", newId);

                //Write the new id file in the JSON file
                objectMapper.writeValue(new File(FILE_LASTIDFILE), jsonObject);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        semaphore.release();
    }

    public int getLastIdFromFile() throws RemoteException{
        JSONParser jsonParser = new JSONParser();
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            //Read the JSON
            Object obj = jsonParser.parse(new FileReader(FILE_LASTIDFILE));

            JSONObject jsonObject = (JSONObject) obj;
            //Check if the file is empty
            if(jsonObject.size() == 0) {
                //Introduce new id file 1
                jsonObject.put("lastIdFile", 1);
                //Write this object to the JSON file
                objectMapper.writeValue(new File(FILE_LASTIDFILE), jsonObject);
                return 1;
            }
            else{
                //Get the id from the object and add one unit
                int newId = toIntExact((Long) jsonObject.get("lastIdFile"))+1;
                return newId;
            }

        }catch(Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public int getLastIdFromUsers() throws RemoteException{
        JSONParser jsonParser = new JSONParser();
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            //Read the JSON
            Object obj = jsonParser.parse(new FileReader(FILE_LASTIDUSER));

            JSONObject jsonObject = (JSONObject) obj;

            if(jsonObject.size()== 0){
                //Introduce new id file 0
                jsonObject.put("lastIdUser", 0);

                //Write this object to the JSON file
                objectMapper.writeValue(new File(FILE_LASTIDUSER), jsonObject);
                return 0;
            }
            else
                //Get the id from the object
                return  toIntExact((Long) jsonObject.get("lastIdUser"));

        }catch(Exception e){
            e.printStackTrace();
            return -1;
        }

    }

    @Override
    public boolean checkCorrectUser(String userName, String password) {
        try {
            semaphore.acquire();
            ArrayUsers arrayUsers = new ArrayUsers();
            ObjectMapper objectMapper = new ObjectMapper();

            //Read the JSON
            arrayUsers = objectMapper.readValue(new File(FILE_USERS), ArrayUsers.class);

            //Check the password and username for each user
            for (User user: arrayUsers.usersArrayList) {
                if(user.getUserName().equals(userName) && user.getPassword().equals(password)){
                    semaphore.release();
                    return true;
                }
            }
            semaphore.release();
            return false;

        } catch (IOException e) {
            e.printStackTrace();
            semaphore.release();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            semaphore.release();
            return false;
        }
    }

    @Override
    public ArrayList<String> showFileInfo(JSONArray filesList, String idFile) throws InterruptedException {
        semaphore.acquire();
        JSONObject searchedFile = new JSONObject();
        //Check the id for each file
        for(Object f: filesList) {
            JSONObject file = (JSONObject) f;
            if(String.valueOf(file.get("id")).equals(idFile)) {
                searchedFile = file;
            }
        }
        //Add the info from the file to show it
        ArrayList<String> fileInfo = new ArrayList<>();
        fileInfo.add("The title is: " + searchedFile.get("name"));
        fileInfo.add("The topic description is: " + searchedFile.get("topicList"));
        fileInfo.add("The file name is: " + searchedFile.get("fileName"));
        semaphore.release();
        return fileInfo;
    }

    @Override
    public String deleteFileInfo(JSONArray filesList, String idFile, String currentUser) throws IOException {

        String titleToDelete = "";
        String fileNameToDelete = "";
        String stringToReturn = "";

        ObjectMapper objectMapper = new ObjectMapper();
        //Get file list
        ArrayDataObject arrayDataObj = getArrayDataObject(objectMapper);
        ArrayList<DataObject> arrayListDataObject = arrayDataObj.getArrayListDataObject();

        for (DataObject dataObject : arrayListDataObject) {
            //Check if the idFile is equals to current dataobject
            if(String.valueOf(dataObject.getId()).equals(idFile)) {
                //Check if you are the user who updated the file
                if(dataObject.getIdUser() == getIdFromUser(currentUser)) {
                    //Get the info from file
                    titleToDelete = dataObject.getName();
                    fileNameToDelete = dataObject.getFileName();
                    arrayListDataObject.remove(dataObject);

                    File file = new File("./Server/" + fileNameToDelete);

                    //Delete the file
                    if (file.delete()) {
                        stringToReturn = "The file " + fileNameToDelete + " with title " + titleToDelete + " has been deleted";
                    } else {
                        stringToReturn = titleToDelete + "does not exists";
                    }

                    break;
                } else {
                    stringToReturn = "You don't uploaded this file";
                    break;
                }
            }
        }
        //Set the new list without the deleted file
        arrayDataObj.setArrayDataObject(arrayListDataObject);

        try {
            objectMapper.writeValue(new File(FILE_INFO), arrayDataObj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        semaphore.release();
        //Delete the file
        deleteFileFromServer(fileNameToDelete);

        return stringToReturn;
    }

    private void deleteFileFromServer(String fileNameToDelete) {
        //Delete the file from Server's folder
        File file = new File("./Server/" + fileNameToDelete);
        if(file.delete()){
            System.out.println(fileNameToDelete + " deleted");
        }else System.out.println("File does not exists");
    }

    public ArrayDataObject getArrayDataObject(ObjectMapper objectMapper) throws IOException{
        //Read the JSON file and return the list
        ArrayDataObject arrayDataObject = objectMapper.readValue(new File(FILE_INFO), ArrayDataObject.class);
        return arrayDataObject;
    }

    @Override
    public String getFileName(String idFile) throws IOException, ParseException, InterruptedException {
        semaphore.acquire();
        //Get all the files
        JSONArray jsonArray = getFilesList();
        String fileName = "";
        //Get the filename which her id is the same that the id passed as parameter
        for(Object object: jsonArray) {
            JSONObject infoFile = (JSONObject) object;
            if(String.valueOf(infoFile.get("id")).equals(idFile))
                fileName = (String) infoFile.get("fileName");
        }
        semaphore.release();
        return fileName;
    }

    @Override
    public String getName(String idFile) throws IOException, ParseException, InterruptedException {
        //Get all the files
        JSONArray jsonArray = getFilesList();
        String name = "";
        //Get the title which her id is the same that the id passed as parameter
        for(Object object: jsonArray) {
            JSONObject infoFile = (JSONObject) object;
            if(String.valueOf(infoFile.get("id")).equals(idFile))
                name = (String) infoFile.get("name");
        }
        return name;
    }

    @Override
    public ArrayList<String> getTopicDescription(String idFile) throws IOException, ParseException, InterruptedException {
        //Get all the files
        JSONArray jsonArray = getFilesList();
        ArrayList<String> descriptionList = new ArrayList<>();
        //Get the topic description which her id is the same that the id passed as parameter
        for(Object object: jsonArray) {
            JSONObject infoFile = (JSONObject) object;
            if(String.valueOf(infoFile.get("id")).equals(idFile))
                descriptionList = (ArrayList<String>) infoFile.get("topicList");
        }
        return descriptionList;
    }

    public int getIdFromUser(String userName) {
        try {
            ArrayUsers arrayUsers = new ArrayUsers();
            ObjectMapper objectMapper = new ObjectMapper();

            //Read the JSON file
            arrayUsers = objectMapper.readValue(new File(FILE_USERS), ArrayUsers.class);

            //Get the id from users whose name is the same that the name passed as parameter
            for (User user: arrayUsers.usersArrayList) {
                if(user.getUserName().equals(userName)){
                    return user.getUserId();
                }
            }
            return -1;

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public String changeFileTitle(String oldTitle, String newTitle, String currentUser) {

        String stringToReturn = "";

        try{

            //Get the ArrayDataObject
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayDataObject arrayDataObj = getArrayDataObject(objectMapper);
            ArrayList<DataObject> arrayListDataObject = arrayDataObj.getArrayListDataObject();

            for (DataObject dataObject : arrayListDataObject) {
                //Check if the object has the title name that oldTitle passed as parameter
                if(String.valueOf(dataObject.getName()).equals(oldTitle)) {
                    //Check if currentUser has uploaded this file
                    if(dataObject.getIdUser() == getIdFromUser(currentUser)) {
                        dataObject.setName(newTitle);
                        stringToReturn = "The title " + newTitle + " has been modified";
                        break;
                    } else {
                        stringToReturn = "You don't uploaded this file";
                        break;
                    }
                }
            }
            //Set the arrayDataObject
            arrayDataObj.setArrayDataObject(arrayListDataObject);

            try {
                objectMapper.writeValue(new File(FILE_INFO), arrayDataObj);
            } catch (IOException e) {
                e.printStackTrace();
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringToReturn;

    }

    @Override
    public String changeFileDecription(ArrayList<String> oldDescription, ArrayList<String> newDescriptionArrayList, String currentUserName) {

        String stringToReturn = "";

        try{
            //Get the ArrayDataObject
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayDataObject arrayDataObj = getArrayDataObject(objectMapper);
            ArrayList<DataObject> arrayListDataObject = arrayDataObj.getArrayListDataObject();

            for (DataObject dataObject : arrayListDataObject) {
                //Check if the object has the description that oldDescription passed as parameter
                if(dataObject.getTopicList().equals(oldDescription)) {
                    //Check if currentUser has uploaded this file
                    if(dataObject.getIdUser() == getIdFromUser(currentUserName)) {
                        dataObject.setTopicList(newDescriptionArrayList);
                        stringToReturn = "The description " + newDescriptionArrayList + " has been modified";
                        break;
                    } else {
                        stringToReturn = "You don't uploaded this file";
                        break;
                    }
                }
            }
            //Set the arrayDataObject
            arrayDataObj.setArrayDataObject(arrayListDataObject);

            try {
                objectMapper.writeValue(new File(FILE_INFO), arrayDataObj);
            } catch (IOException e) {
                e.printStackTrace();
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringToReturn;
    }

    public void doSubcriptionCallbacks(ArrayList<String> topicList) throws RemoteException{
        try {

            ObjectMapper objectMapper = new ObjectMapper();
            //read from JSON file the array of users are registered
            ArrayUsers arrayUsers = objectMapper.readValue(new File(FILE_USERS), ArrayUsers.class);

            //for each user registered, if there ara connected and her subscriptions match
            //with the file topics, do a callback to this User
            for (User user: arrayUsers.usersArrayList) {
                ArrayList<String> matches = getTopicMatches(user.getSubscriptionList(), topicList);
                if (matches.size() != 0 && clientHash.containsKey(user.getUserId())){
                    CallbackClientInterface callbackClient = clientHash.get(user.getUserId());
                    callbackClient.notifyMe("A file with "+ matches.toString().replace("[","").replace("]","") + " topic/s has been uploaded");
                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> getTopicMatches(ArrayList<String> subsList, ArrayList<String> topicList) {
        ArrayList<String> matches = new ArrayList<>();

        //Check if the list of user's subscriptions contains the topics from the uploaded file
        for (String sub: subsList) {
            if(topicList.contains(sub)){
                matches.add(sub);
            }
        }
        return matches;
    }

    @Override
    public String addSubscription(ArrayList<String> newSubscriptionArrayList, String currentUser) {
        String stringToReturn = "No add";

        try {

            //Read the JSON file from users
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayUsers arrayUsers = objectMapper.readValue(new File(FILE_USERS), ArrayUsers.class);
            ArrayList<User> usersArrayList = arrayUsers.getUsersArrayList();


            for (User user : usersArrayList) {
                //Check if user is the same that currentUser
                if (user.getUserName().equals(currentUser)){
                    //Add the subscriptionsList to the user
                    user.addSubscriptionList(newSubscriptionArrayList);
                    stringToReturn = "Now you have this subscriptions " + user.getSubscriptionList();
                    break;
                }
            }
            arrayUsers.setArrayUsers(usersArrayList);

            objectMapper.writeValue(new File(FILE_USERS), arrayUsers);

        } catch (RemoteException e1){
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringToReturn;
    }

    @Override
    public String deleteSubscription(List<String> deleteSubscriptionList, String currentUserName) {
        String stringToReturn = "No delete";

        try {

            //Read the JSON file from users
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayUsers arrayUsers = objectMapper.readValue(new File(FILE_USERS), ArrayUsers.class);
            ArrayList<User> usersArrayList = arrayUsers.getUsersArrayList();

            for (User user : usersArrayList) {
                //Check if user is the same that currentUser
                if (user.getUserName().equals(currentUserName)){
                    //Delete the subscriptionsList to the user
                    user.deleteSubscriptionList(deleteSubscriptionList);
                    stringToReturn = "Now you have this subscriptions " + user.getSubscriptionList();
                    break;

                }

            }
            arrayUsers.setArrayUsers(usersArrayList);

            objectMapper.writeValue(new File(FILE_USERS), arrayUsers);

        } catch (RemoteException e1){
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringToReturn;
    }

    @Override
    public String getSubscriptions(String currentUserName) {
        String stringToReturn = "";

        try {
            //Read the JSON file
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayUsers arrayUsers = objectMapper.readValue(new File(FILE_USERS), ArrayUsers.class);
            ArrayList<User> usersArrayList = arrayUsers.getUsersArrayList();

            //Get the subscriptions of current user
            for (User user : usersArrayList) {
                if (user.getUserName().equals(currentUserName)){
                    stringToReturn += user.getSubscriptionList();
                    break;
                }

            }

        } catch (RemoteException e1){
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringToReturn;
    }
}// end ServerImpl class
