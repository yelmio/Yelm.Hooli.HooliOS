package yelm.io.yelm.database_old.news;

import java.util.List;

import io.reactivex.Flowable;


public class NewsRepository {

    private static NewsRepository instance;
    private NewsDataSource newsDataSource;


    public NewsRepository(NewsDataSource newsDataSource) {
        this.newsDataSource = newsDataSource;
    }

    public static NewsRepository getInstance(NewsDataSource newsDataSource) {
        if (instance == null)
        {
            instance = new NewsRepository(newsDataSource);
        }
        return instance;
    }

    public Flowable<List<News>> getNews() {
        return newsDataSource.getNews();
    }

    public void emptyNews() {
        newsDataSource.emptyNews();
    }

    public void insertToNews(News... news) {
        newsDataSource.insertToNews(news);
    }

    public int countNews() {
        return newsDataSource.countNews();
    }



}
