package eagles.com;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import eagles.com.adapter.PlaceAutocompleteAdapter;
import eagles.com.helpers.Commons;
import eagles.com.helpers.GoogleAPI;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        PlaceAutocompleteAdapter.PlaceAutoCompleteInterface,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap bookingMap;
    private Boolean mLocationPermissionGranted, isPlacesCount = false;
    private LatLng pickupLatLng, destLatLng;
    private String pickupPlace, destPlace;
    private static final float MAP_ZOOM = 14.0f;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private CardView cardViewSearchContainer;
    private LinearLayout llLocationSearchContainer, llBookingContainer;
    private EditText etDestSearch, etPickupSearch;
    private static int pcs = 0, etClickNo = 0;
    private PlaceAutocompleteAdapter mAdapter;
    private InputMethodManager imm;
    private RecyclerView mRecyclerView;
    private GoogleApiClient mGoogleApiClient;
    private static final LatLngBounds BOUNDS_BANGLADESH = new LatLngBounds(new LatLng(-0, 0), new LatLng(0, 0));
    private GoogleAPI mService;
    private String distance, duration;
    private ProgressDialog pDialog;
    private List<LatLng> polyLineList;
    private CircleImageView civBookingHistory;
    private SupportMapFragment bookingMapsFragment;
    private View btnLocation;
    private FloatingActionButton fabBtnLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUIComponents();

        bookingMapsFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_booking_map);
        assert bookingMapsFragment != null;
        bookingMapsFragment.getMapAsync(this);
    }

    private void setUIComponents() {

        fabBtnLocation = (FloatingActionButton) findViewById(R.id.fab_btn_location);
        civBookingHistory = (CircleImageView) findViewById(R.id.civ_booking_history);
        civBookingHistory.setImageResource(R.drawable.ic_service);
        civBookingHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BookingHistoryActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                MainActivity.this.startActivity(intent);
                MainActivity.this.overridePendingTransition(0, 0);
                MainActivity.this.finish();
            }
        });

        pDialog = new ProgressDialog(this);
        mService = Commons.getGoogleAPI();
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        llLocationSearchContainer = (LinearLayout) findViewById(R.id.ll_location_search_container);
        cardViewSearchContainer = (CardView) findViewById(R.id.card_view_search_container);
        etDestSearch = (EditText) findViewById(R.id.et_dest_search);
        etPickupSearch = (EditText) findViewById(R.id.et_pickup_search);
        final ImageView ivPickupClear = (ImageView) findViewById(R.id.iv_pickup_clear);
        final ImageView ivDestClear = (ImageView) findViewById(R.id.iv_dest_clear);
        final LinearLayout llPickupContainer = (LinearLayout) findViewById(R.id.ll_pickup_container);
        final LinearLayout llDestContainer = (LinearLayout) findViewById(R.id.ll_dest_container);
        llBookingContainer = (LinearLayout) findViewById(R.id.ll_booking_container);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_place_list);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(llm);

        mAdapter = new PlaceAutocompleteAdapter(this, R.layout.view_place_search, mGoogleApiClient, BOUNDS_BANGLADESH, null);

        cardViewSearchContainer.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                if (mAdapter != null) {
                    mAdapter.clearList();
                }
                cardViewSearchContainer.setVisibility(View.GONE);
                civBookingHistory.setVisibility(View.GONE);
                fabBtnLocation.setVisibility(View.GONE);
                llLocationSearchContainer.setVisibility(View.VISIBLE);
                etDestSearch.requestFocus();
                showKeyboard();
            }
        });

        ivPickupClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etPickupSearch.setText(null);
            }
        });

        ivDestClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etDestSearch.setText(null);
            }
        });

        etPickupSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pcs = 1;
                ivPickupClear.setVisibility(View.VISIBLE);
            }
        });

        etPickupSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean status) {
                etClickNo = 1;
                if (!isPlacesCount) {
                    etPickupSearch.setText(pickupPlace);
                }
                if (status) {
                    ivDestClear.setVisibility(View.GONE);
                    etDestSearch.getText().clear();
                    llDestContainer.setBackgroundColor(Color.parseColor("#FFF9F6F9"));
                    llPickupContainer.setBackgroundColor(Color.parseColor("#E7ECED"));
                } else {
                    isPlacesCount = false;
                }
            }
        });

        etPickupSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!isNetworkAvailable()) {
                    Toasty.warning(MainActivity.this, "Please check network connection", Toast.LENGTH_SHORT, true).show();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
                    if (pcs == 1) {
                        ivPickupClear.setVisibility(View.VISIBLE);
                    }
                    if (mAdapter != null) {
                        mRecyclerView.setAdapter(mAdapter);
                    }
                } else {
                    ivPickupClear.setVisibility(View.GONE);
                }
                if (!s.toString().equals("") && mGoogleApiClient.isConnected()) {
                    mAdapter.getFilter().filter(s.toString());
                } else if (!mGoogleApiClient.isConnected()) {
                    Log.e("Main", "NOT CONNECTED");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etDestSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean status) {
                if (status) {
                    etClickNo = 2;
                    ivPickupClear.setVisibility(View.GONE);
                    llPickupContainer.setBackgroundColor(Color.parseColor("#FFF9F6F9"));
                    llDestContainer.setBackgroundColor(Color.parseColor("#E7ECED"));
                }
            }
        });

        etDestSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                ivPickupClear.setVisibility(View.GONE);
                llDestContainer.setBackgroundColor(Color.parseColor("#E7ECED"));
                llPickupContainer.setBackgroundColor(Color.parseColor("#FFF9F6F9"));
                if (!isNetworkAvailable()) {
                    Toasty.warning(MainActivity.this, "Please check network connection", Toast.LENGTH_SHORT, true).show();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
                    ivDestClear.setVisibility(View.VISIBLE);
                    if (mAdapter != null) {
                        mRecyclerView.setAdapter(mAdapter);
                    }
                } else {
                    ivDestClear.setVisibility(View.GONE);
                }
                if (!s.toString().equals("") && mGoogleApiClient.isConnected()) {
                    mAdapter.getFilter().filter(s.toString());
                } else if (!mGoogleApiClient.isConnected()) {
                    Log.e("Main", "NOT CONNECTED");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void getPickupAddress() {
        try {
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addressList = geocoder.getFromLocation(pickupLatLng.latitude, pickupLatLng.longitude, 1);
            Address address = addressList.get(0);
            pickupPlace = address.getAddressLine(0);
            etPickupSearch.setText(pickupPlace);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideKeyboard() {
        if (imm != null) {
            imm.hideSoftInputFromWindow(etDestSearch.getWindowToken(), 0);
        }
    }

    private void showKeyboard() {
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        bookingMap = googleMap;
        bookingMap.setTrafficEnabled(false);
        bookingMap.setIndoorEnabled(false);
        bookingMap.setBuildingsEnabled(false);
        bookingMap.getUiSettings().setZoomControlsEnabled(false);

        getLocationPermission();
        getLocations();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void getLocations() {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    pickupLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    bookingMap.addMarker(new MarkerOptions().position(pickupLatLng).title("Current Location"));
                    bookingMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLng, MAP_ZOOM));
                    bookingMap.setMyLocationEnabled(true);
                    customiseBtnLocation();
                    getPickupAddress();
                }
            }
        });
    }

    private void customiseBtnLocation() {
        if (bookingMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                btnLocation = ((View) bookingMapsFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                if (btnLocation != null) {
                    btnLocation.setVisibility(View.GONE);
                }
                fabBtnLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (bookingMap != null) {
                            if (btnLocation != null) {
                                btnLocation.callOnClick();
                            }
                        }
                    }
                });
            } else {
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onPlaceClick(ArrayList<PlaceAutocompleteAdapter.PlaceAutocomplete> mResultList, int position) {
        if (mResultList != null) {
            try {
                final String placeId = String.valueOf(mResultList.get(position).placeId);
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getCount() == 1) {
                            Intent data = new Intent();
                            data.putExtra("latitude", String.valueOf(places.get(0).getLatLng().latitude));
                            data.putExtra("longitude", String.valueOf(places.get(0).getLatLng().longitude));
                            setResult(MainActivity.RESULT_OK, data);
                            if (etClickNo == 1) {
                                isPlacesCount = true;
                                pickupLatLng = places.get(0).getLatLng();
                                pickupPlace = places.get(0).getAddress().toString();
                                etPickupSearch.setText(pickupPlace);
                                etClickNo = 2;
                                if (mAdapter != null) {
                                    mAdapter.clearList();
                                }
                                etDestSearch.requestFocus();
                            } else if (etClickNo == 2) {
                                destLatLng = places.get(0).getLatLng();
                                destPlace = places.get(0).getAddress().toString();
                                getDestinationRoute();
                            }
                        } else {
                            Toasty.warning(MainActivity.this, "something went wrong", Toast.LENGTH_LONG, true).show();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getDestinationRoute() {

        Commons.startWaitingDialog(pDialog);
        etClickNo = 0;
        llLocationSearchContainer.setVisibility(View.GONE);
        if (mAdapter != null) {
            mAdapter.clearList();
        }
        hideKeyboard();
        bookingMap.addMarker(new MarkerOptions().position(destLatLng).title("Destination"));
        bookingMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destLatLng, MAP_ZOOM));
        llBookingContainer.setVisibility(View.VISIBLE);

        try {
            mService.getPath(Commons.directionsApi(pickupLatLng, destLatLng, this)).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body());
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyLine = poly.getString("points");
                            polyLineList = Commons.decodePoly(polyLine);
                        }
                        JSONObject object = jsonArray.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");
                        JSONObject legsObject = legs.getJSONObject(0);

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng latLng : polyLineList) {
                            builder.include(latLng);
                        }
                        LatLngBounds bounds = builder.build();
                        CameraUpdate destCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50);
                        bookingMap.animateCamera(destCameraUpdate, new GoogleMap.CancelableCallback() {
                            @Override
                            public void onCancel() {

                            }

                            @Override
                            public void onFinish() {
                                CameraUpdate zOut = CameraUpdateFactory.zoomBy(-1.0f);
                                bookingMap.animateCamera(zOut);
                            }
                        });

                        JSONObject distanceObj = legsObject.getJSONObject("distance");
                        String distanceText = distanceObj.getString("text");
                        distance = distanceText.replaceAll("[^0-9\\\\.]+", "");

                        JSONObject durationObj = legsObject.getJSONObject("duration");
                        String durationText = durationObj.getString("text");
                        duration = durationText.replaceAll("[^0-9\\\\.]+", "");

                        Commons.stopWaitingDialog(pDialog);
                    } catch (Exception e) {
                        Commons.stopWaitingDialog(pDialog);
                        Toasty.warning(MainActivity.this, "No Points", Toast.LENGTH_LONG, true).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Commons.stopWaitingDialog(pDialog);
                    Toasty.warning(MainActivity.this, "No Points", Toast.LENGTH_LONG, true).show();
                }
            });
        } catch (Exception e) {
            Commons.stopWaitingDialog(pDialog);
            Toasty.warning(MainActivity.this, "No Points", Toast.LENGTH_LONG, true).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void onBooking(View view) {
        Intent intent = new Intent(MainActivity.this, BookingActivity.class);
        intent.putExtra("status", "booking");
        intent.putExtra("pickup_place", pickupPlace);
        intent.putExtra("dest_place", destPlace);
        intent.putExtra("distance", distance);
        intent.putExtra("duration", duration);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        MainActivity.this.startActivity(intent);
        MainActivity.this.overridePendingTransition(0, 0);
        MainActivity.this.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted) {
                        mLocationPermissionGranted = true;
                        getLocations();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel(
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                                                }
                                            }
                                        });
                            }
                        }
                    }
                }
        }
    }

    private void showMessageOKCancel(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("You need to allow location access permission")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBackPressed() {
        if (llLocationSearchContainer.getVisibility() == View.VISIBLE) {
            llLocationSearchContainer.setVisibility(View.GONE);
            cardViewSearchContainer.setVisibility(View.VISIBLE);
            civBookingHistory.setVisibility(View.VISIBLE);
            fabBtnLocation.setVisibility(View.VISIBLE);
        } else if (llBookingContainer.getVisibility() == View.VISIBLE) {
            llBookingContainer.setVisibility(View.GONE);
            cardViewSearchContainer.setVisibility(View.VISIBLE);
            civBookingHistory.setVisibility(View.VISIBLE);
            fabBtnLocation.setVisibility(View.VISIBLE);
        } else {
            moveTaskToBack(true);
        }
    }
}