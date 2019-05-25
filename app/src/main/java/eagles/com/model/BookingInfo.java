package eagles.com.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "booking_info")
public class BookingInfo {

    @ColumnInfo(name = "booking_id")
    @PrimaryKey(autoGenerate = true)
    private int bookingId;

    @ColumnInfo(name = "pickup_place")
    private String pickupPlace;

    @ColumnInfo(name = "dest_place")
    private String destPlace;

    @ColumnInfo(name = "distance")
    private String distance;

    @ColumnInfo(name = "duration")
    private String duration;

    @ColumnInfo(name = "insert_date")
    private String insertDate;

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getPickupPlace() {
        return pickupPlace;
    }

    public void setPickupPlace(String pickupPlace) {
        this.pickupPlace = pickupPlace;
    }

    public String getDestPlace() {
        return destPlace;
    }

    public void setDestPlace(String destPlace) {
        this.destPlace = destPlace;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(String insertDate) {
        this.insertDate = insertDate;
    }
}