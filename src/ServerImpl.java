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
import java.util.regex.Pattern;
import static java.lang.Math.toIntExact;

public class ServerImpl extends UnicastRemoteObject implements CallbackServerInterface {

    static FileReader fileReader;
    static JSONArray arrayJSON;

    static final String FILE_INFO = "fileInfo.json";
    static final String COPY_FILE = "./fileInfo2.json";

    static final String FILE_USERS = "users.json";
    static final String FILE_LASTIDUSER= "lastiduser.json";
    static final String FILE_LASTIDFILE= "lastidfile.json";



    private Vector clientList;
    private HashMap<Integer, CallbackClientInterface> clientHash;
    final public static int BUF_SIZE = 1024 * 64;


    public ServerImpl() throws RemoteException {
        super( );
        clientList = new Vector();
        clientHash  = new HashMap<Integer, CallbackClientInterface>();

        arrayJSON = new JSONArray();

    }

    public String sayHello( ) throws java.rmi.RemoteException {
        return("hello");
    }

    public synchronized void registerForCallback(CallbackClientInterface callbackClientObject, String userName) throws java.rmi.RemoteException{
        // store the callback object into the vector
        //carrega id del usuari
        Integer idUser = getIdFromUser(userName);
        if(!clientHash.containsKey(idUser)){
            clientHash.put(idUser, callbackClientObject);
            System.out.println("New client logged ");
            doCallbacks();
        }
        /*if (!(clientList.contains(callbackClientObject))) {
            clientList.addElement(callbackClientObject);
            System.out.println("Registered new client ");
            doCallbacks();
        }*/ // end if
    }

    // This remote method allows an object client to
// cancel its registration for callback
// @param id is an ID for the client; to be used by
// the server to uniquely identify the registered client.
    public synchronized void unregisterForCallback(CallbackClientInterface callbackClientObject, String userName)
            throws java.rmi.RemoteException{
        Integer idUser = getIdFromUser(userName);
        if (clientHash.remove(idUser, callbackClientObject)) {
            System.out.println("Client "+ userName +" logged out");
        }
        else {
            System.out.println(
                    "client wasn't logged.");
        }
        /*if (clientList.removeElement(callbackClientObject)) {
            System.out.println("Unregistered client ");
        } else {
            System.out.println(
                    "unregister: client wasn't registered.");
        }*/
    }

    private synchronized void doCallbacks( ) throws java.rmi.RemoteException{
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
        /*for (int i = 0; i < clientList.size(); i++){
            System.out.println("doing "+ i +"-th callback\n");
            // convert the vector object to a callback object
            CallbackClientInterface nextClient = (CallbackClientInterface)clientList.elementAt(i);
            // invoke the callback method
            nextClient.notifyMe("Number of registered clients=" +  clientList.size());
        }*/// end for

    } // doCallbacks

    public File getFileToDownload(String fileName){

        File file = new File("./Server/"+fileName);
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
    public String upload(byte[] bytes, File fileDest, String name, ArrayList<String> topicList, int idUser) {

        try {
            FileOutputStream fileOuputStream = new FileOutputStream(fileDest);
            int idFile = getLastIdFromFile();
            updateLastIdFile(idFile);

            DataObject fileInfo = new DataObject(name, topicList, fileDest.getName(), idUser, idFile);
            fileOuputStream.write(bytes);
            fileOuputStream.close();

            ArrayDataObject arrayDataObject = new ArrayDataObject();
            ObjectMapper objectMapper = new ObjectMapper();

            //objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            arrayDataObject = objectMapper.readValue(new File(FILE_INFO), ArrayDataObject.class);

            arrayDataObject.addDataObject(fileInfo);

            objectMapper.writeValue(new File(FILE_INFO), arrayDataObject);

            //Callbacks
            doSubcriptionCallbacks(topicList);

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

    @Override
    public JSONArray getFilesWithTitles(String fileTitle) throws IOException, ParseException {


        JSONArray filesList = getFilesList();
        //JSONArray filesList = (JSONArray) parser.parse(new FileReader(FILE_INFO));

        JSONArray filesWithTitle = new JSONArray();

        for (Object f : filesList) {
            JSONObject file = (JSONObject) f;
            if(Pattern.compile(Pattern.quote(fileTitle), Pattern.CASE_INSENSITIVE).matcher((CharSequence)
                    file.get("fileName")).find() || Pattern.compile(Pattern.quote(fileTitle),
                    Pattern.CASE_INSENSITIVE).matcher(String.valueOf( file.get("topicList"))).find() ||
                    Pattern.compile(Pattern.quote(fileTitle), Pattern.CASE_INSENSITIVE).matcher((CharSequence)
                            file.get("name")).find()){
                //System.out.println("Found title");
                filesWithTitle.add(file);
            } else {
                //System.out.println("Title not found");
            }
        }
        return filesWithTitle;
    }

    public JSONArray getFilesList() throws ParseException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        JSONParser parser = new JSONParser();
        String stringJSON = mapper.writeValueAsString(parser.parse(new FileReader(FILE_INFO)));


        JSONObject json = (JSONObject) parser.parse(stringJSON);
        return (JSONArray) json.get("arrayListDataObject");

    }

    @Override
    public String downloadFile(JSONObject jsonObject) throws IOException {
        String fileNameDwn = jsonObject.get("fileName").toString();
        return downloadFileString(fileNameDwn);

    }

    public String downloadFileString(String fileNameDwn) throws IOException {
        String copyName = fileNameDwn;

        File fileDestDwn = new File("./Client/"+fileNameDwn);

        while(fileDestDwn.exists()) {
            copyName += "1";
            fileDestDwn = new File("./Client/"+copyName);
        }
        if(copyName != fileNameDwn) {
            System.out.println("The file already exists and it has been modified to "+ copyName);
        }

        byte[] downfileBytes = download(fileNameDwn);

        if(downfileBytes == null) {
            System.out.println("The file does not exists");
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
        }
        return "File: " + fileDestDwn.getName() + " downloaded correctly.";
    }

    @Override
    public ArrayList<String> selectFile(JSONArray filesWithTitle) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < filesWithTitle.size(); i++) {
            Object f = filesWithTitle.get(i);
            JSONObject file = (JSONObject) f;
            arrayList.add(file.get("name") + "[" + file.get("id") +"] ");
        }
        return arrayList;
    }

    @Override
    public boolean checkCorrectUserName(String name) {
        try {
            ArrayUsers arrayUsers = new ArrayUsers();
            ObjectMapper objectMapper = new ObjectMapper();

            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            arrayUsers = objectMapper.readValue(new File(FILE_USERS), ArrayUsers.class);

            if (arrayUsers.exists(name)){
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public String registerNewUser(User newUser) throws RemoteException {
        ArrayUsers arrayUsers = new ArrayUsers();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            arrayUsers = objectMapper.readValue(new File(FILE_USERS), ArrayUsers.class);

            arrayUsers.addUser(newUser);

            objectMapper.writeValue(new File(FILE_USERS), arrayUsers);


            //Store the last id.
            updateLastIdUser(newUser.getUserId());

            System.out.printf("User: "+ newUser.getUserName() + " was registered correctly.");
            return "User: "+ newUser.getUserName() + " was registered correctly.";
        } catch (RemoteException e1){
            e1.printStackTrace();
            return "Error, registering the new user!!!";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error, registering the new user!!!";
        }
    }

    private void updateLastIdUser(int newId) {
        JSONParser jsonParser = new JSONParser();
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            Object obj = jsonParser.parse(new FileReader(FILE_LASTIDUSER));

            JSONObject jsonObject = (JSONObject) obj;

            int lastId = toIntExact((Long) jsonObject.get("lastIdUser"));
            if (lastId != newId){
                jsonObject.put("lastIdUser", newId);

                objectMapper.writeValue(new File(FILE_LASTIDUSER), jsonObject);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void updateLastIdFile(int newId) {
        JSONParser jsonParser = new JSONParser();
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            Object obj = jsonParser.parse(new FileReader(FILE_LASTIDFILE));

            JSONObject jsonObject = (JSONObject) obj;

            int lastId = toIntExact((Long) jsonObject.get("lastIdFile"));
            if (lastId != newId){
                jsonObject.put("lastIdFile", newId);

                objectMapper.writeValue(new File(FILE_LASTIDFILE), jsonObject);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public int getLastIdFromFile() throws RemoteException{
        JSONParser jsonParser = new JSONParser();
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            Object obj = jsonParser.parse(new FileReader(FILE_LASTIDFILE));

            JSONObject jsonObject = (JSONObject) obj;
            if(jsonObject.size() == 0) {
                jsonObject.put("lastIdFile", 1);
                objectMapper.writeValue(new File(FILE_LASTIDFILE), jsonObject);
                return 1;
            }
            else{
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
            Object obj = jsonParser.parse(new FileReader(FILE_LASTIDUSER));

            JSONObject jsonObject = (JSONObject) obj;

            if(jsonObject.size()== 0){
                jsonObject.put("lastIdUser", 0);

                objectMapper.writeValue(new File(FILE_LASTIDUSER), jsonObject);
                return 0;
            }
            else
                return  toIntExact((Long) jsonObject.get("lastIdUser"));

        }catch(Exception e){
            e.printStackTrace();
            return -1;
        }

    }

    @Override
    public boolean checkCorrectUser(String userName, String password) {
        try {
            ArrayUsers arrayUsers = new ArrayUsers();
            ObjectMapper objectMapper = new ObjectMapper();

            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            arrayUsers = objectMapper.readValue(new File(FILE_USERS), ArrayUsers.class);

            for (User user: arrayUsers.usersArrayList) {
                if(user.getUserName().equals(userName) && user.getPassword().equals(password)){
                    return true;
                }
            }
            return false;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public ArrayList<String> showFileInfo(JSONArray filesList, String idFile) {
        JSONObject searchedFile = new JSONObject();
        for(Object f: filesList) {
            JSONObject file = (JSONObject) f;
            if(String.valueOf(file.get("id")).equals(idFile)) {
                searchedFile = file;
            }
        }
        ArrayList<String> fileInfo = new ArrayList<>();
        fileInfo.add("The title is: " + searchedFile.get("name"));
        fileInfo.add("The topic description is: " + searchedFile.get("topicList"));
        fileInfo.add("The file name is: " + searchedFile.get("fileName"));

        return fileInfo;
    }

    @Override
    public String deleteFileInfo(JSONArray filesList, String idFile, String currentUser) throws IOException {

        String titleToDelete = "";
        String fileNameToDelete = "";
        String stringToReturn = "";

        ObjectMapper objectMapper = new ObjectMapper();
        ArrayDataObject arrayDataObj = getArrayDataObject(objectMapper);
        ArrayList<DataObject> arrayListDataObject = arrayDataObj.getArrayListDataObject();

        for (DataObject dataObject : arrayListDataObject) {
            if(String.valueOf(dataObject.getId()).equals(idFile)) {
                if(dataObject.getIdUser() == getIdFromUser(currentUser)) {
                    titleToDelete = dataObject.getName();
                    fileNameToDelete = dataObject.getFileName();
                    arrayListDataObject.remove(dataObject);

                    File file = new File("./receivedData/" + fileNameToDelete);

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
        arrayDataObj.setArrayDataObject(arrayListDataObject);

        //objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            objectMapper.writeValue(new File(FILE_INFO), arrayDataObj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        deleteFileFromServer(fileNameToDelete);

        return stringToReturn;
    }

    private void deleteFileFromServer(String fileNameToDelete) {
        File file = new File("./receivedData/" + fileNameToDelete);
        if(file.delete()){
            System.out.println(fileNameToDelete + " deleted");
        }else System.out.println("File does not exists");
    }

    public ArrayDataObject getArrayDataObject(ObjectMapper objectMapper) throws IOException {

        ArrayDataObject arrayDataObject = objectMapper.readValue(new File(FILE_INFO), ArrayDataObject.class);
        return arrayDataObject;
    }

    @Override
    public String getFileName(String idFile) throws IOException, ParseException {
        JSONArray jsonArray = getFilesList();
        String fileName = "";
        for(Object object: jsonArray) {
            JSONObject infoFile = (JSONObject) object;
            if(String.valueOf(infoFile.get("id")).equals(idFile))
                fileName = (String) infoFile.get("fileName");
        }
        return fileName;
    }

    @Override
    public String getName(String idFile) throws IOException, ParseException {
        JSONArray jsonArray = getFilesList();
        String name = "";
        for(Object object: jsonArray) {
            JSONObject infoFile = (JSONObject) object;
            if(String.valueOf(infoFile.get("id")).equals(idFile))
                name = (String) infoFile.get("name");
        }
        return name;
    }

    @Override
    public ArrayList<String> getTopicDescription(String idFile) throws IOException, ParseException {
        JSONArray jsonArray = getFilesList();
        ArrayList<String> descriptionList = new ArrayList<>();
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

            //objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            arrayUsers = objectMapper.readValue(new File(FILE_USERS), ArrayUsers.class);

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

    public String getUserFromId(int idUser) {
        try {
            ArrayUsers arrayUsers = new ArrayUsers();
            ObjectMapper objectMapper = new ObjectMapper();

            //objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            arrayUsers = objectMapper.readValue(new File(FILE_USERS), ArrayUsers.class);

            for (User user: arrayUsers.usersArrayList) {
                if(user.getUserId() == idUser){
                    return user.getUserName();
                }
            }
            return "";

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public String changeFileTitle(String oldTitle, String newTitle, String currentUser) {


        String stringToReturn = "";

        try{
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayDataObject arrayDataObj = getArrayDataObject(objectMapper);
            ArrayList<DataObject> arrayListDataObject = arrayDataObj.getArrayListDataObject();

            for (DataObject dataObject : arrayListDataObject) {
                if(String.valueOf(dataObject.getName()).equals(oldTitle)) {
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
            arrayDataObj.setArrayDataObject(arrayListDataObject);

            //objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
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
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayDataObject arrayDataObj = getArrayDataObject(objectMapper);
            ArrayList<DataObject> arrayListDataObject = arrayDataObj.getArrayListDataObject();

            for (DataObject dataObject : arrayListDataObject) {
                if(dataObject.getTopicList().equals(oldDescription)) {
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
            arrayDataObj.setArrayDataObject(arrayListDataObject);

            //objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
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
            ArrayUsers arrayUsers = new ArrayUsers();
            ObjectMapper objectMapper = new ObjectMapper();

            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            arrayUsers = objectMapper.readValue(new File(FILE_USERS), ArrayUsers.class);

            for (User user: arrayUsers.usersArrayList) {
                ArrayList<String> matches = getTopicMatches(user.getSubscriptionList(), topicList);
                if (matches != null){
                    CallbackClientInterface callbackClient = clientHash.get(user.getUserId());
                    callbackClient.notifyMe("A file with "+ matches.toString() + " topics has been uploaded");
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

        for (String sub: subsList) {
            if(topicList.contains(sub)){
                matches.add(sub);
            }
        }
        return matches;
    }
}// end ServerImpl class
