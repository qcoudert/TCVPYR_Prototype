package com.example.at_proto.RecommandationRelated;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.at_proto.ItineraryRelated.ItineraryRequest;
import com.example.at_proto.MainActivity;
import com.example.at_proto.R;
import com.example.at_proto.RecommandationRelated.PostPOJO.ProfileRecoHistorical;
import com.example.at_proto.RecommandationRelated.PostPOJO.ProfileRecoTheme;
import com.example.at_proto.RecommandationRelated.PostPOJO.RatingPOI;
import com.example.at_proto.RecommandationRelated.PostPOJO.SentProfile;
import com.example.at_proto.RecommandationRelated.PostPOJO.VisitPOI;
import com.google.gson.GsonBuilder;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.GeometryAdapterFactory;
import com.mapbox.geojson.gson.GeoJsonAdapterFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import static android.content.Context.MODE_PRIVATE;

public class PostUserInfo implements Callback<String> {

    private static final String POST_BASE_URL = "http://tcvpyr.univ-pau.fr/TCVPyrWebService/";
    private static PostAPI postAPI;

    private static SentProfile pendingProfile = null;

    private static List<RatingPOI> sentRatingPOI = new ArrayList<>();
    private static List<VisitPOI> sentVisitPOI = new ArrayList<>();

    public PostUserInfo() {
        if(postAPI==null) {
            Retrofit retrofit = new Retrofit.Builder().baseUrl(POST_BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            postAPI = retrofit.create(PostAPI.class);
        }
    }

    public void postNewUser(final SentProfile profile, final Context context) {
        pendingProfile = profile;
        postAPI.postNewUser(profile).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response){
                pendingProfile = null;
                saveProfile(profile, context);
                sendStoredPOJOs();
                new AlertDialog.Builder(context).setTitle("Profil envoyé")
                                                .setMessage("Nous vous conseillons de modifier le type de patrimoine qui vous intéresse dans le menu \"Préférences\".")
                                                .setPositiveButton(R.string.ok, null)
                                                .create()
                                                .show();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("PyrAT","Profile could not be send: " + t.getMessage());
            }
        });
    }

    /**
     * Sauvegarde le profil de l'utilisateur sur son appareil.
     * @param profile - Profil de l'utilisateur
     * @param context - Contexte nécessaire à l'obtention du FileOutput afin d'enregistrer le profil
     */
    private void saveProfile(SentProfile profile, Context context) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(context.openFileOutput(MainActivity.PROFILE_PATH, MODE_PRIVATE));
            osw.write(profile.toJSON());
            osw.close();
        }
        catch (IOException e) {
            Log.e("PyrAT", "Could not save profile on device: ", e);
        }
    }

    /**
     * Envoi tous les posts déjà envoyés durant la session de l'utilisateur(qu'ils soient envoyés avec succès ou non)
     * <p>
     *     Les POJOs sont stockés dans les listes {@link #sentRatingPOI} et {@link #sentVisitPOI}.
     * </p>
     */
    public void sendStoredPOJOs() {
        for (RatingPOI rp: sentRatingPOI) {
            postPOIRating(rp);
        }

        for (VisitPOI vp : sentVisitPOI) {
            postVisitPOI(vp);
        }
    }

    public void postThematicPreferences(ProfileRecoTheme profileRecoTheme) {
        if(pendingProfile==null)
            postAPI.postThematicPreferences(profileRecoTheme).enqueue(this);
    }

    public void postHistoricalPreferences(ProfileRecoHistorical profileRecoHistorical) {
        if(pendingProfile==null)
            postAPI.postHistoricalPreferences(profileRecoHistorical).enqueue(this);
    }

    public void postPOIRating(RatingPOI ratingPOI) {
        sentRatingPOI.add(ratingPOI);
        if(pendingProfile==null)
            postAPI.postPOIRating(ratingPOI).enqueue(this);
    }

    public void postVisitPOI(VisitPOI visitPOI) {
        sentVisitPOI.add(visitPOI);
        if(pendingProfile==null)
            postAPI.postVisitPOI(visitPOI).enqueue(this);
    }

    public void postItineraryRequest(final ItineraryRequest itineraryRequest, final Context context) {
        if(pendingProfile==null)
            postAPI.postItineraryRequest(itineraryRequest).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        Log.d("PyrAT", "Response is successful");
                        try {
                            File iti_file = new File(context.getDir("itineraries", MODE_PRIVATE), itineraryRequest.getVisit_area() + " le " + itineraryRequest.getVisit_date());
                            BufferedWriter writer = new BufferedWriter(new FileWriter(iti_file));
                            writer.write(response.body());
                            writer.close();
                        }
                        catch(IOException e) {
                            e.printStackTrace();
                            Log.d("PyrAT", "Couldn't write itinerary JSON");
                        }
                        ((Activity) context).finish();
                    }
                    else {
                        try {
                            Log.d("PyrAT", "Response not successful: " + response.code() + "\n" + response.errorBody().string());
                            response.errorBody().close();
                        }
                        catch(IOException e) {
                            Log.e("PyrAT", "ErrorBody Exception: ", e);
                        }
                        Toast.makeText(context, "Service actuellement indisponible. Veuillez réessayer plus tard.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e("PyrAT", "Itinerary reception failed: ", t);
                    Toast.makeText(context, "Service actuellement indisponible. Veuillez réessayer plus tard.", Toast.LENGTH_LONG).show();
                }
            });
    }

    @Override
    public void onResponse(Call<String> call, Response<String> response) {
        if(response.isSuccessful()) {
            Log.d("PyrAT", "Response from post is: " + response.body());
        }
        else {
            Log.d("PyrAT", "Not successful with code: " + response.code());
            try {
                Log.d("PyrAT", "Other infos: " + response.errorBody().string());
                response.errorBody().close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFailure(Call<String> call, Throwable t) {
        Log.e("PyrAT", "Error when sending post: " + t.getMessage());
    }

    private interface PostAPI {

        @Headers("Content-type: application/json")
        @POST("Users")
        Call<String> postNewUser(@Body SentProfile json);

        @Headers("Content-type: application/json")
        @POST("ThematicCategories/preferences")
        Call<String> postThematicPreferences(@Body ProfileRecoTheme json);

        @Headers("Content-type: application/json")
        @POST("HistoricalCategories/preferences")
        Call<String> postHistoricalPreferences(@Body ProfileRecoHistorical json);

        @Headers("Content-type: application/json")
        @POST("POIRating")
        Call<String> postPOIRating(@Body RatingPOI json);

        @Headers("Content-type: application/json")
        @POST("POIInfoVisit")
        Call<String> postVisitPOI(@Body VisitPOI json);

        @Headers("Content-type: application/json")
        @POST("Itinerary/UserContext")
        Call<String> postItineraryRequest(@Body ItineraryRequest json);
    }

    public static List<VisitPOI> getSentVisitPOI() {
        return sentVisitPOI;
    }
}
