package yelm.io.raccoon.database_old.news;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface NewsDao {

    @Query("SELECT * FROM News")
    Flowable<List<News>> getNews();

    @Insert
    void insertToNews(News... news);

    @Query("DELETE FROM News")
    void emptyNews();

    @Query("SELECT COUNT(*) FROM News")
    int countNews();

}
