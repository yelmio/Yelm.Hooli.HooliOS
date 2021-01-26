
package yelm.io.yelm.fragments.settings_fragment.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CompanyClass {

    @SerializedName("ID")
    @Expose
    private String iD;
    @SerializedName("Platform")
    @Expose
    private String platform;
    @SerializedName("Create_Date")
    @Expose
    private String createDate;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Status")
    @Expose
    private String status;
    @SerializedName("Content")
    @Expose
    private String content;
    @SerializedName("Basis")
    @Expose
    private String basis;

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

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBasis() {
        return basis;
    }

    public void setBasis(String basis) {
        this.basis = basis;
    }

}
