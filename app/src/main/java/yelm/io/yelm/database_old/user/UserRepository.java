package yelm.io.yelm.database_old.user;


public class UserRepository {

    private static UserRepository instance;
    private UserDataSource userDataSource;


    public UserRepository(UserDataSource userDataSource) {
        this.userDataSource = userDataSource;
    }

    public static UserRepository getInstance(UserDataSource userDataSource) {
        if (instance == null) {
            instance = new UserRepository(userDataSource);
        }
        return instance;
    }

    public void insertToUser(User user) {
        userDataSource.insertToUser(user);
    }


    public void updateUser(User user) {
        userDataSource.updateUser(user);
    }


    public User getUserById(int userId) {
        return userDataSource.getUserById(userId);
    }

    public int countUsers() {
        return userDataSource.countUsers();
    }


}
