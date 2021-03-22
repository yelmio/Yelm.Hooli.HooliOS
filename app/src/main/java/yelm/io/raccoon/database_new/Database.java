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

@androidx.room.Database(entities =
        {UserAddress.class, BasketCart.class},
        version = 3,
        exportSchema = false)
@TypeConverters({ModifiersConverter.class})

public abstract class Database extends RoomDatabase {

    public abstract BasketCartDao basketCartDao();

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