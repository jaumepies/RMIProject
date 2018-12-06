import java.util.ArrayList;

public class ArrayUsers {

    public ArrayList<User> usersArrayList;

    public ArrayUsers() {
        this.usersArrayList = new ArrayList<User>();
    }
    public void addUser(User user){
        this.usersArrayList.add(user);
    }


    public boolean exists(String name) {
        for (User user: usersArrayList) {
            if (user.getUserName().equals(name)){
                return true;
            }
        }
        return false;
    }
}
