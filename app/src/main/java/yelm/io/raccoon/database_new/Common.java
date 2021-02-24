package yelm.io.raccoon.database_new;

import yelm.io.raccoon.database_new.basket_new.BasketCartRepository;
import yelm.io.raccoon.database_new.user_addresses.UserAddressesRepository;
import yelm.io.raccoon.database_old.articles.ArticlesRepository;
import yelm.io.raccoon.database_old.basket.CartRepository;
import yelm.io.raccoon.database_old.catalog.stocks.StockRepository;
import yelm.io.raccoon.database_old.news.NewsRepository;

import yelm.io.raccoon.database_old.catalog.products.ProductRepository;
import yelm.io.raccoon.database_old.user.UserRepository;

public class Common {
    public static Database sDatabase;
    public static CartRepository cartRepository;
    public static ArticlesRepository articlesRepository;

    public static UserRepository userRepository;
    public static BasketCartRepository basketCartRepository;

    public static ProductRepository productRepository;
    public static StockRepository stockRepository;
    public static UserAddressesRepository userAddressesRepository;
    public static NewsRepository newsRepository;
}