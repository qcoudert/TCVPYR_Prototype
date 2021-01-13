package com.example.at_proto.LocationTracking;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.at_proto.MainActivity;
import com.example.at_proto.POIRelated.POI;
import com.example.at_proto.RecommandationRelated.PostPOJO.VisitPOI;
import com.example.at_proto.RecommandationRelated.PostUserInfo;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mapbox.api.tilequery.MapboxTilequery;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Classe permettant de détecter la proximitée des POI en fonction de la position de l'utilisateur.
 * A initialiser avant toute utilisation
 */
public class GeofencingPOI {

    /**
     * Instance de la classe qui sera utilisée dans l'application. A initialiser avant d'utiliser la classe.
     */
    private static GeofencingPOI instance;

    /**
     * Date de la dernière mise à jour des POIs à proximités de l'utilisateur (en ms)
     */
    private long lastFeaturesQueryUpdate;

    /**
     * POIs à proximité de l'utilisateur.
     */
    private List<Feature> queriedFeatures;

    /**
     * POIs en train d'être visités par l'utilisateur.
     * <p>
     *     La clé de la map représente l'ID du POI.
     *     Sa value est un tableau Long[2] contenant en [0] la date d'entrée (en ms) et en [1] la dernière date de présence connue autour du POI(en ms).
     * </p>
     */
    private final Map<String, Long[]> visitingFeatures;

    /**
     * Mutex utilisé pour rendre le <i>queriedFeatures</i> thread-safe.
     */
    private Semaphore queriedFeaturesMutex;

    /**
     * Tableau de correspondance entre le type du POI et le rayon de geofencing utilisé.
     * <p>
     *     La clé représente le type simplifié du POI.
     *     La valeur est le rayon de geofencing du POI (en m).
     * </p>
     */
    private final Map<String, Integer> radiusFromType;

    /**
     * Tableau des IDs de POI qui ont été visité durant une session (du démarrage de l'appli jusqu'à son extinction)
     */
    private final List<String> hasVisitedFeatures;
    /**
     * Rayon de requête des POIs qui sont à proximité de l'utilisateur.
     * Ce rayon devrait être >= au rayon le plus grand des rayons de {@Link #radiusFromType}
     */
    private static final int TILEQUERY_RADIUS_IN_METERS = 200;

    /**
     * Nombre de POIs maximum pouvant être récupérés et stockés lors d'une requête.
     * Cette constant détermine par extension le nombre de Feature stockés dans {@Link #queriedFeatures}
     */
    private static final int TILEQUERY_FEATURES_LIMIT = 20;

    /**
     * Durée en ms de raffraîchissement des {@Link #queriedFeatures}.
     * Cette durée fixe donc l'intervalle de temps entre les tilequery
     */
    private static final long TILEQUERY_REFRESH_RATE_IN_MS = 30000L;

    /**
     * ID du tileset qui va être utilisé pour effectuer la tilequery
     */
    public static final String TILESET_ID = "tcvpyr.bjiepq27";

    /**
     * Nom du layer d'informations (ici les POIs) à interroger sur le tileset lors de la tilequery
     */
    private static final String LAYER_NAME = "poi_data-443axb";

    private GeofencingPOI(Context context) {
        Map<String, Integer> radiusFromType1;
        queriedFeatures = null;
        lastFeaturesQueryUpdate = 0;
        visitingFeatures = new HashMap<>();
        hasVisitedFeatures = new ArrayList<>();
        queriedFeaturesMutex = new Semaphore(1);

        Type tempMap = TypeToken.getParameterized(Map.class, String.class, Integer.class).getType();
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(context.getAssets().open("type_radius.json"));
            radiusFromType1 = new GsonBuilder().registerTypeAdapter(tempMap, new TypeRadiusAdapter()).create().fromJson(inputStreamReader, tempMap);
        }
        catch(IOException e){
            Log.e("PyrAT", "GeofencingPOI Exception during initialization: " + e.getMessage());
            radiusFromType1 = new HashMap<>();
            radiusFromType1.put("default", 150);
        }

        radiusFromType = radiusFromType1;
        instance = this;
    }

    /**
     * Initialise l'instance de GeofencingPOI qui sera utilisée par la suite si non crée.
     * <p>
     *     Le contexte est demandé pour retrouver le fichier "type_radius.json" dans les assets de l'application.
     * </p>
     * @param context - Contexte de l'application
     */
    public static void initGeofencing(Context context) {
        if(instance==null){
            instance = new GeofencingPOI(context);
        }
    }

    /**
     * Permet d'obtenir l'instance GeofencingPOI
     * @return Instance de GeofencingPOI, null si non initialisé
     */
    public static GeofencingPOI getInstance() {
        return instance;
    }

    /**
     * Met à jour la collection de POI utilisée pour vérifier la proximitée avec l'utilisateur.
     * <p>
     *     Cette collection de POI est obtenu via le <b>tilequery</b> de mapbox.
     *     Le nombre de requête étant limité à 500 par minute, on met à jour la collection toutes les {@link #TILEQUERY_REFRESH_RATE_IN_MS} ms.
     *     Le rayon d'action est déterminé par {@link #TILEQUERY_RADIUS_IN_METERS} en m.
     *     Le nombre de POI récupéré par la requête est déterminé par {@link #TILEQUERY_FEATURES_LIMIT}.
     * </p>
     * @param location - Localisation de l'utilisateur utilisée pour mettre à jour les POI.
     */
    private void updateQueriedFeatures(Location location) {

        MapboxTilequery tilequery = MapboxTilequery.builder()
                .accessToken(MainActivity.ACCESS_TOKEN)
                .mapIds(TILESET_ID)
                .layers(LAYER_NAME)
                .query(Point.fromLngLat(location.getLongitude(), location.getLatitude()))
                .radius(TILEQUERY_RADIUS_IN_METERS)
                .limit(TILEQUERY_FEATURES_LIMIT)
                .build();

        try {
            queriedFeaturesMutex.acquire();

            tilequery.enqueueCall(new Callback<FeatureCollection>() {
                @Override
                public void onResponse(Call<FeatureCollection> call, Response<FeatureCollection> response) {
                    if (response.isSuccessful() && response.body().features()!=null) {
                            queriedFeatures = response.body().features();
                    }
                    queriedFeaturesMutex.release();
                }

                @Override
                public void onFailure(Call<FeatureCollection> call, Throwable t) {
                    Log.e("PyrAT", "updateQueriedFeatures Error: " + t.getMessage());
                    queriedFeaturesMutex.release();
                }
            });
        }
        catch(InterruptedException e) {
            Log.e("PyrAT", "updateQueriedFeatures Exception: " + e.getMessage());
        }
    }

    /**
     * Met à jour la map {@link #visitingFeatures} des POIs en train d'être visités.
     * Met à jour la liste {@link #hasVisitedFeatures} des POIs qui ont été visités.
     * @param location - Localisation de l'utilisateur utilisée pour mettre à jour les POIs visités.
     */
    private void updateVisitingFeatures(@NonNull Location location) {

        try{
            queriedFeaturesMutex.acquire();
            synchronized (visitingFeatures) {
                synchronized (hasVisitedFeatures) {
                    for (Feature f : queriedFeatures) {
                        if (f.hasProperty(POI.ID_KEY) && isVisited(f, location)) {
                            if (visitingFeatures.containsKey(f.getStringProperty(POI.ID_KEY))) {
                                Long[] time = visitingFeatures.get(f.getStringProperty(POI.ID_KEY));
                                time[1] = location.getTime();
                            } else {
                                Long[] time = new Long[2];
                                time[0] = location.getTime();
                                time[1] = location.getTime();
                                visitingFeatures.put(f.getStringProperty(POI.ID_KEY), time);

                                if (!hasVisitedFeatures.contains(f.getStringProperty(POI.ID_KEY)))
                                    hasVisitedFeatures.add(f.getStringProperty(POI.ID_KEY));
                            }
                        }
                    }

                    List<String> keys = new ArrayList<>();
                    for (Map.Entry<String, Long[]> ent : visitingFeatures.entrySet()) {
                        Long[] time = ent.getValue();
                        if (time[1] != location.getTime()) {
                            long timeSpent = time[1] - time[0];
                            Log.d("PyrAT", "User have visited " + ent.getKey() + " for " + timeSpent + " ms");
                            keys.add(ent.getKey());

                            //Envoi des informations de visite
                            if (timeSpent > 60000) {
                                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time[0]));
                                int duration = (int) (timeSpent / 60000); //Durée en minutes
                                VisitPOI visitPOI = new VisitPOI(MainActivity.USER_ID, ent.getKey(), duration, date);
                                Log.d("PyrAt", "Sending VisitPOI:" + visitPOI);
                                new PostUserInfo().postVisitPOI(visitPOI);
                            }
                        }
                    }

                    //Les clés des POI fini d'être visités ont été stockées pour enlever les éléments une fois la vérification terminée.
                    for (String k : keys) {
                        visitingFeatures.remove(k);
                    }
                }
            }
            queriedFeaturesMutex.release();

        }
        catch(InterruptedException e) {
            Log.e("PyrAT","updateVisitingFeatures Exception: " + e.getMessage());
        }
    }

    /**
     * Vérifie la présence de l'utilisateur dans le rayon de geofencing du POI.
     * <p>
     *      Les différents rayons d'action utilisés sont contenus dans {@link #radiusFromType} lui même initialisé à l'aide du fichier JSON <i>type_radius.json</i>.
     * </p>
     * @param f - Feature représentant le POI à vérifier.
     * @param location - Location de l'utilisateur.
     * @return true si l'utilisateur est dans le rayon d'action du POI, false sinon.
     */
    private boolean isVisited(Feature f, Location location) {
        Point p = (Point) f.geometry();
        String type = "default";
        if(f.hasProperty(POI.TYPE_KEY)){
            type = f.getStringProperty(POI.TYPE_KEY);
        }
        float[] res = new float[1];
        int radius;

        Location.distanceBetween(location.getLatitude(), location.getLongitude(), p.latitude(), p.longitude(), res);

        if(radiusFromType.containsKey(type))
            radius = radiusFromType.get(type);
        else
            radius = radiusFromType.get("default");

        return res[0]<=radius;
    }

    /**
     * S'occupe de gérer la mise à jour de {@link #visitingFeatures} et {@link #queriedFeatures} lors du changement de position de l'utilisateur.
     * @param location - Nouvelle location de l'utilisateur
     */
    public void update(Location location) {
        final Location location1 = location;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if((location1.getTime()-lastFeaturesQueryUpdate)>=TILEQUERY_REFRESH_RATE_IN_MS){
                    updateQueriedFeatures(location1);
                    lastFeaturesQueryUpdate = location1.getTime();
                }
                if(queriedFeatures!=null)
                    updateVisitingFeatures(location1);
            }
        }).start();
    }

    public String getIDs(){
        String res = "";
        synchronized (visitingFeatures){
            for(String s : visitingFeatures.keySet()){
                res = res + s + ", ";
            }
        }
        return res;
    }

    /**
     * Retourne les IDs des POI en train d'être visités.
     * @return String[] contenant les IDs des POI en train d'être visités.
     */
    public String[] getIDArray(){
        synchronized (visitingFeatures){
            Set<String> set = visitingFeatures.keySet();
            String[] res = new String[set.size()];
            int i = 0;
            for (String s : set) {
                res[i] = s;
                i++;
            }
            if(res.length!=0)
                return res;
            else
                return new String[]{""};
        }
    }

    /**
     * Retourne les IDs des POI en train d'être visités.
     * @return List<String> contenant les IDs des POI en train d'être visités.
     */
    public List<String> getVisitingList(){
        synchronized (visitingFeatures){
            Set<String> set = visitingFeatures.keySet();
            List<String> list = new ArrayList<>(set);
            return list;
        }
    }

    public String[] getHasVisitedFeatures() {
        synchronized (hasVisitedFeatures) {
            return (String[])hasVisitedFeatures.toArray();
        }
    }

    /**
     * TypeAdapter utiliser pour récupérer les informations de <i>type_radius.json</i> à l'aide de Gson pour les stocker dans {@link #radiusFromType}.
     */
    private static class TypeRadiusAdapter extends TypeAdapter<Map<String, Integer>> {

        @Override
        public void write(JsonWriter out, Map<String, Integer> value) throws IOException {

        }

        @Override
        public Map<String, Integer> read(JsonReader in) throws IOException {
            Map<String, Integer> map = new HashMap<>();
            String name = "";
            if(in.peek().equals(JsonToken.BEGIN_ARRAY)){
                in.beginArray();
                while(in.hasNext()) {
                    if(in.peek().equals(JsonToken.BEGIN_OBJECT)){
                        in.beginObject();
                        String type = "";
                        int radius = 0;
                        while(in.hasNext()){
                            if (in.peek().equals(JsonToken.NAME)) {
                                name = in.nextName();
                            }

                            switch(name){
                                case "type":
                                    type = in.nextString();
                                    break;
                                case "radius":
                                    radius = in.nextInt();
                                    break;
                                default:
                                    in.skipValue();
                                    break;
                            }

                        }
                        if(!type.isEmpty() && radius != 0)
                            map.put(type, radius);
                        in.endObject();
                    }
                    else
                        in.skipValue();
                }
                in.endArray();
            }

            if(map.isEmpty())
                map.put("default", 20);

            return map;
        }
    }
}
