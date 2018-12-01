

import org.json.JSONObject;

import java.util.Objects;

public class DataObject {
    private String name;
    private String tag;
    private int id;
    private static int nextId = 0;


    JSONObject jsonObject;

    public  DataObject() {
        this.name = null;
        this.tag = null;
        jsonObject = null;
        this.id = 0;
    }

    public  DataObject(String name, String tag) {
        this.name = name;
        this.tag = tag;
        jsonObject = new JSONObject();
        id = nextId;
        nextId++;
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

    public JSONObject createJSONObject(){
        jsonObject.put("Name",this.name);
        jsonObject.put("Tag",this.tag);
        jsonObject.put("Id", this.id);
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
