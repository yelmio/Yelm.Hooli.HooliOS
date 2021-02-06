package yelm.io.yelm.basket.model;

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
                '}';
    }
}
