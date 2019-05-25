package eagles.com.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import eagles.com.R;

public class PlaceAutocompleteAdapter extends RecyclerView.Adapter<PlaceAutocompleteAdapter.PlaceViewHolder> implements Filterable {

    public interface PlaceAutoCompleteInterface {
        void onPlaceClick(ArrayList<PlaceAutocomplete> mResultList, int position);
    }

    private Context context;
    private PlaceAutoCompleteInterface pacListener;
    private ArrayList<PlaceAutocomplete> placeAutocompleteList;
    private GoogleApiClient googleApiClient;
    private LatLngBounds bounds;
    private int layoutResource;
    private AutocompleteFilter autocompleteFilter;

    public PlaceAutocompleteAdapter(Context context, int layoutResource, GoogleApiClient googleApiClient, LatLngBounds bounds, AutocompleteFilter autocompleteFilter) {
        this.context = context;
        this.layoutResource = layoutResource;
        this.googleApiClient = googleApiClient;
        this.bounds = bounds;
        this.autocompleteFilter = autocompleteFilter;
        this.pacListener = (PlaceAutoCompleteInterface) context;
    }

    public void clearList() {
        if (placeAutocompleteList != null && placeAutocompleteList.size() > 0) {
            placeAutocompleteList.clear();
        }
    }

    public void setBounds(LatLngBounds latLngBounds) {
        bounds = latLngBounds;
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = layoutInflater.inflate(layoutResource, parent, false);
        PlaceViewHolder mPredictionHolder = new PlaceViewHolder(convertView);
        return mPredictionHolder;
    }

    @Override
    public void onBindViewHolder(PlaceViewHolder holder, final int position) {
        holder.tvPlaceDescriptions.setText(placeAutocompleteList.get(position).description);
        holder.llRowPlaceName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pacListener.onPlaceClick(placeAutocompleteList, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (placeAutocompleteList != null)
            return placeAutocompleteList.size();
        else {
            return 0;
        }
    }

    public PlaceAutocomplete getItem(int position) {
        return placeAutocompleteList.get(position);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                // Skip the autocomplete query if no constraints are given.
                if (constraint != null) {
                    // Query the autocomplete API for the (constraint) search string.
                    placeAutocompleteList = getAutocomplete(constraint);
                    if (placeAutocompleteList != null) {
                        // The API successfully returned results.
                        results.values = placeAutocompleteList;
                        results.count = placeAutocompleteList.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    notifyDataSetChanged();
                } else {
                    // The API did not return any results, invalidate the data set.
                    //notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    private ArrayList<PlaceAutocomplete> getAutocomplete(CharSequence constraint) {
        if (googleApiClient.isConnected()) {
            Log.i("", "Starting autocomplete query for: " + constraint);
            // Submit the query to the autocomplete API and retrieve a PendingResult that will
            // contain the results when the query completes.
            PendingResult<AutocompletePredictionBuffer> results = Places.GeoDataApi.getAutocompletePredictions(googleApiClient,
                    constraint.toString(), bounds, autocompleteFilter);
            // This method should have been called off the main UI thread. Block and wait for at most 60s
            // for a result from the API.
            AutocompletePredictionBuffer autocompletePredictions = results.await(60, TimeUnit.SECONDS);
            // Confirm that the query completed successfully, otherwise return null
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
//                Toast.makeText(mContext, "Error contacting API: " + status.toString(),
//                Toast.LENGTH_SHORT).show();
                Log.e("", "Error getting autocomplete prediction API call: " + status.toString());
                autocompletePredictions.release();
                return null;
            }
            Log.i("", "Query completed. Received " + autocompletePredictions.getCount() + " predictions.");
            // Copy the results into our own data structure, because we can't hold onto the buffer.
            // AutocompletePrediction objects encapsulate the API response (place ID and description).
            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            ArrayList resultList = new ArrayList<>(autocompletePredictions.getCount());
            while (iterator.hasNext()) {
                AutocompletePrediction prediction = iterator.next();
                // Get the details of this prediction and copy it into a new PlaceAutocomplete object.
                resultList.add(new PlaceAutocomplete(prediction.getPlaceId(), prediction.getFullText(null)));
            }
            // Release the buffer now that all data has been copied.
            autocompletePredictions.release();
            return resultList;
        }
        Log.e("", "Google API client is not connected for autocomplete query.");
        return null;
    }


    public class PlaceViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout llRowPlaceName;
        public TextView tvPlaceDescriptions;

        public PlaceViewHolder(View itemView) {
            super(itemView);
            llRowPlaceName = (LinearLayout) itemView.findViewById(R.id.ll_row_place_name);
            tvPlaceDescriptions = (TextView) itemView.findViewById(R.id.tv_place_descriptions);
        }
    }

    public class PlaceAutocomplete {

        public CharSequence placeId;
        public CharSequence description;

        PlaceAutocomplete(CharSequence placeId, CharSequence description) {
            this.placeId = placeId;
            this.description = description;
        }

        @Override
        public String toString() {
            return description.toString();
        }
    }
}