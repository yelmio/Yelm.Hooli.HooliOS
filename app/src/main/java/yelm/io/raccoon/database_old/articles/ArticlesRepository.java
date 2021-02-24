package yelm.io.raccoon.database_old.articles;

import java.util.List;

import io.reactivex.Flowable;


public class ArticlesRepository {


    private static ArticlesRepository instance;
    private ArticleDataSource articleDataSource;


    public ArticlesRepository(ArticleDataSource articleDataSource) {
        this.articleDataSource = articleDataSource;
    }

    public static ArticlesRepository getInstance(ArticleDataSource articleDataSource) {
        if (instance == null) {
            instance = new ArticlesRepository(articleDataSource);
        }
        return instance;
    }

    public Flowable<List<Articles>> getArticlesItems() {
        return articleDataSource.getArticlesItems();
    }

    public List<Articles> getArticlesItemsList() {
        return articleDataSource.getArticlesItemsList();
    }

    public void emptyArticles() {
        articleDataSource.emptyArticles();
    }

    public void insertToArticles(Articles... articles) {
        articleDataSource.insertToArticles(articles);
    }

    public int countArticlesItems() {
        return articleDataSource.countArticlesItems();
    }


}
