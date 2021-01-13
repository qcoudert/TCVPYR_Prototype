package com.example.at_proto.CarnetVoyageRelated;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.at_proto.CarnetActivity;
import com.example.at_proto.MainActivity;
import com.example.at_proto.R;
import com.example.at_proto.RecommandationRelated.PostPOJO.VisitPOI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DisplayCarnetActivity extends AppCompatActivity {

    private CarnetVoyage carnetVoyage;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_carnet);

        String file = getIntent().getStringExtra(CarnetActivity.CARNET_KEY);
        try {
            File folder = this.getDir(CarnetActivity.CARNET_DIRECTORY, MODE_PRIVATE);
            File it = new File(folder, file);
            Log.d("PyrAT", "File content: " + it.exists());
            carnetVoyage = new Gson().fromJson(new FileReader(it), CarnetVoyage.class);
        } catch (IOException e) {
            Log.e("PyrAT", "Error when attempting to read CarnetVoyage: ", e);
            finish();
        }

        Mapbox.getInstance(this, MainActivity.ACCESS_TOKEN);

        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        MapboxMapOptions options = new MapboxMapOptions();
        options.camera(new CameraPosition.Builder()
                .target(new LatLng(42.821, 0.723))
                .zoom(4.5)
                .build());

        mapFragment = SupportMapFragment.newInstance(options);

        transaction.add(R.id.carnet_voyage_map_container, mapFragment, "com.mapbox.map");
        transaction.commit();

        FeatureCollection fc = FeatureCollection.fromFeature(Feature.fromGeometry(carnetVoyage.getUserPath()));
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {

                mapboxMap.setStyle(new Style.Builder().fromUrl(MainActivity.CUSTOM_STYLE), new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        Log.d("PyrAT", "Everything is done.");
                        if(carnetVoyage.getIdVisitedPOIs()!=null)
                            ((SymbolLayer)style.getLayer("poi-data")).withFilter(Expression.match(Expression.get("id"), new Expression.ExpressionLiteral(carnetVoyage.getIdVisitedPOIs()), Expression.literal(true), Expression.literal(false)));

                        style.addSource(new GeoJsonSource("userPathSource",Feature.fromGeometry(carnetVoyage.getUserPath())));
                        style.addLayer(new LineLayer("userPathLayer", "userPathSource").withProperties(PropertyFactory.lineJoin(Property.LINE_JOIN_MITER),
                                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                                PropertyFactory.lineWidth(7f),
                                PropertyFactory.lineColor(Color.parseColor("#ff0000"))));

                        mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(mapboxMap.getCameraForGeometry(carnetVoyage.getUserPath())));
                    }
                });
            }
        });

        LinearLayout ll = (LinearLayout)findViewById(R.id.linear_layout_cdv);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView textViewStart = new TextView(this);
        textViewStart.setLayoutParams(layoutParams);
        textViewStart.setText("Départ le " + carnetVoyage.getDate_debut_visite());
        textViewStart.setPadding(16, 8,0,8);
        ll.addView(textViewStart);

        if(carnetVoyage.getVisitedPOIs()!=null && carnetVoyage.getVisitedPOIs().size()>0) {
            for (VisitPOI vp : carnetVoyage.getVisitedPOIs()) {
                TextView tvPOI = new TextView(this);
                tvPOI.setLayoutParams(layoutParams);
                tvPOI.setPadding(16, 8, 0,8);
                tvPOI.setText("POI " + vp.getPOIid() + " visité le " + vp.getVisit_date());
                ll.addView(tvPOI);
            }
        }

        if(!carnetVoyage.getPhotosCarnet().isEmpty()) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM à HH:mm");
            for (File f : carnetVoyage.getPhotosCarnet()) {
                if(!f.getAbsolutePath().contains("thumbnail")) {
                    TextView tvImage = new TextView(this);
                    tvImage.setPadding(16, 16, 0, 0);
                    tvImage.setText("Photo prise le " + sdf.format(new Date(f.lastModified())));
                    tvImage.setLayoutParams(layoutParams);
                    ll.addView(tvImage);

                    ImageView iv = new ImageView(this);
                    iv.setLayoutParams(params);
                    iv.setImageBitmap(BitmapFactory.decodeFile(f.toString()));
                    iv.setAdjustViewBounds(true);
                    iv.setPadding(0,0,0,16);
                    ll.addView(iv);
                }
            }
        }

        TextView textViewEnd = new TextView(this);
        textViewEnd.setLayoutParams(layoutParams);
        textViewEnd.setText("Arrivé le " + carnetVoyage.getDate_fin_visite());
        textViewEnd.setPadding(16, 8, 0, 8);
        ll.addView(textViewEnd);
    }

    public void onClickExitDispActivity(View v) {
        finish();
    }
}
