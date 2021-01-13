package com.example.at_proto.RecommandationRelated.PostPOJO;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * POJO utilisé pour envoyer les préférences thématiques de l'utilisateur
 */
public class ProfileRecoTheme {

    /**
     * ID de l'utilisateur généré via UUID
     */
    private String user_id;

    /**
     * Liste des IDs de catégories thématiques suivies par l'utilisateur
     */
    private List<Integer> preferences_thematic_id;

    public ProfileRecoTheme(String user_id, List<Integer> preferences_thematic_id) {
        this.user_id = user_id;
        this.preferences_thematic_id = preferences_thematic_id;
    }

    public List<Integer> getPreferences_thematic_id() {
        return preferences_thematic_id;
    }
}
