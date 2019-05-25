package eagles.com.localdb;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import eagles.com.model.BookingInfo;

@Dao
public interface BookingDao {

    @Insert
    public long insertBooking(BookingInfo bookingInfo);

    @Query("SELECT * FROM booking_info")
    public List<BookingInfo> getBookingInfo();
}