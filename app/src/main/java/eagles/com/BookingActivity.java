package eagles.com;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import eagles.com.localdb.BookingDB;
import eagles.com.model.BookingInfo;
import es.dmoral.toasty.Toasty;

public class BookingActivity extends AppCompatActivity {

    private String pickupPlace, destPlace, distance, duration;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_booking_info);
        setSupportActionBar(toolbar);

        TextView tvPickupPlace = (TextView) findViewById(R.id.tv_pickup_place);
        TextView tvDestPlace = (TextView) findViewById(R.id.tv_dest_place);
        TextView tvDistance = (TextView) findViewById(R.id.tv_distance);
        TextView tvDuration = (TextView) findViewById(R.id.tv_duration);

        if (getIntent() != null) {
            String status = getIntent().getStringExtra("status");
            if (status != null) {
                if (status.equalsIgnoreCase("booking")) {
                    pickupPlace = getIntent().getStringExtra("pickup_place");
                    destPlace = getIntent().getStringExtra("dest_place");
                    distance = getIntent().getStringExtra("distance");
                    duration = getIntent().getStringExtra("duration");
                    if (distance == null) {
                        distance = "0";
                    }
                    if (duration == null) {
                        duration = "0";
                    }
                    tvPickupPlace.setText(pickupPlace);
                    tvDestPlace.setText(destPlace);
                    tvDistance.setText("Distance " + distance + " km");
                    tvDuration.setText("Duration " + duration + " minutes");
                }
            }
        }
    }

    public BookingDB getDBInstance() {
        return Room.databaseBuilder(getApplicationContext(), BookingDB.class, "db_booking_info").allowMainThreadQueries().build();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(BookingActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        BookingActivity.this.startActivity(intent);
        BookingActivity.this.overridePendingTransition(0, 0);
        BookingActivity.this.finish();
    }

    public void onBookingSubmit(View view) {
        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setPickupPlace(pickupPlace);
        bookingInfo.setDestPlace(destPlace);
        bookingInfo.setDistance(distance);
        bookingInfo.setDuration(duration);
        long status = getDBInstance().bookingDao().insertBooking(bookingInfo);
        if (status < 1) {
            Toasty.error(this, "Booking submit failed", Toast.LENGTH_LONG, true).show();
        } else {
            Toasty.success(this, "Booking submit successful", Toast.LENGTH_LONG, true).show();
            Intent intent = new Intent(BookingActivity.this, BookingHistoryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            BookingActivity.this.startActivity(intent);
            BookingActivity.this.overridePendingTransition(0, 0);
            BookingActivity.this.finish();
        }
    }
}