


import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class DataObject {
    private String name;
    private ArrayList<String> topicList;
    private String fileName;
    private int id;
    private int idUser;

    public DataObject() {

    }

    public  DataObject(String name, ArrayList<String> tl, String fileName, int idUser, int idFile) {
        this.name = name;
        this.topicList = tl;
        id = idFile;
        this.fileName = fileName;
        this.idUser = idUser;
    }

    public int getIdUser() {
        return idUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getTopicList() {
        return topicList;
    }

    public void setTopicList(ArrayList<String> topicList) {
        this.topicList = topicList;
    }

    public int getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    //Check if DataObjects are equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataObject)) return false;
        DataObject that = (DataObject) o;
        return getId() == that.getId() &&
                getIdUser() == that.getIdUser() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getTopicList(), that.getTopicList()) &&
                Objects.equals(getFileName(), that.getFileName());
    }

}
