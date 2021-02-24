package yelm.io.raccoon.database_old.catalog.products;

import java.util.List;

import io.reactivex.Flowable;


public class ProductRepository {

    private static ProductRepository instance;
    private ProductDataSource productDataSource;


    public ProductRepository(ProductDataSource productDataSource) {
        this.productDataSource = productDataSource;
    }

    public static ProductRepository getInstance(ProductDataSource productDataSource) {
        if (instance == null)
        {
            instance = new ProductRepository(productDataSource);
        }
        return instance;
    }

    public Flowable<List<Product>> getProducts() {
        return productDataSource.getProducts();
    }

    public List<Product> getProductsList() {
        return productDataSource.getProductsList();
    }

    public void emptyProduct() {
        productDataSource.emptyProduct();
    }

    public void insertToProduct(Product... products) {
        productDataSource.insertToProduct(products);
    }

    public int countProduct() {
        return productDataSource.countProduct();
    }


}
