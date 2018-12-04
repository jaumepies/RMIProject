import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.*;
import java.util.ArrayList;
import java.util.BitSet;

/**
 * This is a remote interface for illustrating RMI
 * client callback.
 * @author M. L. Liu
 */

public interface CallbackServerInterface extends Remote {

    public String sayHello( )
            throws java.rmi.RemoteException;

// This remote method allows an object client to
// register for callback
// @param callbackClientObject is a reference to the
//        object of the client; to be used by the server
//        to make its callbacks.

    public void registerForCallback(
            CallbackClientInterface callbackClientObject
    ) throws java.rmi.RemoteException;

// This remote method allows an object client to
// cancel its registration for callback

    public void unregisterForCallback(
            CallbackClientInterface callbackClientObject)
            throws java.rmi.RemoteException;

    public byte[] fileToBytes(File file) throws java.rmi.RemoteException;
    public File getFileToDownload(String fileName) throws java.rmi.RemoteException;

    public String upload(byte[] bytes, File fileDest, String name, String tag)throws java.rmi.RemoteException;

    public byte[] download(String name)throws java.rmi.RemoteException;

    public JSONArray getFilesWithTitles(String fileTitle) throws IOException, ParseException;

    public String downloadFile(JSONObject jsonObject) throws IOException;
    public String downloadFileString(String string) throws IOException;

    public ArrayList<String> selectFile(JSONArray filesWithTitle)throws java.rmi.RemoteException;
}
