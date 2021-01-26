package yelm.io.yelm.database_old.basket;

import java.util.List;

import io.reactivex.Flowable;

public interface ICartDataSource {

    Flowable<List<Cart>> getCartItems();
    List<Cart> getCartItemsList();
    Cart getCartItemById(String cartItemId);
    int countCardItems();
    void emptyCart();
    void insertToCart (Cart...carts);
    void updateCart (Cart...carts);
    void deleteCartItem (Cart cart);
}
