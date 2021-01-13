package com.example.at_proto;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.at_proto.CarnetVoyageRelated.CarnetVoyage;
import com.example.at_proto.LocationTracking.GeofencingPOI;
import com.example.at_proto.POIRelated.POI;
import com.example.at_proto.POIRelated.PopUpActivity;
import com.example.at_proto.POIRelated.QueryPOI;
import com.example.at_proto.RecommandationRelated.PostPOJO.SentProfile;
import com.example.at_proto.RecommandationRelated.PostPOJO.VisitPOI;
import com.example.at_proto.RecommandationRelated.PostUserInfo;
import com.example.at_proto.RecommandationRelated.ProfileMakerDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener, NavigationView.OnNavigationItemSelectedListener {

    private MapView mapview;
    private MapboxMap mapboxMap;
    private static final int EXTERNAL_STORAGE_READ_KEY = 4;

    private LocationEngine locationEngine;
    private boolean isTrackingActive = false;
    private int visitIndexTracking;
    public static Location currentLocation;
    private List<Location> locationTraceList;
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private MainActivityLocationCallback callback = new MainActivityLocationCallback(this);
    public static boolean isOnItinerary = false;

    public static final String CUSTOM_STYLE = "mapbox://styles/tcvpyr/cjxxaq86u01h91co60lko93vm";
    public static final String ACCESS_TOKEN = "pk.eyJ1IjoidGN2cHlyIiwiYSI6ImNqeHd6NzZ6NDA1eGYzbm11aWUwNzljeHcifQ.U7_ZPNSN3XKTZ9d67zwtWQ";

    private PermissionsManager permissionsManager;

    public final static int BITMAP_REQUEST_CODE = 1672;

    public final static String PROFILE_PATH = "profile.json";
    public static String USER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, ACCESS_TOKEN);
        setContentView(R.layout.activity_main);

        mapview = findViewById(R.id.mapView);
        mapview.onCreate(savedInstanceState);
        mapview.getMapAsync(this );

        checkExternalStorageReading();

        initButtons();
        GeofencingPOI.initGeofencing(this);
        checkProfile();

        if(!Utils.isLocationServicesEnabled(this))
            Utils.showLocationAlertDialog(this);

        //new PostUserInfo().postVisitPOI(new VisitPOI(USER_ID, "Ref" , 13, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime())));
    }

    /**
     * Une fois la carte prête à être affichée, on prépare l'interface utilisateur
     * et on attribue le style désiré à la carte.
     * Le listener de click sur la carte y est aussi attribué.
     * @param mapboxMap - Carte utilisée par l'activité
     */
    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap){
        this.mapboxMap = mapboxMap;
        mapboxMap.getUiSettings().setCompassMargins(0, 300, 30, 0);
        mapboxMap.getUiSettings().setLogoMargins(30, 0, 0, 150);
        mapboxMap.getUiSettings().setAttributionMargins(300, 0, 0, 150);
        Style.Builder bild = new Style.Builder();
        bild.fromUrl(CUSTOM_STYLE);
        mapboxMap.setStyle(bild, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
            }
        });

        mapboxMap.addOnMapClickListener(this);
    }

    /**
     * Le MapClick listener cherche la présence d'une feature POI au point touché par l'utilisateur.
     * Si c'est le cas, un popup est affiché avec le titre et la description du POI.
     * @param point - Le point (en coordonnées géographiques) que l'utilisateur a cliqué
     * @return Vrai si la recherche d'un POI a pu s'effectuer. Faux sinon.
     */
    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        Style style = mapboxMap.getStyle();

        if(style!=null){

            final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
            List<Feature> features;
            int featuresSize = 0;
            if(isOnItinerary) {
                features = mapboxMap.queryRenderedFeatures(pixel, "itiPOI");
                if(features!=null)
                    featuresSize = features.size();
                Log.d("PyrAT", "Features found: " + featuresSize);
                if(featuresSize>0){
                    String ref = features.get(0).getProperty("poi_to_visit").getAsJsonArray().get(0).getAsJsonObject().get("poi_id").getAsString();
                    Log.d("PyrAT", "POI ID is: " + ref);
                    if(ref!=null) {
                        Intent i = new Intent(this, PopUpActivity.class);
                        i.putExtra(PopUpActivity.POI_ID_EXTRA, ref);
                        startActivity(i);
                    }
                }
            }
            else {
                features = mapboxMap.queryRenderedFeatures(pixel, "poi-data");
                if(features!=null)
                    featuresSize = features.size();
                Log.d("PyrAT", "Features found: " + featuresSize);
                if(featuresSize>0){
                    Intent i = new Intent(this, PopUpActivity.class);
                    i.putExtra(PopUpActivity.POI_ID_EXTRA, features.get(0).getStringProperty(POI.ID_KEY));
                    startActivity(i);
                }
            }


            return true;
        }
        return false;
    }

    public void displayItinerary(InputStream geojson){

        Style style = mapboxMap.getStyle();

        if(style!=null & geojson!=null){
                Scanner scanner = new Scanner(geojson).useDelimiter("\\A");
                String geojsonString = scanner.next();
                FeatureCollection featureCollection = FeatureCollection.fromJson(geojsonString);

                style.addSource(new GeoJsonSource("itiSource", featureCollection));

                style.addLayer(new LineLayer("itiLine", "itiSource")
                        .withProperties(PropertyFactory.lineJoin(Property.LINE_JOIN_MITER),
                                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                                PropertyFactory.lineWidth(7f),
                                PropertyFactory.lineColor(Color.parseColor("#ff0000")))
                        .withFilter(Expression.has("start_stage")));

                style.addLayer(new SymbolLayer("itiPOI", "itiSource")
                        .withProperties(PropertyFactory.iconImage("dot-11"),
                                PropertyFactory.textField(Expression.concat(Expression.get("stage_number"),Expression.literal(". "),Expression.get("poi_name"))),
                                PropertyFactory.textAnchor(Property.TEXT_ANCHOR_BOTTOM),
                                PropertyFactory.textHaloColor("white"),
                                PropertyFactory.textHaloWidth(1f))
                        .withFilter(Expression.has("stage_number")));

                style.getLayer("poi-data").setProperties(PropertyFactory.visibility(Property.NONE));

                mapboxMap.getLocationComponent().setCameraMode(CameraMode.NONE);
                mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(Utils.bboxFromFeatureCollection(featureCollection), 500), 2000);
                isOnItinerary = true;
        }
    }

    @TargetApi(23)
    public void checkExternalStorageReading() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_READ_KEY);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode!=EXTERNAL_STORAGE_READ_KEY)
            permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            if (mapboxMap.getStyle() != null) {
                enableLocationComponent(mapboxMap.getStyle());
            }
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @SuppressLint("MissingPermission")
    private void initLocationEngine(){
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS).setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY).setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();
        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);

    }

    @SuppressWarnings("MissingPermission")
    private void enableLocationComponent(@NonNull Style loadedMapStyle){

        if(PermissionsManager.areLocationPermissionsGranted(this)) {
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            LocationComponentActivationOptions locationComponentActivationOptions = LocationComponentActivationOptions.builder(this, loadedMapStyle).useDefaultLocationEngine(false).build();
            locationComponent.activateLocationComponent(locationComponentActivationOptions);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();
        }
        else{
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapview.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapview.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapview.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapview.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(locationEngine!=null){
            locationEngine.removeLocationUpdates(callback);
        }
        mapview.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapview.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapview.onStart();
    }

    @Override
    public void onBackPressed() {
        if(!isOnItinerary) {
            new PostUserInfo().sendStoredPOJOs();
            super.onBackPressed();
        }
        else {
            isOnItinerary = false;
            Style style = mapboxMap.getStyle();
            if(style!=null) {
                style.removeLayer("itiPOI");
                style.removeLayer("itiLine");
                style.removeSource("itiSource");
                style.getLayer("poi-data").setProperties(PropertyFactory.visibility(Property.VISIBLE));
            }

            if(currentLocation!=null) {
                mapboxMap.easeCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())), 2000);
            }
        }
    }

    private void initButtons(){

        final ImageButton buttonBurger = (ImageButton) findViewById(R.id.buttonBurger);
        final ImageButton button3 = (ImageButton)findViewById(R.id.button3);
        final NavigationView nv = (NavigationView)findViewById(R.id.navigationView);
        final SearchView sv = (SearchView)findViewById(R.id.mapSearchView);
        final Context context = this.getApplicationContext();

        buttonBurger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isTrackingActive){
                    button3.setImageResource(R.drawable.ic_stop_black_24dp);
                    enableTracking();
                }
                else{
                    button3.setImageResource(R.drawable.ic_fiber_manual_record_black_24dp);
                    disableTracking();
                }
            }
        });
        nv.setNavigationItemSelectedListener(this);

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                geocodeFromName(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    public void geocodeFromName(String name){

        try {
            MapboxGeocoding client = MapboxGeocoding.builder()
                    .accessToken(ACCESS_TOKEN)
                    .country(Locale.FRANCE)
                    .query(name)
                    .autocomplete(true)
                    .limit(1)
                    .build();


            client.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                    List<CarmenFeature> results = response.body().features();

                    if (results.size() > 0) {
                        Point point = results.get(0).center();
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                                                                                            .target(new LatLng(point.latitude(), point.longitude()))
                                                                                            .zoom(13)
                                                                                            .build()), 1300);
                    }
                }

                @Override
                public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }
        catch(ServicesException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        int id = menuItem.getItemId();
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        switch(id){
            case R.id.menu_itineraire:
                startActivityForResult(new Intent(this, ItHubActivity.class), ItHubActivity.ITINERARY_GAVE);
                break;
            case R.id.menu_navigation_libre:
                break;
            case R.id.menu_recommandations:
                startActivity(new Intent(this, RecommandationActivity.class));
                break;
            case R.id.menu_aide:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.menu_aide_string)
                        .setMessage(R.string.aide_message)
                        .setPositiveButton(R.string.ok, null)
                        .show();
                break;

            case R.id.menu_credits:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.menu_credits_string)
                        .setMessage(R.string.credits_message)
                        .setPositiveButton(R.string.ok, null)
                        .show();
                break;
            case R.id.menu_carnet_voyage:
                startActivity(new Intent(this, CarnetActivity.class));
                break;
            default:
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==ItHubActivity.ITINERARY_GAVE && resultCode==1) {

            if(!isOnItinerary) {
                String file = data.getStringExtra(ItHubActivity.ITINERARY_STRING_KEY);
                if (file.equals("Itinéraire exemple")) {
                    try {
                        displayItinerary(this.getAssets().open("itinerary_example.json"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        File folder = this.getDir("itineraries", MODE_PRIVATE);
                        File it = new File(folder, file);
                        displayItinerary(new FileInputStream(it));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Vérifie la présence d'un profil utilisateur, normalement inexistant lors de la première exécution de l'app.
     *
     * <p>
     *     Si le profil utilisateur était déjà créé et stocké dans le fichier .json correspondant, on instancie l'USER_ID pour son utilisation future.
     * </p>
     * <p>
     *     Si le profil utilisateur est inexistant, on créé un nouveau profil en demandant à l'utilisateur les informations nécessaires et en générant un nouvel UUID.
     *     On enregistre ensuite ce profil dans un fichier .json et on l'envoi côté serveur.
     * </p>
     */
    private void checkProfile(){

        try{
            InputStreamReader isr = new InputStreamReader(openFileInput(PROFILE_PATH));
            SentProfile sentProfile = SentProfile.fromJSON(isr);
            Log.d("PyrAT", "User id: " + sentProfile.getUser_id());
            USER_ID = sentProfile.getUser_id();
            isr.close();
        }
        catch(IOException e) {
            e.printStackTrace();
            Log.d("PyrAT", "Creating new user..");

            ProfileMakerDialog.OnProfileListener profileListener = new ProfileMakerDialog.OnProfileListener() {
                @Override
                public void onProfileSet(View v, SentProfile sp) {
                        USER_ID = sp.getUser_id();
                        new PostUserInfo().postNewUser(sp, MainActivity.this);
                }
            };
            ProfileMakerDialog pmd = new ProfileMakerDialog(this, profileListener);
            pmd.show();
        }
    }

    private void enableTracking(){
        isTrackingActive = true;
        locationTraceList = new ArrayList<>();
        visitIndexTracking = PostUserInfo.getSentVisitPOI().size();

        if(currentLocation!=null){
            locationTraceList.add(currentLocation);
            Log.d("LocationTracking", "Location tracking has been launched at " + currentLocation.getTime() + " to [" + currentLocation.getLatitude() +", " +currentLocation.getLongitude() + "]");
        }
    }

    private void disableTracking(){
        isTrackingActive = false;
        if(locationTraceList.size()>2) {

            CarnetVoyage cdv;
            if(visitIndexTracking<PostUserInfo.getSentVisitPOI().size())
                cdv = new CarnetVoyage(locationTraceList, PostUserInfo.getSentVisitPOI().subList(visitIndexTracking+1, PostUserInfo.getSentVisitPOI().size()));
            else {
                List<VisitPOI> poi = new ArrayList<>();
                poi.add(new VisitPOI(this.USER_ID, "ID POI 1", 10, "DATE VISITE 1"));
                poi.add(new VisitPOI(this.USER_ID, "ID POI 2", 15, "DATE VISITE 2"));
                cdv = new CarnetVoyage(locationTraceList, poi);
            }

            try {
                File file = new File(getDir(CarnetActivity.CARNET_DIRECTORY, MODE_PRIVATE), "Voyage du " + new SimpleDateFormat("yyyy_MM_dd HH_mm").format(Calendar.getInstance().getTime()));
                Log.d("PyrAT", "Path is : " + file.getAbsolutePath() + " / " + file.getCanonicalPath());
                boolean created = file.createNewFile();
                Log.d("PyrAT", "File is created: " + created);
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                Log.d("PyrAT", "JSON CDV is: " + new Gson().toJson(cdv, CarnetVoyage.class));
                writer.write(new GsonBuilder().serializeNulls().create().toJson(cdv, CarnetVoyage.class));
                writer.close();
            } catch (IOException e) {
                Log.e("PyrAT", "Could not save CarnetVoyage: ", e);
            }
        }
        else
            Toast.makeText(this, "Le parcours est trop court pour être enregistré.", Toast.LENGTH_SHORT).show();
    }

    private void addTrace(Location location){
        if(isTrackingActive && location!=null){
            locationTraceList.add(location);
            Log.d("LocationTracking", "Location trace added [" + location.getLatitude() + "," + location.getLongitude() + " at " + location.getTime());
        }
    }

    private static class MainActivityLocationCallback implements LocationEngineCallback<LocationEngineResult>{

        private final WeakReference<MainActivity> activityWeakReference;

        MainActivityLocationCallback(MainActivity activity){
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            final MainActivity activity = activityWeakReference.get();
            final GeofencingPOI geofencingPOI = GeofencingPOI.getInstance();

            if(activity!=null){
                Location location = result.getLastLocation();
                MainActivity.currentLocation = location;

                activity.addTrace(location);

                if(location!=null) {
                    if (activity.mapboxMap != null) {
                        activity.mapboxMap.getLocationComponent().forceLocationUpdate(location);
                    }
                    geofencingPOI.update(location);
                    activity.mapboxMap.getStyle().getLayer("poi-data").setProperties(PropertyFactory.iconImage(Expression.match(Expression.get(POI.ID_KEY),
                                                                                                                    new Expression.ExpressionLiteral(geofencingPOI.getIDArray()), Expression.literal("rectangle-green-2"),
                                                                                                                    Expression.literal("rectangle-red-2"))));
                }

            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d("LocationChangeActivity", exception.getLocalizedMessage());
            MainActivity activity = activityWeakReference.get();
            if(activity!=null){
                Toast.makeText(activity, exception.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
