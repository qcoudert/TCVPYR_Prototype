package com.example.at_proto.ItineraryRelated;

import android.location.Location;

import com.example.at_proto.LocationTracking.GeofencingPOI;
import com.example.at_proto.MainActivity;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.api.tilequery.MapboxTilequery;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;

import java.util.Locale;

import retrofit2.Callback;

public class GeocodeUtil {

    public static void geocodeLocationToCity(Location location, int limit, Callback<GeocodingResponse> callback) {

        MapboxGeocoding client = MapboxGeocoding.builder()
                .accessToken(MainActivity.ACCESS_TOKEN)
                .country(Locale.FRANCE)
                .geocodingTypes(GeocodingCriteria.TYPE_PLACE)
                .query(Point.fromLngLat(location.getLongitude(), location.getLatitude()))
                .limit(limit)
                .build();

        client.enqueueCall(callback);
    }

    public static void geocodeName(String name, int limit, Callback<GeocodingResponse> callback) {

        MapboxGeocoding client = MapboxGeocoding.builder()
                .accessToken(MainActivity.ACCESS_TOKEN)
                .country(Locale.FRANCE)
                .query(name)
                .autocomplete(true)
                .limit(limit)
                .build();

        client.enqueueCall(callback);
    }

    public static void tileQueryLocation(Location location, Callback<FeatureCollection> callback){
        MapboxTilequery tilequery = MapboxTilequery.builder()
                .accessToken(MainActivity.ACCESS_TOKEN)
                .mapIds(GeofencingPOI.TILESET_ID)
                .layers("poi_data")
                .query(Point.fromLngLat(location.getLongitude(), location.getLatitude()))
                .radius(50)
                .limit(20)
                .build();

        tilequery.enqueueCall(callback);
    }
}
