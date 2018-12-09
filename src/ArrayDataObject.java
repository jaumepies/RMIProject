import java.util.ArrayList;

public class ArrayDataObject {
    private ArrayList<DataObject> dataObjectArraylist;

    public ArrayDataObject() {

        this.dataObjectArraylist = new ArrayList<DataObject>();
    }
    //Add new DataObject
    public void addDataObject(DataObject dataObject) {
        this.dataObjectArraylist.add(dataObject);
    }

    //Get the ArrayDataObject
    public ArrayList<DataObject> getArrayListDataObject() {
        return this.dataObjectArraylist;
    }

    //Set the ArrayDataObject
    public void setArrayDataObject(ArrayList<DataObject> arrayListDataObject) {
        this.dataObjectArraylist = arrayListDataObject;
    }
}
