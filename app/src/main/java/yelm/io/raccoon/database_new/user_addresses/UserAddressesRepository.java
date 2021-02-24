package yelm.io.raccoon.database_new.user_addresses;

import java.util.List;

import io.reactivex.Flowable;

public class UserAddressesRepository {

    private static UserAddressesRepository instance;
    private UserAddressesDataSource userAddressesDataSource;

    public UserAddressesRepository(UserAddressesDataSource userAddressesDataSource) {
        this.userAddressesDataSource = userAddressesDataSource;
    }

    public static UserAddressesRepository getInstance(UserAddressesDataSource userAddressesDataSource) {
        if (instance == null) {
            instance = new UserAddressesRepository(userAddressesDataSource);
        }
        return instance;
    }

    public Flowable<List<UserAddress>> getUserAddresses() {
        return userAddressesDataSource.getUserAddresses();
    }

    public List<UserAddress> getUserAddressesList() {
        return userAddressesDataSource.getUserAddressesList();
    }


    public UserAddress getUserAddressById(int id) {
        return userAddressesDataSource.getUserAddressById(id);
    }

    public UserAddress getUserAddressByName(String address) {
        return userAddressesDataSource.getUserAddressByName(address);
    }

    public void deleteUserAddressById(int id) {
        userAddressesDataSource.deleteUserAddressById(id);
    }

    public Flowable<Integer> countUserAddresses() {
        return userAddressesDataSource.countUserAddresses();
    }

    public void insertToUserAddresses(UserAddress... userAddresses) {
        userAddressesDataSource.insertToUserAddresses(userAddresses);
    }

    public void updateUserAddresses(UserAddress... userAddresses) {
        userAddressesDataSource.updateUserAddresses(userAddresses);
    }

    public void deleteUserAddress(UserAddress userAddress) {
        userAddressesDataSource.deleteUserAddress(userAddress);
    }




}
