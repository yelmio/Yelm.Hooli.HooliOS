
package yelm.io.raccoon.loader.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PrimarySettingsClass {

    @SerializedName("Username")
    @Expose
    private String username;

    @SerializedName("UserType")
    @Expose
    private String userType;

    @SerializedName("PublicID")
    @Expose
    private String publicID;

    @SerializedName("AllowPayments")
    @Expose
    private String allowPayments;

    @SerializedName("minPrice")
    @Expose
    private String minPrice;

    @SerializedName("minDeliveryPrice")
    @Expose
    private String minDeliveryPrice;

    @SerializedName("CatalogStyle")
    @Expose
    private String catalogStyle;

    @SerializedName("allowv3")
    @Expose
    private String allowv3;

    @SerializedName("allowDeliverly")
    @Expose
    private String allowDeliverly;

    @SerializedName("allowTakeOff")
    @Expose
    private String allowTakeOff;

    @SerializedName("PriceIn")
    @Expose
    private String priceIn;

    @SerializedName("CNT")
    @Expose
    private String CNT;

    @SerializedName("countryCode")
    @Expose
    private String countryCode;

    public String getCNT() {
        return CNT;
    }

    public void setCNT(String CNT) {
        this.CNT = CNT;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }


    public String getAllowv3() {
        return allowv3;
    }

    public void setAllowv3(String allowv3) {
        this.allowv3 = allowv3;
    }

    public String getAllowDeliverly() {
        return allowDeliverly;
    }

    public void setAllowDeliverly(String allowDeliverly) {
        this.allowDeliverly = allowDeliverly;
    }

    public String getAllowTakeOff() {
        return allowTakeOff;
    }

    public void setAllowTakeOff(String allowTakeOff) {
        this.allowTakeOff = allowTakeOff;
    }

    public String getPriceIn() {
        return priceIn;
    }

    public void setPriceIn(String priceIn) {
        this.priceIn = priceIn;
    }

    public String getCatalogStyle() {
        return catalogStyle;
    }

    public void setCatalogStyle(String catalogStyle) {
        this.catalogStyle = catalogStyle;
    }

    public String getMinDeliveryPrice() {
        return minDeliveryPrice;
    }

    public void setMinDeliveryPrice(String minDeliveryPrice) {
        this.minDeliveryPrice = minDeliveryPrice;
    }

    public String getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(String minPrice) {
        this.minPrice = minPrice;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getPublicID() {
        return publicID;
    }

    public void setPublicID(String publicID) {
        this.publicID = publicID;
    }

    public String getAllowPayments() {
        return allowPayments;
    }

    public void setAllowPayments(String allowPayments) {
        this.allowPayments = allowPayments;
    }

    @Override
    public String toString() {
        return "PrimarySettingsClass{" +
                "username='" + username + '\'' +
                ", userType='" + userType + '\'' +
                ", publicID='" + publicID + '\'' +
                ", allowPayments='" + allowPayments + '\'' +
                ", minPrice='" + minPrice + '\'' +
                ", minDeliveryPrice='" + minDeliveryPrice + '\'' +
                ", catalogStyle='" + catalogStyle + '\'' +
                ", allowv3='" + allowv3 + '\'' +
                ", allowDeliverly='" + allowDeliverly + '\'' +
                ", allowTakeOff='" + allowTakeOff + '\'' +
                ", priceIn='" + priceIn + '\'' +
                ", CNT='" + CNT + '\'' +
                ", countryCode='" + countryCode + '\'' +
                '}';
    }
}