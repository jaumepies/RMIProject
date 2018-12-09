import java.util.ArrayList;
import java.util.List;

public class User implements java.io.Serializable {

    private String userName;
    private String password;
    private int userId;
    private ArrayList<String> subscriptionList;

    public User() {

    }

    public User(String userName, String password, int userId) {
        this.userName = userName;
        this.password = password;
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public int getUserId() {
        return userId;
    }

    public ArrayList<String> getSubscriptionList() {
        return subscriptionList;
    }

    public void setSubscriptionList(ArrayList<String> subscriptionList) {
        this.subscriptionList = subscriptionList;
    }

    //Add the subscriptions that you have introduced
    public void addSubscriptionList(ArrayList<String> newSubscriptionArrayList) {
        for (String newSubs : newSubscriptionArrayList){
            this.subscriptionList.add(newSubs);
        }
    }

    //Delete the subscriptions that you have introduced
    public void deleteSubscriptionList(List<String> deleteSubscriptionList) {
        for (String deleteSubs : deleteSubscriptionList) {
            if((this.subscriptionList).contains(deleteSubs)){
                this.subscriptionList.remove(deleteSubs);
            }
        }
    }
}
