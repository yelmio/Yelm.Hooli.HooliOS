package yelm.io.raccoon.database_old.catalog.stocks;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import io.reactivex.Flowable;

@Dao
public interface StockDao {

    @Query("SELECT * FROM Stock")
    Flowable<List<Stock>> getStocks();

    @Insert
    void insertToStock(Stock...stocks);

    @Query("DELETE FROM Stock")
    void emptyStock();

    @Query("SELECT COUNT(*) FROM Stock")
    int countStock();

}
