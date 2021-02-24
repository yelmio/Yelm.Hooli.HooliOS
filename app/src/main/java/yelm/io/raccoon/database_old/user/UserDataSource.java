package yelm.io.raccoon.database_old.user;


public class UserDataSource {


    private UserDao userDao;
    private static UserDataSource instance;

    public UserDataSource(UserDao userDao) {
        this.userDao = userDao;
    }

    public static UserDataSource getInstance(UserDao userDao) {
        if (instance == null) {
            instance = new UserDataSource(userDao);
        }
        return instance;
    }

    public void updateUser(User user) {
        userDao.updateUser(user);
    }

    public void insertToUser(User user) {
        userDao.insertToUser(user);
    }


    public User getUserById(int userId) {
        return userDao.getUserById(userId);
    }

    public int countUsers() {
        return userDao.countUsers();
    }


}
