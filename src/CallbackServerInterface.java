import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.io.File;
import java.io.IOException;
import java.rmi.*;
import java.util.ArrayList;
import java.util.List;


public interface CallbackServerInterface extends Remote {

    public String sayHello() throws RemoteException;

    public void registerForCallback(CallbackClientInterface callbackClientObject, String userName)
            throws RemoteException, InterruptedException;



    public void unregisterForCallback(CallbackClientInterface callbackClientObject, String userName)
            throws RemoteException, InterruptedException;

    public byte[] fileToBytes(File file) throws RemoteException, InterruptedException;
    public File getFileToDownload(String fileName) throws RemoteException, InterruptedException;

    public String upload(byte[] bytes, File fileDest, String name, ArrayList<String> topicList, int idUser)
            throws RemoteException;

    public byte[] download(String name) throws RemoteException, InterruptedException;

    public JSONArray getFilesWithTitles(String fileTitle) throws IOException, ParseException, InterruptedException;

    public String downloadFile(JSONObject jsonObject) throws IOException, InterruptedException;
    public String downloadFileString(String string) throws IOException, InterruptedException;

    public ArrayList<String> selectFile(JSONArray filesWithTitle) throws RemoteException, InterruptedException;
    public ArrayList<String> selectFileWithDescription(JSONArray filesWithTitle)
            throws RemoteException, InterruptedException;

    public boolean checkCorrectUserName(String name) throws RemoteException;

    public String registerNewUser(User newUser) throws RemoteException, InterruptedException;

    public boolean checkCorrectUser(String userName, String password)throws RemoteException;

    public ArrayList<String> showFileInfo(JSONArray filesList, String idFile) throws RemoteException, InterruptedException;
    public JSONArray getFilesList() throws ParseException, IOException, InterruptedException;

    public String deleteFileInfo(JSONArray filesList, String idFile, String currentUser) throws IOException;

    public String getFileName(String idFile) throws IOException, ParseException, InterruptedException;
    public String getName(String idFile) throws IOException, ParseException, InterruptedException;
    public ArrayDataObject getArrayDataObject(ObjectMapper objectMapper) throws IOException;

    public int getLastIdFromUsers() throws RemoteException, InterruptedException;
    public int getIdFromUser(String userName) throws RemoteException;

    public String changeFileTitle(String oldTitle, String newTitle, String currentUser) throws RemoteException;

    public String changeFileDecription(ArrayList<String> oldDescription, ArrayList<String> newDescriptionArrayList, String currentUserName)
            throws  RemoteException;

    public ArrayList<String> getTopicDescription(String idFile) throws RemoteException, IOException, ParseException, InterruptedException;

    public String addSubscription(ArrayList<String> newSubscriptionArrayList, String currentUser) throws RemoteException;

    public String deleteSubscription(List<String> deleteSubscriptionList, String currentUserName) throws RemoteException;

    public String getSubscriptions(String currentUserName) throws RemoteException;
}
