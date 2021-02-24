package yelm.io.raccoon.loader.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ChatSettingsClass {


    @SerializedName("api_token")
    @Expose
    private String apiToken;
    @SerializedName("room_id")
    @Expose
    private String roomId;
    @SerializedName("shop")
    @Expose
    private String shop;
    @SerializedName("client")
    @Expose
    private String client;

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getShop() {
        return shop;
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    @Override
    public String toString() {
        return "ChatSettingsClass{" +
                "apiToken='" + apiToken + '\'' +
                ", roomId=" + roomId +
                ", shop=" + shop +
                ", client=" + client +
                '}';
    }

}
