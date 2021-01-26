package yelm.io.yelm.database_old.articles;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface ArticlesDao {

    @Query("SELECT * FROM Articles")
    Flowable<List<Articles>> getArticlesItems();

    @Query("SELECT * FROM Articles WHERE id =:articleItemId")
    Flowable<List<Articles>> getArticleItemById(int articleItemId);

    @Query("SELECT * FROM Articles WHERE title =:articleTitle")
    Articles getArticleItemByName(String articleTitle);

    @Query("SELECT COUNT(*) FROM Articles")
    int countArticlesItems();

    @Query("DELETE FROM Articles")
    void emptyArticles();

    @Query("SELECT * FROM Articles")
    List<Articles> getArticlesItemsList();

    @Insert
    void insertToArticles (Articles...carts);

    @Update
    void updateArticles (Articles...carts);

    @Delete
    void deleteArticleItem (Articles cart);



}
