package yelm.io.yelm.basket.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeletedId {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("available_count")
    @Expose
    private Integer availableCount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAvailableCount() {
        return availableCount;
    }

    public void setAvailableCount(Integer availableCount) {
        this.availableCount = availableCount;
    }

}
