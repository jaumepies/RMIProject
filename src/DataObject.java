import com.fasterxml.jackson.databind.util.JSONPObject;

import java.util.Objects;
import javax.json.*;

public class DataObject {
    private String name;
    private String description;
    private String type;
    private int id;


    JSONPObject jsonObject;

    public  DataObject() {
        this.name = null;
        this.description = null;
        this.type = null;
    }

    public  DataObject(String name, String description, String type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void createID(){
        this.id = hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataObject)) return false;
        DataObject that = (DataObject) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getName(), getDescription(), getType());
    }
}
