package yelm.io.yelm.database_old.user;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDao {


    @Query("SELECT * FROM User WHERE id =:userId")
    User getUserById(int userId);

    @Insert
    void insertToUser(User user);

    @Update
    void updateUser(User user);

    @Query("SELECT COUNT(*) FROM User")
    int countUsers();

}
