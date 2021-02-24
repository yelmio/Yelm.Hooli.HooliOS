package yelm.io.raccoon.main.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CategoriesWithProductsClass {

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("shop_id")
    @Expose
    private String shopID;

    @SerializedName("category_id")
    @Expose
    private String categoryID;

    @SerializedName("items")
    @Expose
    private List<Item> items = null;

    public String getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShopID() {
        return shopID;
    }

    public void setShopID(String shopID) {
        this.shopID = shopID;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
