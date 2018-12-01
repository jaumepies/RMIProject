


import org.json.simple.JSONObject;

import java.util.Objects;

public class DataObject {
    private String name;
    private String tag;
    private String fileName;
    private int id;
    private static int nextId = 0;


    JSONObject jsonObject;

    public  DataObject() {
        this.name = null;
        this.tag = null;
        jsonObject = null;
        this.id = 0;
        this.fileName = null;
    }

    public  DataObject(String name, String tag, String fileName) {
        this.name = name;
        this.tag = tag;
        jsonObject = new JSONObject();
        id = nextId;
        nextId++;
        this.fileName = fileName;
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
        jsonObject.put("Name",this.name);
        jsonObject.put("Tag",this.tag);
        jsonObject.put("Id", this.id);
        jsonObject.put("FileName", this.fileName);
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
