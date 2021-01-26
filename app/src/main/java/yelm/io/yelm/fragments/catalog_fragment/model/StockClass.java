
package yelm.io.yelm.fragments.catalog_fragment.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StockClass {

    @SerializedName("ID")
    @Expose
    private String iD;
    @SerializedName("Platform")
    @Expose
    private String platform;
    @SerializedName("Image")
    @Expose
    private String image;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("CardsType")
    @Expose
    private String cardsType;
    @SerializedName("OpenType")
    @Expose
    private String openType;
    @SerializedName("OpenValue")
    @Expose
    private String openValue;
    @SerializedName("CreateDate")
    @Expose
    private String createDate;

    @SerializedName("Attachments")
    @Expose
    private String attachments;

    public String getID() {
        return iD;
    }

    public void setID(String iD) {
        this.iD = iD;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCardsType() {
        return cardsType;
    }

    public void setCardsType(String cardsType) {
        this.cardsType = cardsType;
    }

    public String getOpenType() {
        return openType;
    }

    public void setOpenType(String openType) {
        this.openType = openType;
    }

    public String getOpenValue() {
        return openValue;
    }

    public void setOpenValue(String openValue) {
        this.openValue = openValue;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }


}
