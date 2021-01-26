
package yelm.io.yelm.old_version.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserOrderClass {

    @SerializedName("ID")
    @Expose
    private String iD;
    @SerializedName("OrderItem")
    @Expose
    private String orderItem;
    @SerializedName("CreateDate")
    @Expose
    private String createDate;
    @SerializedName("Platform")
    @Expose
    private String platform;
    @SerializedName("User")
    @Expose
    private String user;
    @SerializedName("UserStatus")
    @Expose
    private String userStatus;
    @SerializedName("TransactionN")
    @Expose
    private String transactionN;
    @SerializedName("SelectedShopID")
    @Expose
    private String selectedShopID;

    public String getID() {
        return iD;
    }

    public void setID(String iD) {
        this.iD = iD;
    }

    public String getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(String orderItem) {
        this.orderItem = orderItem;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getTransactionN() {
        return transactionN;
    }

    public void setTransactionN(String transactionN) {
        this.transactionN = transactionN;
    }

    public String getSelectedShopID() {
        return selectedShopID;
    }

    public void setSelectedShopID(String selectedShopID) {
        this.selectedShopID = selectedShopID;
    }

}
