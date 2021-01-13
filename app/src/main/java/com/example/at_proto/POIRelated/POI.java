package com.example.at_proto.POIRelated;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mapbox.geojson.Point;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class POI implements Parcelable {

    public static final Type ListPOIType = TypeToken.getParameterized(List.class, POI.class).getType();
    private String id;

    private Point coordinates;

    private String titre;
    private String resume;
    private String description;
    private String historique;
    private String type;
    private String adresse;
    private String dateSaisie;
    private List<String> datations;
    private List<String> chercheurs;
    private List<String> personnes;
    private List<Integer> anneesConstructions;

    public static final String ID_KEY = "id";
    public static final String TYPE_KEY = "type";

    public static final Parcelable.Creator CREATOR = new Creator() {
        @Override
        public POI createFromParcel(Parcel source) {
            return new POI(source);
        }

        @Override
        public POI[] newArray(int size) {
            return new POI[size];
        }
    };

    public static List<POI> fromJSON(String json) {
        return new GsonBuilder().registerTypeAdapter(ListPOIType, new POI.ListPOIAdapter()).create().fromJson(json, ListPOIType);
    }

    public POI(Parcel in) {
        double[] arr = new double[2];
        in.readDoubleArray(arr);
        this.coordinates = Point.fromLngLat(arr[0], arr[1]);

        String[] arr2 = new String[8];
        in.readStringArray(arr2);
        this.id = arr2[0];
        this.titre = arr2[1];
        this.description = arr2[2];
        this.historique = arr2[3];
        this.type = arr2[4];
        this.adresse = arr2[5];
        this.dateSaisie = arr2[6];
        this.resume = arr2[7];

        List<String> dates = new ArrayList<>();
        in.readStringList(dates);
        this.datations = dates;

        List<String> chercheurs = new ArrayList<>();
        in.readStringList(chercheurs);
        this.chercheurs = chercheurs;

        List<String> personnes = new ArrayList<>();
        in.readStringList(personnes);
        this.personnes = personnes;

        List<Integer> anneesConstructions = new ArrayList<>();
        in.readList(anneesConstructions, null);
        this.anneesConstructions = anneesConstructions;
    }

    public POI() {
        super();
    }

    /*@Override
    public String toString() {
        String res = "ID: " + id + "\n";
        res += "Titre: " + titre + "\n";
        res += "Type: " + type + "\n";
        res += "Annee: " + anneesConstructions.get(0) + "\n";
        res += "Adresse: " + adresse + "\n";
        res += "Personnes: " + personnes.get(0);
        return res;
    }*/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDoubleArray(new double[] {this.coordinates.longitude(), this.coordinates.latitude()});
        dest.writeStringArray(new String[] {this.id, this.titre, this.description, this.historique, this.type, this.adresse, this.dateSaisie, this.resume});
        dest.writeStringList(this.datations);
        dest.writeStringList(this.chercheurs);
        dest.writeStringList(this.personnes);
        dest.writeList(this.anneesConstructions);
    }

    /**
     * TypeAdapter à utiliser lors de la deserialization d'un seul POI GeoJSON (càd qui n'est pas inclus dans une feature collection)
     */
    public static class POIAdapter extends TypeAdapter<POI> {

        @Override
        public void write(JsonWriter out, POI poi) throws IOException {

        }

        @Override
        public POI read(JsonReader in) throws IOException {
            POI poi = new POI();
            in.beginObject();
            String fieldname = "";

            while(in.hasNext()){

                if(in.peek().equals(JsonToken.NAME)) {
                    fieldname = in.nextName();
                }

                if (fieldname.equals("geometry") && in.peek().equals(JsonToken.BEGIN_OBJECT)){

                    in.beginObject();

                    while(in.hasNext()){
                        if(in.peek().equals(JsonToken.NAME)){
                            fieldname = in.nextName();
                        }

                        if (fieldname.equals("coordinates") && in.peek().equals(JsonToken.BEGIN_ARRAY)){
                            in.beginArray();
                            List<Double> coordinates = new ArrayList<>();
                            while(in.hasNext()){
                                if(in.peek().equals(JsonToken.NUMBER))
                                    coordinates.add(in.nextDouble());
                                else
                                    in.skipValue();
                            }
                            if(coordinates.size()>1) {
                                poi.coordinates = Point.fromLngLat(coordinates.get(0), coordinates.get(1));
                            }
                            in.endArray();
                        }
                        else{
                            in.skipValue();
                        }

                    }

                    in.endObject();
                }
                else if(fieldname.equals("properties") && in.peek().equals(JsonToken.BEGIN_OBJECT)) {
                    in.beginObject();

                    while(in.hasNext()) {

                        if(in.peek().equals(JsonToken.NAME)) {
                            fieldname = in.nextName();
                        }

                        switch(fieldname) {
                            case "titre":
                                if (in.peek().equals(JsonToken.STRING))
                                    poi.titre = in.nextString();
                                break;

                            case "description":
                                if(in.peek().equals(JsonToken.STRING))
                                    poi.description = in.nextString();
                                break;

                            case "typeSimplifie":
                                if (in.peek().equals(JsonToken.STRING))
                                    poi.type = in.nextString();
                                break;

                            case "id":
                                if (in.peek().equals(JsonToken.STRING))
                                    poi.id = in.nextString();
                                break;

                            case "dateSaisie":
                                if (in.peek().equals(JsonToken.STRING))
                                    poi.dateSaisie = in.nextString();
                                break;

                            case "historique":
                                if(in.peek().equals(JsonToken.STRING))
                                    poi.historique = in.nextString();
                                break;

                            case "adresse":
                                if (in.peek().equals(JsonToken.BEGIN_OBJECT)) {
                                    in.beginObject();

                                    String voie = "";
                                    String mun = "";
                                    String num = "";
                                    String region = "";
                                    while(in.hasNext()) {
                                        if(in.peek().equals(JsonToken.NAME)) {
                                            fieldname = in.nextName();
                                        }

                                        switch (fieldname) {
                                            case "nomVoie":
                                                if (in.peek().equals(JsonToken.STRING))
                                                    voie = in.nextString() + ", ";
                                                break;

                                            case "numero":
                                                if (in.peek().equals(JsonToken.STRING))
                                                    num = in.nextString() + " ";
                                                break;

                                            case "municipalite":
                                                if (in.peek().equals(JsonToken.STRING))
                                                    mun = in.nextString() + ", ";
                                                break;

                                            case "region":
                                                if (in.peek().equals(JsonToken.STRING))
                                                    region = in.nextString();
                                                break;

                                            default:
                                                in.skipValue();
                                                break;
                                        }
                                    }

                                    poi.adresse = num + voie + mun + region;
                                    in.endObject();
                                }
                                break;

                            case "anneesConstructions":
                                if (in.peek().equals(JsonToken.BEGIN_ARRAY)){
                                    in.beginArray();
                                    poi.anneesConstructions = new ArrayList<>();
                                    while(in.hasNext()) {
                                        if(in.peek().equals(JsonToken.NUMBER)) {
                                            poi.anneesConstructions.add(in.nextInt());
                                        }
                                    }
                                    in.endArray();
                                }
                                break;

                            case "datations":
                                if (in.peek().equals(JsonToken.BEGIN_ARRAY)){
                                    in.beginArray();
                                    poi.datations = new ArrayList<>();
                                    while(in.hasNext()) {
                                        if(in.peek().equals(JsonToken.STRING)) {
                                            poi.datations.add(in.nextString());
                                        }
                                    }
                                    in.endArray();
                                }
                                break;

                            case "chercheurs":
                                if(in.peek().equals(JsonToken.BEGIN_ARRAY)){
                                    in.beginArray();
                                    poi.chercheurs = new ArrayList<>();
                                    while(in.hasNext()) {
                                        if(in.peek().equals(JsonToken.BEGIN_OBJECT)){
                                            in.beginObject();
                                            String nom = "";
                                            String prenom = "";
                                            while(in.hasNext()){
                                                if(in.peek().equals(JsonToken.NAME))
                                                    fieldname = in.nextName();

                                                switch (fieldname) {
                                                    case "nom":
                                                        if(in.peek().equals(JsonToken.STRING))
                                                            nom = in.nextString() + " ";
                                                        break;

                                                    case "prenom":
                                                        if(in.peek().equals(JsonToken.STRING))
                                                            prenom = in.nextString();
                                                        break;

                                                        default:
                                                            in.skipValue();
                                                            break;
                                                }
                                            }
                                            poi.chercheurs.add(nom + prenom);
                                            in.endObject();
                                        }
                                    }
                                    in.endArray();
                                }
                                break;

                            case "personnes":
                                if(in.peek().equals(JsonToken.BEGIN_ARRAY)){
                                    in.beginArray();
                                    poi.personnes = new ArrayList<>();
                                    while(in.hasNext()) {
                                        if(in.peek().equals(JsonToken.BEGIN_OBJECT)){
                                            in.beginObject();
                                            String nom = "";
                                            String prenom = "";
                                            String role ="";
                                            while(in.hasNext()){
                                                if(in.peek().equals(JsonToken.NAME))
                                                    fieldname = in.nextName();

                                                switch (fieldname) {
                                                    case "role":
                                                        if(in.peek().equals(JsonToken.STRING))
                                                            role = ", " + in.nextString();
                                                        break;

                                                    case "nom":
                                                        if(in.peek().equals(JsonToken.STRING))
                                                            nom = in.nextString() + " ";
                                                        break;

                                                    case "prenom":
                                                        if(in.peek().equals(JsonToken.STRING))
                                                            prenom = in.nextString();
                                                        break;

                                                    default:
                                                        in.skipValue();
                                                        break;
                                                }
                                            }
                                            poi.personnes.add(nom + prenom + role);
                                            in.endObject();
                                        }
                                    }
                                    in.endArray();
                                }
                                break;

                            default:
                                if(!in.peek().equals(JsonToken.NAME))
                                    in.skipValue();
                                break;
                        }
                    }

                    in.endObject();
                }
                else {
                    in.skipValue();
                }
            }
            in.endObject();
            return poi;
        }
    }

    /**
     * TypeAdapter à utiliser lors de la deserialization d'un ou plusieurs POI GeoJSON (càd inclus dans une feature collection)
     */
    public static class ListPOIAdapter extends TypeAdapter<List<POI>> {

        @Override
        public void write(JsonWriter out, List<POI> value) throws IOException {

        }

        @Override
        public List<POI> read(JsonReader in) throws IOException {
            List<POI> pois = new ArrayList<>();
            String fieldname = "";
            in.beginObject();
            while(in.hasNext()) {
                if(in.peek().equals(JsonToken.NAME))
                    fieldname = in.nextName();

                if(fieldname.equals("features")){
                    if(in.peek().equals(JsonToken.BEGIN_ARRAY)) {
                        POIAdapter adapter = new POIAdapter();
                        in.beginArray();

                        while(in.hasNext()){
                            if (in.peek().equals(JsonToken.BEGIN_OBJECT)) {
                                pois.add(adapter.read(in));
                            }
                            else {
                                in.skipValue();
                            }
                        }

                        in.endArray();
                    }
                }
                else {
                    in.skipValue();
                }
            }

            in.endObject();

            if(pois.isEmpty())
                return null;
            else
                return pois;
        }
    }

    //GETTERS

    public String getTitre() {
        return titre;
    }

    public String getResume() { return resume; }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    public String getHistorique() {
        return historique;
    }

    public String getType() {
        return type;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getDateSaisie() {
        return dateSaisie;
    }

    public List<String> getDatations() {
        return datations;
    }

    public List<String> getChercheurs() {
        return chercheurs;
    }

    public List<String> getPersonnes() {
        return personnes;
    }

    public List<Integer> getAnneesConstructions() {
        return anneesConstructions;
    }

    //SETTERS

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAnneesConstructions(List<Integer> anneesConstructions) {
        this.anneesConstructions = anneesConstructions;
    }

    public void setCoordinates(Point coordinates) {
        this.coordinates = coordinates;
    }
}
