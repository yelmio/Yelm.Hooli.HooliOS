package yelm.io.raccoon.database_new.basket_new;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface BasketCartDao {

    @Query("SELECT * FROM BasketCart")
    Flowable<List<BasketCart>> getBasketCarts();

    @Query("SELECT * FROM BasketCart WHERE itemID =:BasketCartItemId")
    BasketCart getBasketCartById(String BasketCartItemId);

    @Query("SELECT * FROM BasketCart WHERE itemID = :BasketCartItemId")
    List<BasketCart> getListBasketCartByItemID(String BasketCartItemId);

    @Query("SELECT modifier FROM BasketCart WHERE itemID =:BasketCartItemId")
    String getModifier(String BasketCartItemId);

    @Query("SELECT COUNT(*) FROM BasketCart")
    int countBasketCarts();

    @Query("DELETE FROM BasketCart")
    void emptyBasketCart();

    @Query("SELECT * FROM BasketCart")
    List<BasketCart> getBasketCartsList();

    @Insert
    void insertToBasketCart (BasketCart...basketCarts);

    @Update
    void updateBasketCart (BasketCart...basketCarts);

    @Delete
    void deleteBasketCart (BasketCart basketCarts);

}
