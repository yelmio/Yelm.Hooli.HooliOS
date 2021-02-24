package yelm.io.raccoon.database_old.news;

import java.util.List;

import io.reactivex.Flowable;


public class NewsDataSource {

    private NewsDao newsDao;
    private static NewsDataSource instance;

    public NewsDataSource(NewsDao newsDao) {
        this.newsDao = newsDao;
    }

    public static NewsDataSource getInstance(NewsDao newsDao) {
        if (instance == null) {
            instance = new NewsDataSource(newsDao);
        }
        return instance;
    }

    public Flowable<List<News>> getNews() {
        return newsDao.getNews();
    }

    public void emptyNews() {
        newsDao.emptyNews();
    }

    public void insertToNews(News... news) {
        newsDao.insertToNews(news);
    }

    public int countNews() {
        return newsDao.countNews();
    }


}
