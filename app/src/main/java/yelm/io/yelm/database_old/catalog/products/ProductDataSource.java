package yelm.io.yelm.database_old.catalog.products;

import java.util.List;

import io.reactivex.Flowable;

public class ProductDataSource {

    private ProductDao productDAO;
    private static ProductDataSource instance;

    public ProductDataSource(ProductDao productDAO) {
        this.productDAO = productDAO;
    }

    public static ProductDataSource getInstance(ProductDao productDAO) {
        if (instance == null) {
            instance = new ProductDataSource(productDAO);
        }
        return instance;
    }


    public Flowable<List<Product>> getProducts() {
        return productDAO.getProducts();
    }
    public List<Product> getProductsList() {
        return productDAO.getProductsList();
    }

    public void emptyProduct() {
        productDAO.emptyProduct();
    }

    public void insertToProduct(Product... products) {
        productDAO.insertToProduct(products);
    }

    public int countProduct() {
        return productDAO.countProduct();
    }

}
