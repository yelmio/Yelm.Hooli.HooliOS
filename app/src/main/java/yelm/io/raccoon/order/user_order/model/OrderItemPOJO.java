package yelm.io.raccoon.order.user_order.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OrderItemPOJO {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("count")
    @Expose
    private String count;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
