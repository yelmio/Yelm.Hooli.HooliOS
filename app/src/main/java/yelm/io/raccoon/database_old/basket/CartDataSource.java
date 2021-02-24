package yelm.io.raccoon.database_old.basket;

import java.util.List;

import io.reactivex.Flowable;

public class CartDataSource implements ICartDataSource {

    private CartDAO cartDAO;
    private static CartDataSource instance;

    public CartDataSource(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
    }

    public static CartDataSource getInstance(CartDAO cartDAO) {
        if (instance == null) {
            instance = new CartDataSource(cartDAO);
        }
        return instance;
    }


    @Override
    public Flowable<List<Cart>> getCartItems() {
        return cartDAO.getCartItems();
    }

    @Override
    public List<Cart> getCartItemsList() {
        return cartDAO.getCartItemsList();
    }


    @Override
    public Cart getCartItemById(String cartItemId) {
        return cartDAO.getCartItemById(cartItemId);
    }

    @Override
    public int countCardItems() {
        return cartDAO.countCardItems();
    }

    @Override
    public void emptyCart() {
        cartDAO.emptyCart();
    }

    @Override
    public void insertToCart(Cart... carts) {
        cartDAO.insertToCart(carts);
    }

    @Override
    public void updateCart(Cart... carts) {
        cartDAO.updateCart(carts);
    }

    @Override
    public void deleteCartItem(Cart cart) {
        cartDAO.deleteCartItem(cart);
    }
}
