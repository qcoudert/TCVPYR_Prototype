package com.example.at_proto.RecommandationRelated.PostPOJO;

/**
 * Objet envoyé lorsque la visite d'un POI est effectuée
 */
public class VisitPOI {

    /**
     * ID de l'utilisateur ayant visité le POI
     */
    private String user_id;

    /**
     * IDs des POIs visités par l'utilisateur
     */
    private Ref[] visited_poi;

    /**
     * Durée de la visite en minutes
     */
    private int visit_duration;

    /**
     * Date de début de visite du POI par l'utilisateur au format "yyyy-mm-dd hh:mm:ss"
     */
    private String visit_date;

    /**
     * C'est le POJO utilisé pour que Gson serialize les données comme demandé par le service Web.
     * C'est pas très beau mais pas le temps de faire un TypeAdapter.
     */
    private class Ref {

        protected Ref(String reference) {
            this.reference = reference;
        }

        protected String reference;
    }


    public VisitPOI(String user_id, String visited_poi, int visit_duration, String visit_date) {
        this.user_id = user_id;
        this.visited_poi = new Ref[]{new Ref(visited_poi)};
        this.visit_duration = visit_duration;
        this.visit_date = visit_date;
    }

    public String getPOIid() {
        return visited_poi[0].reference;
    }

    public String getVisit_date() {
        return visit_date;
    }
}
