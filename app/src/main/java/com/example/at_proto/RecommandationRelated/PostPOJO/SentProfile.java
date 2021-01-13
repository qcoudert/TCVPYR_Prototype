package com.example.at_proto.RecommandationRelated.PostPOJO;

import android.support.annotation.IntDef;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SentProfile{

    /**
     * ID de l'utilisateur généré via UUID
     */
    private String user_id;

    /**
     * Genre choisi par l'utilisateur
     */
    private String gender;

    /**
     * Catégorie d'âge choisie par l'utilisateur.
     */
    private int age_category_id;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CHILD_ID, YOUNG_ID, ADULT_ID, SENIOR_ID})
    public @interface AGE_CATEGORY{}

    public static final int CHILD_ID = 1;
    public static final int YOUNG_ID = 2;
    public static final int ADULT_ID = 3;
    public static final int SENIOR_ID = 4;

    public SentProfile(String user_id, String gender, @AGE_CATEGORY int age_category_id) {
        this.user_id = user_id;
        this.gender = gender;
        this.age_category_id = age_category_id;
    }

    public String toJSON() {
        /*String res;
        try {
            JSONObject json = new JSONObject();
            json.put("user_id", user_id);
            json.put("gender", gender);
            json.put("age_category_id", age_category_id);
            res = json.toString();
        }
        catch(JSONException e) {
            e.printStackTrace();
            res = null;
        }*/

        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static SentProfile fromJSON(InputStreamReader reader) {
        Gson gson = new Gson();
        return gson.fromJson(reader, SentProfile.class);
    }

    public String getUser_id() {
        return user_id;
    }
}
