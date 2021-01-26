
package yelm.io.yelm.fragments.main_fragment.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ArticleClass {
    @SerializedName("ID")
    @Expose
    private String iD;
    @SerializedName("Item")
    @Expose
    private String item;
    @SerializedName("CreateDate")
    @Expose
    private String createDate;
    @SerializedName("Platform")
    @Expose
    private String platform;
    @SerializedName("ViewScreen")
    @Expose
    private String viewScreen;

    public String getID() {
        return iD;
    }

    public void setID(String iD) {
        this.iD = iD;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
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
    public String getViewScreen() {
        return viewScreen;
    }

    public void setViewScreen(String viewScreen) {
        this.viewScreen = viewScreen;
    }
}
