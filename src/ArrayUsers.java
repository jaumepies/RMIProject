import java.util.ArrayList;

public class ArrayUsers {

    public ArrayList<User> usersArrayList;

    public ArrayUsers() {
        this.usersArrayList = new ArrayList<User>();
    }

    //Add new User
    public void addUser(User user){
        this.usersArrayList.add(user);
    }

    //Get the ArrayUsers
    public ArrayList<User> getUsersArrayList() {
        return this.usersArrayList;
    }

    //Set the ArrayUsers
    public void setArrayUsers(ArrayList<User> usersArrayList) {
        this.usersArrayList = usersArrayList;
    }

    //Check if Username exists in ArrayUsers
    public boolean exists(String name) {
        for (User user: usersArrayList) {
            if (user.getUserName().equals(name)){
                return true;
            }
        }
        return false;
    }


}
