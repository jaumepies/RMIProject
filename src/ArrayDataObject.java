import java.util.ArrayList;

public class ArrayDataObject {
    private ArrayList<DataObject> dataObjectArraylist;

    public ArrayDataObject() {

        dataObjectArraylist = new ArrayList<DataObject>();
    }
    public void addDataObject(DataObject dataObject) {
        dataObjectArraylist.add(dataObject);
    }
}
