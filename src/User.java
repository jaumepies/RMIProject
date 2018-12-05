import java.util.ArrayList;

public class User {
    private String userName;
    private String password;
    private int userId;
    private ArrayList<String> subscriptionList;

    private static int nextId = 0;

    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.userId = nextId;
        nextId++;
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
}
