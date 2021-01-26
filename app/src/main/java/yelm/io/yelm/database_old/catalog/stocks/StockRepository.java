package yelm.io.yelm.database_old.catalog.stocks;

import java.util.List;

import io.reactivex.Flowable;

public class StockRepository {

    private static StockRepository instance;
    private StockDataSource stockDataSource;


    public StockRepository(StockDataSource stockDataSource) {
        this.stockDataSource = stockDataSource;
    }

    public static StockRepository getInstance(StockDataSource stockDataSource) {
        if (instance == null)
        {
            instance = new StockRepository(stockDataSource);
        }
        return instance;
    }

    public Flowable<List<Stock>> getStocks() {
        return stockDataSource.getStocks();
    }

    public void emptyStock() {
        stockDataSource.emptyStock();
    }

    public void insertToStock(Stock... stocks) {
        stockDataSource.insertToStock(stocks);
    }

    public int countStock() {
        return stockDataSource.countStock();
    }




}
