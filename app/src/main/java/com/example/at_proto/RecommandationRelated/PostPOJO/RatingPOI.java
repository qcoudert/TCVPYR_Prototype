package com.example.at_proto.RecommandationRelated.PostPOJO;

import java.util.ArrayList;
import java.util.List;

public class RatingPOI {

    private String user_id;
    private List<POINote> poi_rating;
    private String rating_date;

    public RatingPOI(String user_id, int rating, String reference, String rating_date) {
        this.user_id = user_id;
        this.rating_date = rating_date;

        this.poi_rating = new ArrayList<>();
        this.poi_rating.add(new POINote(rating, reference));
    }

    private class POINote {

        protected int rating;
        protected String reference;

        protected POINote(int rating, String reference) {
            this.rating = rating;
            this.reference = reference;
        }

        @Override
        public String toString() {
            return rating + " pour " + reference;
        }
    }

    @Override
    public String toString() {
        return "ID: " + user_id + "\nDate: " + rating_date + "\nNote: " + poi_rating.get(0);
    }
}
