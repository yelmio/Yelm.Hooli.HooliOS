package yelm.io.raccoon.database_old.articles;

import java.util.List;

import io.reactivex.Flowable;

public class ArticleDataSource {

    private ArticlesDao articlesDAO;
    private static ArticleDataSource instance;

    public ArticleDataSource(ArticlesDao articlesDAO) {
        this.articlesDAO = articlesDAO;
    }

    public static ArticleDataSource getInstance(ArticlesDao articlesDAO) {
        if (instance == null) {
            instance = new ArticleDataSource(articlesDAO);
        }
        return instance;
    }


    public Flowable<List<Articles>> getArticlesItems() {
        return articlesDAO.getArticlesItems();
    }
    public List<Articles> getArticlesItemsList() {
        return articlesDAO.getArticlesItemsList();
    }

    public void emptyArticles() {
        articlesDAO.emptyArticles();
    }

    public void insertToArticles(Articles... articles) {
        articlesDAO.insertToArticles(articles);
    }

    public int countArticlesItems() {
        return articlesDAO.countArticlesItems();
    }


}
