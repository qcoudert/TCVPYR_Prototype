package com.example.at_proto.POIRelated;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class QueryPOI {

    private static final String POI_BASE_URL = "http://tcvpyr.univ-pau.fr/TCVPyrWebService/resources/";
    private static POIApi poiApi;


    public QueryPOI(){
        if (poiApi==null){
            Retrofit retrofit = new Retrofit.Builder()
                                            .baseUrl(POI_BASE_URL)
                                            .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().registerTypeAdapter(POI.ListPOIType, new POI.ListPOIAdapter()).create()))
                                            .build();

            poiApi = retrofit.create(POIApi.class);
        }
    }

    public void getPoiFromID(String id, Callback<List<POI>> callback) {
        poiApi.getPOI(id).enqueue(callback);
    }

    public void getPoisFromCity(String city, Callback<List<POI>> callback) {
        poiApi.getPOIsVille(city).enqueue(callback);
    }

    public void getAllPois(Callback<List<POI>> callback) {
        poiApi.getPOIs().enqueue(callback);
    }


    private interface POIApi {

        @GET("poi/")
        Call<List<POI>> getPOIs();

        @GET("poi/{ville}")
        Call<List<POI>> getPOIsVille(@Path("ville") String ville);

        @GET("poi/ref/{id}")
        Call<List<POI>> getPOI(@Path("id") String idPOI);
    }
}
