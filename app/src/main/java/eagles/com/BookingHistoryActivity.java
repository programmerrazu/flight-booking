package eagles.com;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import eagles.com.adapter.BookingAdapter;
import eagles.com.localdb.BookingDB;
import eagles.com.model.BookingInfo;
import es.dmoral.toasty.Toasty;

public class BookingHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_booking_history);
        setSupportActionBar(toolbar);

        RecyclerView rvBookingHistory = (RecyclerView) findViewById(R.id.recycler_view_booking_history);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvBookingHistory.setLayoutManager(layoutManager);
        rvBookingHistory.setHasFixedSize(true);
        rvBookingHistory.setNestedScrollingEnabled(false);

        File dbFilePath = getApplicationContext().getDatabasePath("db_booking_info");
        if (dbFilePath.exists()) {
            List<BookingInfo> bookingInfoList = getDBInstance().bookingDao().getBookingInfo();
            BookingAdapter bookingAdapter = new BookingAdapter(bookingInfoList);
            rvBookingHistory.setAdapter(bookingAdapter);
        } else {
            Toasty.error(this, "No Booking info", Toast.LENGTH_LONG, true).show();
        }
    }

    public BookingDB getDBInstance() {
        return Room.databaseBuilder(getApplicationContext(), BookingDB.class, "db_booking_info").allowMainThreadQueries().build();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(BookingHistoryActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        BookingHistoryActivity.this.startActivity(intent);
        BookingHistoryActivity.this.overridePendingTransition(0, 0);
        BookingHistoryActivity.this.finish();
    }
}