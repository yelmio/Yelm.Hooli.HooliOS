package yelm.io.raccoon.order.user_order.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import yelm.io.raccoon.main.model.Item;

public class UserOrderPOJO {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("login")
    @Expose
    private String login;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("comment")
    @Expose
    private String comment;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("flat")
    @Expose
    private String flat;
    @SerializedName("entrance")
    @Expose
    private String entrance;
    @SerializedName("floor")
    @Expose
    private String floor;
    @SerializedName("start_total")
    @Expose
    private String startTotal;
    @SerializedName("end_total")
    @Expose
    private Double endTotal;
    @SerializedName("discount")
    @Expose
    private String discount;
    @SerializedName("items")
    @Expose
    private List<OrderItemPOJO> items = null;
    @SerializedName("items_info")
    @Expose
    private List<Item> itemsInfo = null;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("delivery")
    @Expose
    private String delivery;
    @SerializedName("delivery_price")
    @Expose
    private String deliveryPrice;
    @SerializedName("currency")
    @Expose
    private String currency;
    @SerializedName("payment")
    @Expose
    private String payment;
    @SerializedName("transaction_status")
    @Expose
    private String transactionStatus;
    @SerializedName("latitude")
    @Expose
    private String latitude;
    @SerializedName("longitude")
    @Expose
    private String longitude;
    @SerializedName("platform")
    @Expose
    private String platform;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;


    public List<OrderItemPOJO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemPOJO> items) {
        this.items = items;
    }

    public List<Item> getItemsInfo() {
        return itemsInfo;
    }

    public void setItemsInfo(List<Item> itemsInfo) {
        this.itemsInfo = itemsInfo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFlat() {
        return flat;
    }

    public void setFlat(String flat) {
        this.flat = flat;
    }

    public String getEntrance() {
        return entrance;
    }

    public void setEntrance(String entrance) {
        this.entrance = entrance;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getStartTotal() {
        return startTotal;
    }

    public void setStartTotal(String startTotal) {
        this.startTotal = startTotal;
    }

    public Double getEndTotal() {
        return endTotal;
    }

    public void setEndTotal(Double endTotal) {
        this.endTotal = endTotal;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDelivery() {
        return delivery;
    }

    public void setDelivery(String delivery) {
        this.delivery = delivery;
    }

    public String getDeliveryPrice() {
        return deliveryPrice;
    }

    public void setDeliveryPrice(String deliveryPrice) {
        this.deliveryPrice = deliveryPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
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
}
