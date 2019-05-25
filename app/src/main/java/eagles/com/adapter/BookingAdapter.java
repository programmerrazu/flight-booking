package eagles.com.adapter;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import eagles.com.R;
import eagles.com.model.BookingInfo;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingHolder> {

    private List<BookingInfo> bookingInfoList;

    public BookingAdapter(List<BookingInfo> bookingInfoList) {
        this.bookingInfoList = bookingInfoList;
    }

    @NonNull
    @Override
    public BookingHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.booking_history_adapter, viewGroup, false);
        return new BookingHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BookingHolder holder, int position) {
        if (bookingInfoList.size() != 0) {
            holder.tvPickupPlaces.setText(bookingInfoList.get(position).getPickupPlace());
            holder.tvDestPlaces.setText(bookingInfoList.get(position).getDestPlace());
            holder.tvDistances.setText("Distance " + bookingInfoList.get(position).getDistance() + " km");
            holder.tvDurations.setText("Durations " + bookingInfoList.get(position).getDuration() + " minutes");
        }
    }

    @Override
    public int getItemCount() {
        return bookingInfoList.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull BookingHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull BookingHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    class BookingHolder extends RecyclerView.ViewHolder {

        private TextView tvPickupPlaces, tvDestPlaces, tvDistances, tvDurations;

        BookingHolder(@NonNull View itemView) {
            super(itemView);
            tvPickupPlaces = itemView.findViewById(R.id.tv_pickup_places);
            tvDestPlaces = itemView.findViewById(R.id.tv_dest_places);
            tvDistances = itemView.findViewById(R.id.tv_distances);
            tvDurations = itemView.findViewById(R.id.tv_durations);
        }
    }
}