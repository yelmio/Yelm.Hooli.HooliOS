package yelm.io.raccoon.database_old.user;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "User")
public class User {

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "userName")
    public String userName;

    @ColumnInfo(name = "userPhone")
    public String userPhone;

    @ColumnInfo(name = "userAddress")
    public String userAddress;

    @ColumnInfo(name = "userOffice")
    public String userOffice;

    @ColumnInfo(name = "userIntercom")
    public String userIntercom;

    @ColumnInfo(name = "userEntrance")
    public String userEntrance;

    @ColumnInfo(name = "userFloor")
    public String userFloor;

    @ColumnInfo(name = "latitude")
    public String latitude;

    @ColumnInfo(name = "longitude")
    public String longitude;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", userPhone='" + userPhone + '\'' +
                ", userAddress='" + userAddress + '\'' +
                ", userOffice='" + userOffice + '\'' +
                ", userIntercom='" + userIntercom + '\'' +
                ", userEntrance='" + userEntrance + '\'' +
                ", userFloor='" + userFloor + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                '}';
    }


    public User(int id, String userName, String userPhone, String userAddress, String userOffice, String userIntercom, String userEntrance, String userFloor, String latitude, String longitude) {
        this.id = id;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userAddress = userAddress;
        this.userOffice = userOffice;
        this.userIntercom = userIntercom;
        this.userEntrance = userEntrance;
        this.userFloor = userFloor;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
