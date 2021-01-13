package com.example.at_proto.RecommandationRelated.PostPOJO;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO utilisé pour envoyer les préférences historiques de l'utilisateur
 */
public class ProfileRecoHistorical {

    /**
     * ID de l'utilisateur généré via UUID
     */
    private String user_id;

    /**
     * Liste des IDs de catégories thématiques suivies par l'utilisateur
     */
    private List<Integer> preferences_historical_id;

    public ProfileRecoHistorical(String user_id, List<Integer> preferences_historical_id) {
        this.user_id = user_id;
        this.preferences_historical_id = preferences_historical_id;
    }

    public List<Integer> getPreferences_historical_id() {
        return preferences_historical_id;
    }
}
