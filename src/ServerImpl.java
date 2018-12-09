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

    static FileReader fileReader;
    static JSONArray arrayJSON;

    static final String FILE_INFO = "fileInfo.json";
    static final String COPY_FILE = "./fileInfo2.json";

    static final String FILE_USERS = "users.json";
    static final String FILE_LASTIDUSER= "lastiduser.json";
    static final String FILE_LASTIDFILE= "lastidfile.json";

    public static final Semaphore semaphore = new Semaphore(1, true);


    private Vector clientList;
    private HashMap<Integer, CallbackClientInterface> clientHash;
    final public static int BUF_SIZE = 1024 * 64;


    public ServerImpl() throws RemoteException {
        super( );
        clientList = new Vector();
        clientHash  = new HashMap<>();

        arrayJSON = new JSONArray();

    }

    public String sayHello( ) throws java.rmi.RemoteException {
        return("hello");
    }

    public synchronized void registerForCallback(CallbackClientInterface callbackClientObject, String userName)
            throws java.rmi.RemoteException, InterruptedException {
        // store the callback object into the vector
            semaphore.acquire();

            //carrega id del usuari
            Integer idUser = getIdFromUser(userName);
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
            throws java.rmi.RemoteException, InterruptedException {

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
    } // doCallbacks

    public File getFileToDownload(String fileName) {
        File file = new File("./Server/"+fileName);
        if(!file.exists()) {
            return null;
        }
        return file;
    }

    @Override
    public byte[] fileToBytes(File file) {
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
    public JSONArray getFilesWithTitles(String fileTitle) throws IOException, ParseException, InterruptedException {
            semaphore.acquire();

            JSONArray filesList = getFilesList();
            //JSONArray filesList = (JSONArray) parser.parse(new FileReader(FILE_INFO));

            JSONArray filesWithTitle = new JSONArray();

            for (Object f : filesList) {
                JSONObject file = (JSONObject) f;
                if(Pattern.compile(Pattern.quote(fileTitle), Pattern.CASE_INSENSITIVE).matcher((CharSequence)
                        file.get("fileName")).find() || Pattern.compile(Pattern.quote(fileTitle),
                        Pattern.CASE_INSENSITIVE).matcher((CharSequence) file.get("topicList")).find() ||
                        Pattern.compile(Pattern.quote(fileTitle), Pattern.CASE_INSENSITIVE).matcher((CharSequence)
                                file.get("name")).find()){
                    //System.out.println("Found title");
                    filesWithTitle.add(file);
                } else {
                    //System.out.println("Title not found");
                }
            }
            semaphore.release();
            return filesWithTitle;
    }

    public JSONArray getFilesList() throws ParseException, IOException, InterruptedException {
            semaphore.acquire();

            ObjectMapper mapper = new ObjectMapper();
            JSONParser parser = new JSONParser();
            String stringJSON = mapper.writeValueAsString(parser.parse(new FileReader(FILE_INFO)));

            JSONObject json = (JSONObject) parser.parse(stringJSON);

            semaphore.release();
            return (JSONArray) json.get("arrayListDataObject");

    }

    @Override
    public String downloadFile(JSONObject jsonObject) throws IOException, InterruptedException {
        String fileNameDwn = jsonObject.get("fileName").toString();
        return downloadFileString(fileNameDwn);

    }

    public String downloadFileString(String fileNameDwn) throws IOException, InterruptedException {
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
        }
        return "File: " + fileDestDwn.getName() + " downloaded correctly.";
    }

    @Override
    public ArrayList<String> selectFile(JSONArray filesWithTitle) throws InterruptedException {
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
    public boolean checkCorrectUserName(String name) {
        try {
            semaphore.acquire();
            ArrayUsers arrayUsers = new ArrayUsers();
            ObjectMapper objectMapper = new ObjectMapper();

            //objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            arrayUsers = objectMapper.readValue(new File(FILE_USERS), ArrayUsers.class);

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
            //objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            arrayUsers = objectMapper.readValue(new File(FILE_USERS), ArrayUsers.class);

            arrayUsers.addUser(newUser);

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

    private void updateLastIdUser(int newId) throws InterruptedException {
        semaphore.acquire();
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
        semaphore.release();
    }

    private void updateLastIdFile(int newId) throws InterruptedException {
        semaphore.acquire();
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
        semaphore.release();
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
            semaphore.acquire();
            ArrayUsers arrayUsers = new ArrayUsers();
            ObjectMapper objectMapper = new ObjectMapper();

            //objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            arrayUsers = objectMapper.readValue(new File(FILE_USERS), ArrayUsers.class);

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
        semaphore.release();
        return fileInfo;
    }

    @Override
    public String deleteFileInfo(JSONArray filesList, String idFile) throws IOException, InterruptedException {
        semaphore.acquire();
        String titleToDelete = "";

        ObjectMapper objectMapper = new ObjectMapper();
        ArrayDataObject arrayDataObj = getArrayDataObject(objectMapper);
        ArrayList<DataObject> arrayListDataObject = arrayDataObj.getArrayListDataObject();

        for (DataObject dataObject : arrayListDataObject) {
            if(String.valueOf(dataObject.getId()).equals(idFile)) {
                titleToDelete = dataObject.getName();
                arrayListDataObject.remove(dataObject);
                break;

            }
        }
        arrayDataObj.setArrayDataObject(arrayListDataObject);

        //objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            objectMapper.writeValue(new File(FILE_INFO), arrayDataObj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        semaphore.release();
        return "The title " + titleToDelete + "has been deleted";
    }

    public ArrayDataObject getArrayDataObject(ObjectMapper objectMapper) throws IOException{
        ArrayDataObject arrayDataObject = objectMapper.readValue(new File(FILE_INFO), ArrayDataObject.class);
        return arrayDataObject;
    }

    @Override
    public String getFileName(String idFile) throws IOException, ParseException, InterruptedException {
        semaphore.acquire();
        JSONArray jsonArray = getFilesList();
        String fileName = "";
        for(Object object: jsonArray) {
            JSONObject infoFile = (JSONObject) object;
            if(String.valueOf(infoFile.get("id")).equals(idFile))
                fileName = (String) infoFile.get("fileName");
        }
        semaphore.release();
        return fileName;
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



    public void doSubcriptionCallbacks(ArrayList<String> topicList) throws RemoteException{
        try {
            ArrayUsers arrayUsers = new ArrayUsers();
            ObjectMapper objectMapper = new ObjectMapper();

            //objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            arrayUsers = objectMapper.readValue(new File(FILE_USERS), ArrayUsers.class);

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

        for (String sub: subsList) {
            if(topicList.contains(sub)){
                matches.add(sub);
            }
        }
        return matches;
    }
}// end ServerImpl class
