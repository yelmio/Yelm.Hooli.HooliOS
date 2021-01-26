package yelm.io.yelm.database_new.user_addresses;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName="UserAddresses")
public class UserAddress {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    public int id;

    @ColumnInfo(name="latitude")
    public String latitude;

    @ColumnInfo(name="longitude")
    public String longitude;

    @ColumnInfo(name="address")
    public String address;

    @ColumnInfo(name="isChecked")
    public boolean isChecked;

    @Override
    public String toString() {
        return "UserAddress{" +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", address='" + address + '\'' +
                ", isChecked=" + isChecked +
                '}';
    }

    public UserAddress(String latitude, String longitude, String address, boolean isChecked) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.isChecked = isChecked;
    }

}
