package yelm.io.yelm.database_old.basket;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName="Cart")
public class Cart {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    public int id;

    @ColumnInfo(name="item")
    public String item;

    @ColumnInfo(name="name")
    public String name;

    @ColumnInfo(name="price")
    public String price;

    @ColumnInfo(name="type")
    public String type;

    @ColumnInfo(name="count")
    public String count;

    @ColumnInfo(name="imageUrl")
    public String imageUrl;

    @ColumnInfo(name = "quantity")
    public String quantity;

    @ColumnInfo(name = "isPromo")
    public Boolean isPromo;

}
