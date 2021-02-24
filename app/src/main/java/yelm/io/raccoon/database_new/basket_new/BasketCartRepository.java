package yelm.io.raccoon.database_new.basket_new;

import java.util.List;

import io.reactivex.Flowable;


public class BasketCartRepository {

    private static BasketCartRepository instance;
    private BasketCartDataSource basketCartDataSource;


    public BasketCartRepository(BasketCartDataSource basketCartDataSource) {
        this.basketCartDataSource = basketCartDataSource;
    }

    public List<BasketCart> getListBasketCartByItemID(String basketCartItemId) {
        return basketCartDataSource.getListBasketCartByItemID(basketCartItemId);
    }

    public static BasketCartRepository getInstance(BasketCartDataSource basketCartDataSource) {
        if (instance == null)
        {
            instance = new BasketCartRepository(basketCartDataSource);
        }
        return instance;
    }

    public Flowable<List<BasketCart>> getBasketCarts() {
        return basketCartDataSource.getBasketCarts();
    }

    public List<BasketCart> getBasketCartsList() {
        return basketCartDataSource.getBasketCartsList();
    }

    public BasketCart getBasketCartById(String basketCartItemId) {
        return basketCartDataSource.getBasketCartById(basketCartItemId);
    }

    public String getModifier(String basketCartItemId) {
        return basketCartDataSource.getModifier(basketCartItemId);
    }

    public void emptyBasketCart() {
        basketCartDataSource.emptyBasketCart();
    }

    public void insertToBasketCart(BasketCart... basketCarts) {
        basketCartDataSource.insertToBasketCart(basketCarts);
    }

    public int countBasketCarts() {
        return basketCartDataSource.countBasketCarts();
    }

    public void updateBasketCart(BasketCart... basketCarts) {
        basketCartDataSource.updateBasketCart(basketCarts);
    }

    public void deleteBasketCart(BasketCart basketCarts) {
        basketCartDataSource.deleteBasketCart(basketCarts);
    }

}
