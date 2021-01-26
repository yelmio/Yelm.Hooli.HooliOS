
package yelm.io.yelm.fragments.catalog_fragment.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CatalogClass {

    @SerializedName("item")
    @Expose
    private Item item;
    @SerializedName("item_count")
    @Expose
    private String itemCount;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getItemCount() {
        return itemCount;
    }

    public void setItemCount(String itemCount) {
        this.itemCount = itemCount;
    }

}
