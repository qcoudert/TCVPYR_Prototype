package com.example.at_proto.ItineraryRelated;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import com.google.gson.Gson;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

/**
 * Objet utilisé pour créer les requêtes d'itinéraires
 * <p>
 *     Pour créer une requête, il suffit d'utiliser le constructeur par défaut puis de modifier les 5 champs qui composent la requête avec les setters.
 *     On utilise alors la méthode {@link #toJSON()} pour créer le JSON qui sera transmis au WebService.
 * </p>
 */
public class ItineraryRequest {

    /**
     * ID de l'utilisateur généré via UUID
     */
    private String user_id;

    /**
     *Le mode de transport utilisé lors de l'itinéraire. Les valeurs attribuables sont déterminées par l'annotation @TRANSPORT.
     */
    private String transportation_mode;

    /**
     *La date (format yyyy-MM-dd hh:mm:ss) à laquelle l'itinéraire va être effectuée
     */
    private String visit_date;

    /**
     *La durée de la visite en minutes (-1 si non précisé)
     */
    private int visit_duration;

    /**
     *La ville à visiter
     */
    private String visit_area;

    /**
     *Le type de personne participant à l'itinéraire. Les valeurs attribuables sont déterminées par l'annotation @GROUP.
     */
    private int group_id;

    public ItineraryRequest() {}

    /**
     * Sérialise la requête en JSON
     * @return Le JSON correspondant à la requête créée
     */
    public String toJSON() {
        if(this.isValid()) {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
        return null;
    }

    /**
     * Vérifie si la requête est valide.
     * @return true si la requête est valide, faux sinon
     */
    public boolean isValid() {
        return (transportation_mode!=null && visit_date!=null && visit_area!=null && visit_duration!=0 && group_id!=0);
    }

    //GETTERS
    public String getTransportation_mode() {
        return transportation_mode;
    }

    public String getVisit_date() {
        return visit_date;
    }

    public int getVisit_duration() {
        return visit_duration;
    }

    public String getVisit_area() {
        return visit_area;
    }

    public int getGroup_id() {
        return group_id;
    }

    //SETTERS

    /*
     *  Les trois types de transports autorisés pour les requêtes d'itinéraires sont listés ci-dessous et ne doivent pas
     *  être modifiés sauf en cas de modification des requêtes côté serveur.
     */
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({TRANSPORT_WALK, TRANSPORT_CAR, TRANSPORT_BIKE})
    public @interface TRANSPORT{}

    public static final String TRANSPORT_WALK = "pied";
    public static final String TRANSPORT_CAR = "voiture";
    public static final String TRANSPORT_BIKE = "vélo";

    /**
     * @param transportation_mode - Type de transport qui sera utilisé pendant l'itinéraire.
     */
    public void setTransportation_mode(@TRANSPORT String transportation_mode) {
        this.transportation_mode = transportation_mode;
    }

    /*
     * Les trois types d'identifiants de groupe implémentés sont listés ci-dessous et ne doivent pas
     * être modifiés sauf en cas de modification des requêtes côté serveur.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({GROUP_KIDS, GROUP_YOUNG, GROUP_ADULTS, GROUP_SENIORS})
    public @interface GROUP{}

    public static final int GROUP_KIDS = 1;
    public static final int GROUP_YOUNG = 2;
    public static final int GROUP_ADULTS = 3;
    public static final int GROUP_SENIORS = 4;

    /**
     * @param group_id - ID du groupe participant à l'itinéraire
     */
    public void setGroup_id(@GROUP int group_id) {
        this.group_id = group_id;
    }

    /**
     * @param visit_date - date à laquelle l'itinéraire va être parcouru (au format dd/mm/yyyy hh:mm:ss)
     */
    public void setVisit_date(String visit_date) {
        this.visit_date = visit_date;
    }

    /**
     * @param visit_duration - durée de la visite en minutes
     */
    public void setVisit_duration(int visit_duration) {
        this.visit_duration = visit_duration;
    }

    /**
     * @param visit_area - Ville à visiter lors de l'itinéraire
     */
    public void setVisit_area(String visit_area) {
        this.visit_area = visit_area;
    }

    /**
     * @param user_id - ID de l'utilisateur généré via UUID
     */
    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
