package yelm.io.raccoon.database_new.basket_new;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

import yelm.io.raccoon.main.model.Modifier;

@Entity(tableName = "BasketCart")
public class BasketCart {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "itemID")
    public String itemID;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "startPrice")
    public String startPrice;

    @ColumnInfo(name = "finalPrice")
    public String finalPrice;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "count")
    public String count;

    @ColumnInfo(name = "imageUrl")
    public String imageUrl;

    @ColumnInfo(name = "quantity")
    public String quantity;

    @ColumnInfo(name = "modifier")
    public List<Modifier> modifier = new ArrayList<>();

    @ColumnInfo(name = "isPromo")
    public Boolean isPromo;

    @ColumnInfo(name = "exist")
    public Boolean isExist;

    @ColumnInfo(name = "quantityType")
    public String quantityType;

    @ColumnInfo(name = "discount")
    public String discount;

    @Override
    public String toString() {
        return "BasketCart{" +
                "id=" + id +
                ", itemID='" + itemID + '\'' +
                ", name='" + name + '\'' +
                ", startPrice='" + startPrice + '\'' +
                ", finalPrice='" + finalPrice + '\'' +
                ", type='" + type + '\'' +
                ", count='" + count + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", quantity='" + quantity + '\'' +
                ", modifier=" + modifier +
                ", isPromo=" + isPromo +
                ", isExist=" + isExist +
                ", quantityType='" + quantityType + '\'' +
                ", discount='" + discount + '\'' +
                '}';
    }
}
