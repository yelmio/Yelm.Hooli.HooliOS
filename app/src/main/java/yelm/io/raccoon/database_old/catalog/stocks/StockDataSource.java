package yelm.io.raccoon.database_old.catalog.stocks;

import java.util.List;

import io.reactivex.Flowable;

public class StockDataSource {

    private StockDao stockDao;
    private static StockDataSource instance;

    public StockDataSource(StockDao stockDao) {
        this.stockDao = stockDao;
    }

    public static StockDataSource getInstance(StockDao stockDao) {
        if (instance == null) {
            instance = new StockDataSource(stockDao);
        }
        return instance;
    }

    public Flowable<List<Stock>> getStocks() {
        return stockDao.getStocks();
    }

    public void emptyStock() {
        stockDao.emptyStock();
    }

    public void insertToStock(Stock... stocks) {
        stockDao.insertToStock(stocks);
    }

    public int countStock() {
        return stockDao.countStock();
    }

}
