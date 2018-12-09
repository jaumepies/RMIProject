import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class User implements java.io.Serializable {
    private String userName;
    private String password;
    private int userId;
    private ArrayList<String> subscriptionList;

    public User(){}

    public User(String userName, String password, int userId) {
        this.userName = userName;
        this.password = password;
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public ArrayList<String> getSubscriptionList() {
        return subscriptionList;
    }

    public void setSubscriptionList(ArrayList<String> subscriptionList) {
        this.subscriptionList = subscriptionList;
    }

    public boolean possibleSubscription(String sub){
        if (this.subscriptionList.contains(sub)){
            return true;
        }else
            return false;
    }

    public void addSubscription(String sub){
        this.subscriptionList.add(sub);
    }

    public void addSubscriptionList(ArrayList<String> newSubscriptionArrayList) {
        for (String newSubs : newSubscriptionArrayList){
            this.subscriptionList.add(newSubs);

        }
    }


    public void deleteSubscriptionList(List<String> deleteSubscriptionList) {
        for (String deleteSubs : deleteSubscriptionList) {
            if((this.subscriptionList).contains(deleteSubs)){
                this.subscriptionList.remove(deleteSubs);
            }
        }
    }
}
