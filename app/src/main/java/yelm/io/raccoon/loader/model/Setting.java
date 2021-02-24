package yelm.io.raccoon.loader.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Setting {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("theme")
    @Expose
    private String theme;
    @SerializedName("foreground")
    @Expose
    private String foreground;
    @SerializedName("min_order_price")
    @Expose
    private String minOrderPrice;
    @SerializedName("min_delivery_price")
    @Expose
    private String minDeliveryPrice;
    @SerializedName("region_code")
    @Expose
    private String regionCode;
    @SerializedName("public_id")
    @Expose
    private String publicId;
    @SerializedName("api_key")
    @Expose
    private String apiKey;
    @SerializedName("app_version")
    @Expose
    private String appVersion;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getForeground() {
        return foreground;
    }

    public void setForeground(String foreground) {
        this.foreground = foreground;
    }

    public String getMinOrderPrice() {
        return minOrderPrice;
    }

    public void setMinOrderPrice(String minOrderPrice) {
        this.minOrderPrice = minOrderPrice;
    }

    public String getMinDeliveryPrice() {
        return minDeliveryPrice;
    }

    public void setMinDeliveryPrice(String minDeliveryPrice) {
        this.minDeliveryPrice = minDeliveryPrice;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
