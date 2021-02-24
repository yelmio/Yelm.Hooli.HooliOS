package yelm.io.raccoon.chat.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import yelm.io.raccoon.main.model.Item;

public class ChatHistoryClass {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("room_id")
    @Expose
    private String roomId;
    @SerializedName("from_whom")
    @Expose
    private String fromWhom;
    @SerializedName("to_whom")
    @Expose
    private String toWhom;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("images")
    @Expose
    private ArrayList<String> images;
    @SerializedName("platform")
    @Expose
    private String platform;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("items")
    @Expose
    private Item items;
    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("order_id")
    @Expose
    private String orderID;

    @Override
    public String toString() {
        return "ChatHistoryClass{" +
                "id='" + id + '\'' +
                ", roomId='" + roomId + '\'' +
                ", fromWhom='" + fromWhom + '\'' +
                ", toWhom='" + toWhom + '\'' +
                ", message='" + message + '\'' +
                ", images=" + images +
                ", platform='" + platform + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", items=" + items +
                ", type='" + type + '\'' +
                '}';
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getFromWhom() {
        return fromWhom;
    }

    public void setFromWhom(String fromWhom) {
        this.fromWhom = fromWhom;
    }

    public String getToWhom() {
        return toWhom;
    }

    public void setToWhom(String toWhom) {
        this.toWhom = toWhom;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
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

    public Item getItems() {
        return items;
    }

    public void setItems(Item items) {
        this.items = items;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
