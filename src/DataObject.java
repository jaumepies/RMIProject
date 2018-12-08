


import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class DataObject {
    private String name;
    private ArrayList<String> topicList;
    private String fileName;
    private int id;
    private int idUser;


    static JSONObject jsonObject;

    public  DataObject() {
        this.name = null;
        this.topicList = null;
        jsonObject = null;
        this.id = 0;
        this.fileName = null;
        this.idUser = 0;
    }

    public  DataObject(String name, ArrayList<String> tl, String fileName, int idUser, int idFile) {
        this.name = name;
        this.topicList = tl;
        jsonObject = new JSONObject();
        id = idFile;
        this.fileName = fileName;
        this.idUser = idUser;
    }



    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
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

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public JSONObject createJSONObject(){
        jsonObject.put("name", this.name);
        jsonObject.put("topicList", this.topicList);
        jsonObject.put("id", this.id);
        jsonObject.put("fileName", this.fileName);
        jsonObject.put("idUser", this.idUser);
        return jsonObject;
    }


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
