import java.util.ArrayList;

public class ArrayDataObject {
    private ArrayList<DataObject> dataObjectArraylist;

    public ArrayDataObject() {

        this.dataObjectArraylist = new ArrayList<DataObject>();
    }
    public void addDataObject(DataObject dataObject) {
        this.dataObjectArraylist.add(dataObject);
    }

    public ArrayList<DataObject> getArrayListDataObject() {
        return this.dataObjectArraylist;
    }

    public void setArrayDataObject(ArrayList<DataObject> arrayListDataObject) {
        this.dataObjectArraylist = arrayListDataObject;
    }
}
