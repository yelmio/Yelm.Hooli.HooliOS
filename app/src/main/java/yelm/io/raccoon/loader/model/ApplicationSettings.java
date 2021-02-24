package yelm.io.raccoon.loader.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ApplicationSettings {

    @SerializedName("currency")
    @Expose
    private String currency;
    @SerializedName("symbol")
    @Expose
    private String symbol;
    @SerializedName("shop_id")
    @Expose
    private String shopId;
    @SerializedName("settings")
    @Expose
    private Setting settings;


    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public Setting getSettings() {
        return settings;
    }

    public void setSettings(Setting settings) {
        this.settings = settings;
    }


}
