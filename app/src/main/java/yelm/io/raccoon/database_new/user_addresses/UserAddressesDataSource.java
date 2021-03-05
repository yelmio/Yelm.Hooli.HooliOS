package yelm.io.raccoon.database_new.user_addresses;

import java.util.List;

import io.reactivex.Flowable;

public class UserAddressesDataSource {

    private AddressesDao addressesDAO;
    private static UserAddressesDataSource instance;

    public UserAddressesDataSource(AddressesDao addressesDAO) {
        this.addressesDAO = addressesDAO;
    }

    public static UserAddressesDataSource getInstance(AddressesDao addressesDAO) {
        if (instance == null) {
            instance = new UserAddressesDataSource(addressesDAO);
        }
        return instance;
    }

    public Flowable<List<UserAddress>> getUserAddresses() {
        return addressesDAO.getUserAddresses();
    }

    public List<UserAddress> getUserAddressesList() {
        return addressesDAO.getUserAddressesList();
    }

    public UserAddress getUserAddressById(int id) {
        return addressesDAO.getUserAddressById(id);
    }

    public UserAddress getUserAddressByName(String address) {
        return addressesDAO.getUserAddressByName(address);
    }

    public void deleteUserAddressById(int id) {
        addressesDAO.deleteUserAddressById(id);
    }

    public Flowable<Integer> countUserAddresses() {
        return addressesDAO.countUserAddresses();
    }

    public void insertToUserAddresses(UserAddress... userAddresses) {
        addressesDAO.insertToUserAddresses(userAddresses);
    }

    public void updateUserAddresses(UserAddress... userAddresses) {
        addressesDAO.updateUserAddresses(userAddresses);
    }

    public void deleteUserAddress(UserAddress userAddress) {
        addressesDAO.deleteUserAddress(userAddress);
    }
}