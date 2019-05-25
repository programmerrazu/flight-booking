package eagles.com.localdb;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import eagles.com.model.BookingInfo;

@Database(entities = BookingInfo.class, version = 1)
public abstract class BookingDB extends RoomDatabase {

    public abstract BookingDao bookingDao();
}