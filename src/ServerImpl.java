import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.regexp.internal.RE;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Pattern;


public class ServerImpl extends UnicastRemoteObject
        implements CallbackServerInterface {

    static FileReader fileReader;
    static JSONArray arrayJSON;

    static final String FILE_INFO = "fileInfo.json";
    static final String COPY_FILE = "./fileInfo2.json";

    static final String FILE_USERS = "users.json";



    private Vector clientList;
    final public static int BUF_SIZE = 1024 * 64;


    public ServerImpl() throws RemoteException {
        super( );
        clientList = new Vector();

        arrayJSON = new JSONArray();

    }

    public String sayHello( )
            throws java.rmi.RemoteException {
        return("hello");
    }

    public synchronized void registerForCallback(CallbackClientInterface callbackClientObject) throws java.rmi.RemoteException{
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
            DataObject fileInfo = new DataObject(name, tag, fileDest.getName());
            fileOuputStream.write(bytes);
            fileOuputStream.close();

            ArrayDataObject arrayDataObject = new ArrayDataObject();
            ObjectMapper objectMapper = new ObjectMapper();

            //objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            arrayDataObject = objectMapper.readValue(new File(FILE_INFO), ArrayDataObject.class);

            arrayDataObject.addDataObject(fileInfo);

            objectMapper.writeValue(new File(FILE_INFO), arrayDataObject);

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
                    Pattern.CASE_INSENSITIVE).matcher((CharSequence) file.get("tag")).find() ||
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

        File fileDestDwn = new File("./sharedData/"+fileNameDwn);

        while(fileDestDwn.exists()) {
            copyName += "1";
            fileDestDwn = new File("./sharedData/"+copyName);
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
        fileInfo.add("The topic description is: " + searchedFile.get("tag"));
        fileInfo.add("The file name is: " + searchedFile.get("fileName"));

        return fileInfo;
    }

    @Override
    public String deleteFileInfo(JSONArray filesList, String idFile) throws IOException {

        String titleToDelete = "";
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayDataObject arrayDataObj = getArrayDataObject(objectMapper);
        ArrayList<DataObject> arrayListDataObject = arrayDataObj.getArrayListDataObject();
        for (DataObject dataObject : arrayListDataObject) {
            if(String.valueOf(dataObject.getId()).equals(idFile)) {
                arrayListDataObject.remove(dataObject);
                titleToDelete = dataObject.getName();
            }
        }

        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            objectMapper.writeValue(new File(FILE_INFO), arrayListDataObject);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "The title " + titleToDelete + "has been deleted";
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
}// end ServerImpl class
