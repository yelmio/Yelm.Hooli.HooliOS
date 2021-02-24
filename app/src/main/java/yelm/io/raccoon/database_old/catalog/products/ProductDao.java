package yelm.io.raccoon.database_old.catalog.products;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import io.reactivex.Flowable;

@Dao
public interface ProductDao {

    @Query("SELECT * FROM Product")
    Flowable<List<Product>> getProducts();

    @Insert
    void insertToProduct(Product... products);

    @Query("DELETE FROM Product")
    void emptyProduct();

    @Query("SELECT COUNT(*) FROM Product")
    int countProduct();

    @Query("SELECT * FROM Product")
    List<Product> getProductsList();


}
