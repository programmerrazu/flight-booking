package eagles.com.helpers;

import android.app.ProgressDialog;
import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import eagles.com.R;

public class Commons {

    private static final String baseUrl = "https://maps.googleapis.com";

    public static GoogleAPI getGoogleAPI() {
        return RetrofitClient.getClient(baseUrl).create(GoogleAPI.class);
    }

    public static void startWaitingDialog(ProgressDialog pDialog) {
        pDialog.setMessage("Please waiting...");
        pDialog.setCancelable(true);
        pDialog.show();
    }

    public static void stopWaitingDialog(ProgressDialog pDialog) {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    public static String directionsApi(LatLng startPoint, LatLng endPoint, Context context) {
        return "https://maps.googleapis.com/maps/api/directions/json?" +
                "mode=driving&" +
                "transit_routing_preference=less_driving&" +
                "origin=" + startPoint.latitude + "," + startPoint.longitude + "&" +
                "destination=" + endPoint.latitude + "," + endPoint.longitude + "&" +
                "key=" + context.getResources().getString(R.string.google_direction_api_key);
    }

    public static List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}