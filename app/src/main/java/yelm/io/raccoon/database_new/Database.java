package yelm.io.raccoon.database_new;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import yelm.io.raccoon.database_new.converters.ModifiersConverter;
import yelm.io.raccoon.database_new.basket_new.BasketCart;
import yelm.io.raccoon.database_new.basket_new.BasketCartDao;
import yelm.io.raccoon.database_new.user_addresses.AddressesDao;
import yelm.io.raccoon.database_new.user_addresses.UserAddress;
import yelm.io.raccoon.database_old.articles.Articles;
import yelm.io.raccoon.database_old.articles.ArticlesDao;
import yelm.io.raccoon.database_old.basket.Cart;
import yelm.io.raccoon.database_old.basket.CartDAO;
import yelm.io.raccoon.database_old.catalog.stocks.Stock;
import yelm.io.raccoon.database_old.catalog.stocks.StockDao;
import yelm.io.raccoon.database_old.news.News;
import yelm.io.raccoon.database_old.news.NewsDao;
import yelm.io.raccoon.database_old.catalog.products.Product;
import yelm.io.raccoon.database_old.catalog.products.ProductDao;
import yelm.io.raccoon.database_old.user.User;
import yelm.io.raccoon.database_old.user.UserDao;

@androidx.room.Database(entities =
        {Cart.class, Product.class, Articles.class, Stock.class, News.class, User.class, UserAddress.class, BasketCart.class},
        version = 2,
        exportSchema = false)
@TypeConverters({ModifiersConverter.class})

public abstract class Database extends RoomDatabase {

    public abstract CartDAO cartDAO();

    public abstract UserDao userDAO();

    public abstract BasketCartDao basketCartDao();

    public abstract ArticlesDao articlesDao();

    public abstract ProductDao productDao();

    public abstract StockDao stockDao();

    public abstract NewsDao newsDao();

    public abstract AddressesDao addressesDao();

    private static Database instance;

    public static synchronized Database getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, Database.class, "DataBase").
                    fallbackToDestructiveMigration().
                    allowMainThreadQueries().
                    //.addMigrations(MIGRATION_1_2)
                    build();
        }
        return instance;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Поскольку мы не изменяли таблицу, здесь больше ничего не нужно делать.
        }
    };
}