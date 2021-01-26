package yelm.io.yelm.database_old.catalog.products;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;

@Entity(tableName = "Product")
public class Product {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name="itemID")
    public String itemID;

    @ColumnInfo(name="type")
    public String type;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "price")
    public String price;

    @ColumnInfo(name = "parameters")
    public String parameters;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "quantity")
    public String quantity;

    @ColumnInfo(name = "text_about")
    public String text_about;

    @ColumnInfo(name = "image")
    public String image;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @ColumnInfo(name = "date")
    public String date;


    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getText_about() {
        return text_about;
    }

    public void setText_about(String text_about) {
        this.text_about = text_about;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Product(String itemID, String type, String name, String price, String parameters, String category, String quantity, String text_about, String image, String date) {
        this.itemID = itemID;
        this.type = type;
        this.name = name;
        this.price = price;
        this.parameters = parameters;
        this.category = category;
        this.quantity = quantity;
        this.text_about = text_about;
        this.image = image;
        this.date = date;

    }

    //по убыванию
    public static Comparator<Product> BY_PRICE_DESC = new Comparator<Product>() {
        @Override
        public int compare(Product p1, Product p2) {
            Integer i1 = Integer.parseInt(p1.getPrice());
            Integer i2 = Integer.parseInt(p2.getPrice());
            return i2.compareTo(i1);
        }
    };
    //по возрастанию
    public static Comparator<Product> BY_PRICE_ASC = new Comparator<Product>() {
        @Override
        public int compare(Product p1, Product p2) {
            Integer i1 = Integer.parseInt(p1.getPrice());
            Integer i2 = Integer.parseInt(p2.getPrice());
            return i1.compareTo(i2);
        }
    };


    public static Comparator<Product> BY_DATE_ASC = new Comparator<Product>() {
        @Override
        public int compare(Product p1, Product p2) {

            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            SimpleDateFormat calFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            try {
                cal1.setTime(calFormat.parse(p1.getDate()));
                cal2.setTime(calFormat.parse(p2.getDate()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return cal1.compareTo(cal2);
        }
    };

    public static Comparator<Product> BY_DATE_DESC = new Comparator<Product>() {
        @Override
        public int compare(Product p1, Product p2) {

            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            SimpleDateFormat calFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            try {
                cal1.setTime(calFormat.parse(p1.getDate()));
                cal2.setTime(calFormat.parse(p2.getDate()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return cal2.compareTo(cal1);
        }
    };


}
