package com.example.at_proto.CarnetVoyageRelated;

import android.content.Context;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

import com.example.at_proto.RecommandationRelated.PostPOJO.VisitPOI;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CarnetVoyage {

    /**
     * Polyline représentant le parcours enregistré de l'utilisateur
     */
    private LineString userPath;

    /**
     * POI visités durant l'enregistrement du parcours de l'utilisateur
     */
    private List<VisitPOI> visitedPOIs;

    /**
     * IDs des POIs visités pour affichage sur la carte.
     */
    private String[] idVisitedPOIs;

    private String date_debut_visite;

    private String date_fin_visite;

    /**
     * Path des photos prises durant le parcours
     */
    private List<File> photosCarnet;


    public CarnetVoyage(List<Location> locations, List<VisitPOI> pois) {
        this.visitedPOIs = pois;

        List<Point> points = new ArrayList<>();
        for (Location l : locations) {
            points.add(Point.fromLngLat(l.getLongitude(), l.getLatitude()));
        }
        this.userPath = LineString.fromLngLats(points);

        if(visitedPOIs!=null && visitedPOIs.size()>0) {
            idVisitedPOIs = new String[visitedPOIs.size()];
            for (int i = 0; i < visitedPOIs.size(); i++) {
                idVisitedPOIs[i] = visitedPOIs.get(i).getPOIid();
            }
        }
        else
            idVisitedPOIs = null;

        Date start = new Date(locations.get(0).getTime());
        Date end = new Date(locations.get(locations.size()-1).getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy à HH:mm");
        date_debut_visite = sdf.format(start);
        date_fin_visite = sdf.format(end);

        photosCarnet = new ArrayList<>();
        FileFilter ff = new DateFileFilter(locations.get(0).getTime(), locations.get(locations.size()-1).getTime());
        File pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        checkDirectory(pictures, ff);
        checkDirectory(dcim, ff);
        Log.d("PyrAT", "Content: " + photosCarnet);
    }

    private void checkDirectory(File directory, FileFilter ff) {
        if(directory.listFiles()!=null) {
            for (File f : directory.listFiles()) {
                Log.d("SpamDeb", "File detected: " + f);
                if (f.isFile() && ff.accept(f)) {
                    photosCarnet.add(f);
                    Log.d("PyrAT", "File was added to photosCarnet: " + f);
                }
                else if (f.isDirectory())
                    checkDirectory(f, ff);
            }
        }
    }

    public LineString getUserPath() {
        return userPath;
    }

    public List<VisitPOI> getVisitedPOIs() {
        return visitedPOIs;
    }

    public String[] getIdVisitedPOIs() {
        return idVisitedPOIs;
    }

    public String getDate_debut_visite() {
        return date_debut_visite;
    }

    public String getDate_fin_visite() {
        return date_fin_visite;
    }

    public List<File> getPhotosCarnet() {
        return photosCarnet;
    }

    public class DateFileFilter implements FileFilter {

        private long start;
        private long end;

        public DateFileFilter(long start, long end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public boolean accept(File file) {
            return (file.isFile() && file.lastModified()>start && file.lastModified()<end);
        }
    }
}
