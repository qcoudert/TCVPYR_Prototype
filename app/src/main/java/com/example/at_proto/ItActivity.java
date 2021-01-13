package com.example.at_proto;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.at_proto.ItineraryRelated.GeocodeUtil;
import com.example.at_proto.ItineraryRelated.ItineraryRequest;
import com.example.at_proto.ItineraryRelated.TimeDurationPickerDialog;
import com.example.at_proto.RecommandationRelated.PostUserInfo;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItActivity extends AppCompatActivity {

    private RadioGroup radioGroupTransport;
    private RadioGroup radioGroupGroup;

    private ItineraryRequest itineraryRequest;
    private String day;
    private String hour;

    private boolean isMapOpen = false;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_it);

        radioGroupTransport = (RadioGroup)findViewById(R.id.radio_group_transport);
        radioGroupGroup = (RadioGroup)findViewById(R.id.radio_group_group_id);
        itineraryRequest = new ItineraryRequest();

        //Initialisation des Textviews pour qu'ils affichent la date et la ville lors de l'ouverture de l'activité
        TextView date_day = (TextView)findViewById(R.id.visit_date_day);
        TextView date_hour = (TextView)findViewById(R.id.visit_date_hour);
        final TextView visit_area = (TextView)findViewById(R.id.visit_area);
        Calendar c = Calendar.getInstance();

        day = (c.get(Calendar.YEAR)) + "-" + (c.get(Calendar.MONTH)+1) + "-" + c.get(Calendar.DAY_OF_MONTH);
        hour = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":00";
        date_day.setText(day);
        date_hour.setText(hour);

        //Décommenter si besoin de geocoding à partir de la localisation de l'utilisateur
        /*if(MainActivity.currentLocation!=null) {
            GeocodeUtil.geocodeLocationToCity(MainActivity.currentLocation, 1, new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                    List<CarmenFeature> features = response.body().features();

                    if (features.size() > 0) {
                        visit_area.setText("Position actuelle: " + features.get(0).placeName());
                        itineraryRequest.setVisit_area(features.get(0).placeName());
                    }
                }

                @Override
                public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }
        else*/
            itineraryRequest.setVisit_area("Bagnères-de-Luchon");

        //Initialisation de la requête d'itinéraire
        itineraryRequest.setGroup_id(ItineraryRequest.GROUP_ADULTS);
        itineraryRequest.setTransportation_mode(ItineraryRequest.TRANSPORT_WALK);
        itineraryRequest.setVisit_date(day + " " + hour);
        itineraryRequest.setVisit_duration(60);
        itineraryRequest.setUser_id(MainActivity.USER_ID);
    }

    /**
     *  Fonction appelée lorsque l'utilisateur veut quitter l'activité en cliquant sur le bouton de sortie en haut à gauche de l'interface
     */
    public void onClickExitButton(View v){
        setResult(0);
        finish();
    }

    /**
     * Fonction onClick de (R.id.radio_group_transport)
     *  <p>
     *  Fonction appelée lorsque l'utilisateur choisit le type de transport utilisé lors de l'itinéraire en cliquant sur la vue associée (R.id.radio_group_transport)
     *  On repère alors quel bouton a été pressé pour mettre à jour la requête d'itinéraire.
     *  </p>
     */
    public void checkButtonClickTransport(View v) {
        int id = radioGroupTransport.getCheckedRadioButtonId();

        switch(id){
            case R.id.transport_walk:
                itineraryRequest.setTransportation_mode(ItineraryRequest.TRANSPORT_WALK);
                break;
            case R.id.transport_car:
                itineraryRequest.setTransportation_mode(ItineraryRequest.TRANSPORT_CAR);
                break;
            case R.id.transport_bike:
                itineraryRequest.setTransportation_mode(ItineraryRequest.TRANSPORT_BIKE);
                break;
        }
    }

    /**
     * Fonction onClick de (R.id.radio_group_group_id)
     *  <p>
     *  Fonction appelée lorsque l'utilisateur choisit le type de personnes présentes lors de l'itinéraire en cliquant sur la vue associée (R.id.radio_group_group_id)
     *  On repère alors quel bouton a été pressé pour mettre à jour la requête d'itinéraire.
     *  </p>
     */
    public void checkButtonClickGroup(View v) {
        int id = radioGroupGroup.getCheckedRadioButtonId();

        switch(id){
            case R.id.group_kids:
                itineraryRequest.setGroup_id(ItineraryRequest.GROUP_KIDS);
                break;
            case R.id.group_adults:
                itineraryRequest.setGroup_id(ItineraryRequest.GROUP_ADULTS);
                break;
            case R.id.group_seniors:
                itineraryRequest.setGroup_id(ItineraryRequest.GROUP_SENIORS);
                break;
            case R.id.group_young:
                itineraryRequest.setGroup_id(ItineraryRequest.GROUP_YOUNG);
                break;
        }
    }

    /**
     * Fonction onClick de (R.id.visit_date_day_container)
     *  <p>
     *  Fonction utilisée lorsque l'utilisateur veut choisir le jour de son itinéraire en cliquant sur la vue associée (R.id.visit_date_day_container)
     *  Un DatePickerDialog est alors ouvert pour permettre à l'utilisateur de choisir une journée. Les vues et la requête sont alors mis à jour en conséquence.
     *  </p>
     */
    public void dateClick(View v) {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month++;
                day = year + "-" + month + "-" + dayOfMonth;
                TextView txt = (TextView) findViewById(R.id.visit_date_day);
                txt.setText(day);
                itineraryRequest.setVisit_date(day + " " + hour);
            }
        };

        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth, dateSetListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    /**
     * Fonction onClick de (R.id.visit_date_hour_container)
     *  <p>
     *  Fonction utilisée lorsque l'utilisateur veut choisir l'heure de son itinéraire en cliquant sur la vue associée (R.id.visit_date_hour_container)
     *  Un TimePickerDialog est alors ouvert pour permettre à l'utilisateur de choisir un horaire. Les vues et la requête sont alors mis à jour en conséquence.
     *  </p>
     */
    public void timeClick(View v) {
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Log.d("DebugTimeDialog", "Time is: " + hourOfDay + ":" + minute);
                hour = hourOfDay +":"+ minute + ":00";
                TextView txt = (TextView) findViewById(R.id.visit_date_hour);
                txt.setText(hour);
                itineraryRequest.setVisit_date(day + " " + hour);
            }
        };

        Calendar c = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, android.R.style.Theme_DeviceDefault_Dialog_MinWidth, timeSetListener, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
        dialog.show();
    }

    public void durationClick(View v) {
        TimeDurationPickerDialog.OnDurationListener durationListener = new TimeDurationPickerDialog.OnDurationListener() {
            @Override
            public void onDurationSet(View v, int duration) {
                TextView txt = (TextView) findViewById(R.id.visit_duration);
                txt.setText(duration + " min");
                itineraryRequest.setVisit_duration(duration);
            }
        };

        TimeDurationPickerDialog dialog = new TimeDurationPickerDialog(this, durationListener, 1);
        dialog.show();
    }

    public void cityClick(View v) {

        final FrameLayout layout = (FrameLayout)findViewById(R.id.map_frame_layout);
        final TextView txtv = (TextView)findViewById(R.id.visit_area);
        if(!isMapOpen) {
            layout.setVisibility(View.VISIBLE);
            Mapbox.getInstance(this, MainActivity.ACCESS_TOKEN);

            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            MapboxMapOptions options = new MapboxMapOptions();
            options.camera(new CameraPosition.Builder()
                    .target(new LatLng(42.821, 0.723))
                    .zoom(4.5)
                    .build());

            mapFragment = SupportMapFragment.newInstance(options);

            transaction.add(R.id.map_frame_layout, mapFragment, "com.mapbox.map");
            transaction.commit();

            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull final MapboxMap mapboxMap) {

                    mapboxMap.setStyle(new Style.Builder().fromUrl("mapbox://styles/tcvpyr/cjxzv3mts0qn41dlc61sm02mx"), new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {
                            Log.d("PyrAT", "Everything is done.");
                        }
                    });

                    mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                        @Override
                        public boolean onMapClick(@NonNull LatLng point) {

                            Style style = mapboxMap.getStyle();

                            if(style!=null){

                                final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
                                List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, "villes-tcvpyr");
                                int featuresSize = features.size();

                                Log.d("PyrAT", "Features found: " + featuresSize);
                                if(featuresSize>0){
                                    itineraryRequest.setVisit_area(features.get(0).getStringProperty("name"));
                                    txtv.setText(features.get(0).getStringProperty("name"));
                                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                    transaction.remove(mapFragment);
                                    transaction.commit();
                                    layout.setVisibility(View.GONE);
                                    isMapOpen = false;
                                }
                                return true;
                            }
                            return false;
                        }
                    });
                }
            });

            isMapOpen = true;
        }
        else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.remove(mapFragment);
            transaction.commit();
            layout.setVisibility(View.GONE);
            isMapOpen = false;
        }
    }

    /**
     * Fonction onClick du bouton de confirmation
     */
    public void confirmClick(View v) {
        if(Utils.isNetworkAvailable(this)) {
            if (itineraryRequest.isValid()) {
                new PostUserInfo().postItineraryRequest(itineraryRequest, this);
                Log.d("PyrAT", "Contenu posté: " + itineraryRequest.toJSON());
            } else {
                Toast.makeText(this, "JSON not valid", Toast.LENGTH_SHORT).show();
            }
        }
        else
            Utils.showNetworkAlertDialog(this);
    }

}
