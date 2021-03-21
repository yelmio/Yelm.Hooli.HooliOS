package yelm.io.raccoon.basket.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BasketCheckPOJO {

    @SerializedName("deleted_id")
    @Expose
    private List<DeletedId> deletedId = null;
    @SerializedName("delivery")
    @Expose
    private Delivery delivery;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("time_work")
    @Expose
    private String timeWork;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimeWork() {
        return timeWork;
    }

    public void setTimeWork(String timeWork) {
        this.timeWork = timeWork;
    }

    public List<DeletedId> getDeletedId() {
        return deletedId;
    }

    public void setDeletedId(List<DeletedId> deletedId) {
        this.deletedId = deletedId;
    }

    public Delivery getDelivery() {
        return delivery;
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }

    @Override
    public String toString() {
        return "BasketCheckPOJO{" +
                "deletedId=" + deletedId +
                ", delivery=" + delivery +
                ", type='" + type + '\'' +
                ", timeWork='" + timeWork + '\'' +
                '}';
    }
}
