package yelm.io.raccoon.database_new.basket_new;

import java.util.List;

import io.reactivex.Flowable;


public class BasketCartDataSource {

    private BasketCartDao basketCartDao;
    private static BasketCartDataSource instance;

    public BasketCartDataSource(BasketCartDao basketCartDao) {
        this.basketCartDao = basketCartDao;
    }

    public static BasketCartDataSource getInstance(BasketCartDao basketCartDao) {
        if (instance == null) {
            instance = new BasketCartDataSource(basketCartDao);
        }
        return instance;
    }

    public Flowable<List<BasketCart>> getBasketCarts() {
        return basketCartDao.getBasketCarts();
    }

    public List<BasketCart> getBasketCartsList() {
        return basketCartDao.getBasketCartsList();
    }


    public List<BasketCart> getListBasketCartByItemID(String basketCartItemId) {
        return basketCartDao.getListBasketCartByItemID(basketCartItemId);
    }


    public BasketCart getBasketCartById(String basketCartItemId) {
        return basketCartDao.getBasketCartById(basketCartItemId);
    }

    public String getModifier(String basketCartItemId) {
        return basketCartDao.getModifier(basketCartItemId);
    }

    public void emptyBasketCart() {
        basketCartDao.emptyBasketCart();
    }

    public void insertToBasketCart(BasketCart... basketCarts) {
        basketCartDao.insertToBasketCart(basketCarts);
    }

    public int countBasketCarts() {
        return basketCartDao.countBasketCarts();
    }

    public void updateBasketCart(BasketCart... basketCarts) {
        basketCartDao.updateBasketCart(basketCarts);
    }

    public void deleteBasketCart(BasketCart basketCarts) {
        basketCartDao.deleteBasketCart(basketCarts);
    }
}
