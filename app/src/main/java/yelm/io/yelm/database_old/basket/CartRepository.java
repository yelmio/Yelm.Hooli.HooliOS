package yelm.io.yelm.database_old.basket;

import java.util.List;

import io.reactivex.Flowable;

public class CartRepository implements ICartDataSource {

    private ICartDataSource iCartDataResource;
    private static CartRepository instance;

    public CartRepository(ICartDataSource iCartDataResource) {
        this.iCartDataResource = iCartDataResource;
    }

    public static CartRepository getInstance(ICartDataSource iCartDataResource) {
        if (instance == null)
        {
            instance = new CartRepository(iCartDataResource);
        }
        return instance;
    }

    @Override
    public Flowable<List<Cart>> getCartItems() {
        return iCartDataResource.getCartItems();
    }

    @Override
    public List<Cart> getCartItemsList() {
        return iCartDataResource.getCartItemsList();
    }


    @Override
    public Cart getCartItemById(String cartItemId) {
        return iCartDataResource.getCartItemById(cartItemId);
    }


    @Override
    public int countCardItems() {
        return iCartDataResource.countCardItems();
    }

    @Override
    public void emptyCart() {
        iCartDataResource.emptyCart();
    }

    @Override
    public void insertToCart(Cart... carts) {
        iCartDataResource.insertToCart(carts);
    }

    @Override
    public void updateCart(Cart... carts) {
        iCartDataResource.updateCart(carts);
    }

    @Override
    public void deleteCartItem(Cart cart) {
        iCartDataResource.deleteCartItem(cart);
    }
}
