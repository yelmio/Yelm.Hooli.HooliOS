package yelm.io.yelm.basket.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BasketCheckResponse {


    @SerializedName("DeletedID")
    @Expose
    private List<String> deletedID = null;
    @SerializedName("PriceDelivery")
    @Expose
    private String priceDelivery;
    @SerializedName("TimeDelivery")
    @Expose
    private String timeDelivery;

    public List<String> getDeletedID() {
        return deletedID;
    }

    public void setDeletedID(List<String> deletedID) {
        this.deletedID = deletedID;
    }

    public String getPriceDelivery() {
        return priceDelivery;
    }

    public void setPriceDelivery(String priceDelivery) {
        this.priceDelivery = priceDelivery;
    }

    public String getTimeDelivery() {
        return timeDelivery;
    }

    public void setTimeDelivery(String timeDelivery) {
        this.timeDelivery = timeDelivery;
    }

}
