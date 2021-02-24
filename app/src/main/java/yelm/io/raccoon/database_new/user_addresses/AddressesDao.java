package yelm.io.raccoon.database_new.user_addresses;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface AddressesDao {

    @Query("SELECT * FROM UserAddresses")
    Flowable<List<UserAddress>> getUserAddresses();

    @Query("SELECT * FROM UserAddresses")
    List<UserAddress> getUserAddressesList();

    @Query("SELECT * FROM UserAddresses WHERE id =:id")
    UserAddress getUserAddressById(int id);

    @Query("SELECT * FROM UserAddresses WHERE address =:address")
    UserAddress getUserAddressByName(String address);

    @Query("Delete FROM UserAddresses WHERE id =:id")
    void deleteUserAddressById(int id);

    @Query("SELECT COUNT(*) FROM UserAddresses")
    Flowable<Integer> countUserAddresses();

    @Insert
    void insertToUserAddresses (UserAddress...userAddresses);

    @Update
    void updateUserAddresses (UserAddress...userAddresses);

    @Delete
    void deleteUserAddress (UserAddress userAddress);

}
