import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.rmi.*;
import java.util.ArrayList;



public interface CallbackServerInterface extends Remote {

    public String sayHello( )
            throws java.rmi.RemoteException;

// This remote method allows an object client to
// register for callback
// @param callbackClientObject is a reference to the
//        object of the client; to be used by the server
//        to make its callbacks.

    public void registerForCallback(CallbackClientInterface callbackClientObject, String userName)
            throws java.rmi.RemoteException, InterruptedException;

// This remote method allows an object client to
// cancel its registration for callback

    public void unregisterForCallback(CallbackClientInterface callbackClientObject, String userName)
            throws java.rmi.RemoteException, InterruptedException;

    public byte[] fileToBytes(File file) throws java.rmi.RemoteException;
    public File getFileToDownload(String fileName) throws java.rmi.RemoteException, InterruptedException;

    public String upload(byte[] bytes, File fileDest, String name, ArrayList<String> topicList, int idUser)throws java.rmi.RemoteException;

    public byte[] download(String name) throws java.rmi.RemoteException, InterruptedException;

    public JSONArray getFilesWithTitles(String fileTitle) throws IOException, ParseException, InterruptedException;

    public String downloadFile(JSONObject jsonObject) throws IOException, InterruptedException;
    public String downloadFileString(String string) throws IOException, InterruptedException;

    public ArrayList<String> selectFile(JSONArray filesWithTitle) throws java.rmi.RemoteException, InterruptedException;

    public boolean checkCorrectUserName(String name) throws java.rmi.RemoteException;

    public String registerNewUser(User newUser)throws java.rmi.RemoteException;

    public boolean checkCorrectUser(String userName, String password)throws java.rmi.RemoteException;

    public ArrayList<String> showFileInfo(JSONArray filesList, String idFile) throws java.rmi.RemoteException, InterruptedException;
    public JSONArray getFilesList() throws ParseException, IOException, InterruptedException;

    public String deleteFileInfo(JSONArray filesList, String idFile) throws IOException, InterruptedException;

    public String getFileName(String idFile) throws IOException, ParseException, InterruptedException;
    public ArrayDataObject getArrayDataObject(ObjectMapper objectMapper) throws IOException, InterruptedException;

    public int getLastIdFromUsers() throws RemoteException;
    public int getIdFromUser(String userName) throws RemoteException;
}
