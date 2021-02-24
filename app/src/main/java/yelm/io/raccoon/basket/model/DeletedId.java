package yelm.io.raccoon.basket.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeletedId {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("available_count")
    @Expose
    private String availableCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvailableCount() {
        return availableCount;
    }

    public void setAvailableCount(String availableCount) {
        this.availableCount = availableCount;
    }

    @Override
    public String toString() {
        return "DeletedId{" +
                "id='" + id + '\'' +
                ", availableCount='" + availableCount + '\'' +
                '}';
    }
}
