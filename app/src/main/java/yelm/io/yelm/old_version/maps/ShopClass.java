
package yelm.io.yelm.old_version.maps;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ShopClass {

    @SerializedName("ID")
    @Expose
    private String iD;
    @SerializedName("Latitude")
    @Expose
    private String latitude;
    @SerializedName("Longitude")
    @Expose
    private String longitude;
    @SerializedName("Text")
    @Expose
    private String text;
    @SerializedName("Address")
    @Expose
    private String address;
    @SerializedName("Image")
    @Expose
    private String image;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Phone")
    @Expose
    private String phone;
    @SerializedName("Website")
    @Expose
    private String website;
    @SerializedName("Platform")
    @Expose
    private String platform;
    @SerializedName("TimeWorkStart")
    @Expose
    private String timeWorkStart;
    @SerializedName("TimeWorkEnd")
    @Expose
    private String timeWorkEnd;
    @SerializedName("Login")
    @Expose
    private String login;
    @SerializedName("Password")
    @Expose
    private String password;

    public String getID() {
        return iD;
    }

    public void setID(String iD) {
        this.iD = iD;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getTimeWorkStart() {
        return timeWorkStart;
    }

    public void setTimeWorkStart(String timeWorkStart) {
        this.timeWorkStart = timeWorkStart;
    }

    public String getTimeWorkEnd() {
        return timeWorkEnd;
    }

    public void setTimeWorkEnd(String timeWorkEnd) {
        this.timeWorkEnd = timeWorkEnd;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
