


import org.json.simple.JSONObject;

import java.util.Objects;

public class DataObject {
    private String name;
    private String tag;
    private String fileName;
    private int id;
    private int idUser;


    static JSONObject jsonObject;

    public  DataObject() {
        this.name = null;
        this.tag = null;
        jsonObject = null;
        this.id = 0;
        this.fileName = null;
        this.idUser = 0;
    }

    public  DataObject(String name, String tag, String fileName, int idUser, int idFile) {
        this.name = name;
        this.tag = tag;
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

    public String getTag() {
        return tag;
    }

    public void setTag(String description) {
        this.tag = description;
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
        jsonObject.put("tag", this.tag);
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
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getTag(), that.getTag());
    }

}
