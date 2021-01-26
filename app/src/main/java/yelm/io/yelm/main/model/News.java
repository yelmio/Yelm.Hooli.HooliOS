package yelm.io.yelm.main.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class News {

    @SerializedName("ID")
    @Expose
    private String iD;
    @SerializedName("Platform")
    @Expose
    private String platform;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("Theme")
    @Expose
    private String theme;
    @SerializedName("Text")
    @Expose
    private String text;
    @SerializedName("Status")
    @Expose
    private String status;
    @SerializedName("ViewScreen")
    @Expose
    private String viewScreen;
    @SerializedName("Image")
    @Expose
    private List<String> image = null;
    @SerializedName("CreateDate")
    @Expose
    private String createDate;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getViewScreen() {
        return viewScreen;
    }

    public void setViewScreen(String viewScreen) {
        this.viewScreen = viewScreen;
    }

    public List<String> getImage() {
        return image;
    }

    public void setImage(List<String> image) {
        this.image = image;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

}
