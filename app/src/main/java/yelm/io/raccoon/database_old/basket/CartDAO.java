package yelm.io.raccoon.database_old.basket;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import io.reactivex.Flowable;

@Dao
public interface CartDAO {

    @Query("SELECT * FROM Cart")
    Flowable<List<Cart>> getCartItems();

    @Query("SELECT * FROM Cart WHERE item =:cartItemId")
    Cart getCartItemById(String cartItemId);

    @Query("SELECT COUNT(*) FROM Cart")
    int countCardItems();

    @Query("DELETE FROM Cart")
    void emptyCart();

    @Query("SELECT * FROM Cart")
    List<Cart> getCartItemsList();

    @Insert
    void insertToCart (Cart...carts);

    @Update
    void updateCart (Cart...carts);

    @Delete
    void deleteCartItem (Cart cart);

}
