package com.example.at_proto;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

public class Utils {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo!=null && networkInfo.isConnected();
    }

    public static boolean isLocationServicesEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        return gps_enabled || network_enabled;
    }

    public static void showLocationAlertDialog(final Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.location_alert_title)
                .setMessage(R.string.location_alert_body)
                .setPositiveButton(R.string.activate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.refuse, null)
                .show();
    }

    public static void showNetworkAlertDialog(final Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.network_alert_title)
                .setMessage(R.string.network_alert_body_block)
                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if(!isNetworkAvailable(context))
                            showNetworkAlertDialog(context);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public static LatLngBounds bboxFromFeatureCollection(FeatureCollection fc) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Feature f : fc.features()) {
            if(f.geometry().type().equals("Point")) {
                Point p = (Point)f.geometry();
                builder.include(new LatLng(p.latitude(), p.longitude()));
            }
        }
        return builder.build();
    }
}
