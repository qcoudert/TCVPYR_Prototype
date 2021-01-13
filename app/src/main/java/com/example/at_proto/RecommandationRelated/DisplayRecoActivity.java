package com.example.at_proto.RecommandationRelated;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.example.at_proto.MainActivity;
import com.example.at_proto.R;
import com.example.at_proto.RecommandationRelated.PostPOJO.ProfileRecoHistorical;
import com.example.at_proto.RecommandationRelated.PostPOJO.ProfileRecoTheme;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayRecoActivity extends Activity {

    public static final String RECO_TYPE_KEY = "typekey";
    public static final String RECO_TYPE_THEME = "theme";
    public static final String RECO_TYPE_HISTO = "histo";

    public static final String RECO_THEME_PATH = "userPrefTheme.ser";
    public static final String RECO_HISTO_PATH = "userPrefHisto.ser";

    public static final String RECO_THEME_IDS = "category_id_theme.json";
    public static final String RECO_HISTO_IDS = "category_id_histo.json";

    private List<String> headers;
    private Map<String, List<String>> stringListMap;
    private Map<String, Boolean> userPreferences;
    private ExpandableListAdapter adapter;
    private boolean isTheme;
    private Map<String, Integer> categoriesID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expendable_list);

        final TextView list_title = (TextView) findViewById(R.id.expandable_list_title);
        Type tempType = TypeToken.getParameterized(Map.class, String.class, Integer.class).getType();
        String[] data;
        headers = new ArrayList<>();
        stringListMap = new HashMap<>();
        if(getIntent().getStringExtra(RECO_TYPE_KEY).equals(RECO_TYPE_THEME)) {
            list_title.setText(R.string.recoTheme);
            data = getResources().getStringArray(R.array.r_exp_list_data_theme);
            if(!readUserPreferences(RECO_THEME_PATH))
                initUserPref(data);
            isTheme = true;
            try {
                categoriesID = new GsonBuilder().registerTypeAdapter(tempType, new CategoryIDAdapter()).create().fromJson(new InputStreamReader(getAssets().open(RECO_THEME_IDS)), tempType);
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
        else {
            list_title.setText(R.string.recoHisto);
            data = getResources().getStringArray(R.array.r_exp_list_data_histo);
            if(!readUserPreferences(RECO_HISTO_PATH))
                initUserPref(data);
            isTheme = false;
            try {
                categoriesID = new GsonBuilder().registerTypeAdapter(tempType, new CategoryIDAdapter()).create().fromJson(new InputStreamReader(getAssets().open(RECO_HISTO_IDS)), tempType);
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
        parseData(data);

        adapter = new ExpandableListAdapter(this, headers, stringListMap, userPreferences);
        ExpandableListView expandableListView = findViewById(R.id.expListView);
        expandableListView.setGroupIndicator(getResources().getDrawable(R.drawable.group_state_list));
        expandableListView.setAdapter(adapter);
    }

    private void parseData(String[] data) {
        for(String s : data) {
            String[] res = s.split(";");
            headers.add(res[0]);
            List<String> temp = new ArrayList<>();

            for(int i = 1; i<res.length; i++) {
                temp.add(res[i]);
            }

            if(res.length>1)
                stringListMap.put(res[0], temp);
            else
                stringListMap.put(res[0], null);
        }
    }

    private boolean readUserPreferences(String path) {
        try{
            FileInputStream fis = openFileInput(path);
            ObjectInputStream is = new ObjectInputStream(fis);
            userPreferences = (Map<String, Boolean>) is.readObject();
            is.close();
            fis.close();
        }
        catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void initUserPref(String[] data) {
        userPreferences = new HashMap<>();
        for(String s : data) {
            String[] res = s.split(";");
            for(String cat  : res) {
                userPreferences.put(cat, false);
            }
        }
    }

    private void writeUserPreferences(String path) {
        if(userPreferences!=null && !userPreferences.isEmpty()) {
            try {
                FileOutputStream fos = openFileOutput(path, MODE_PRIVATE);
                ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(userPreferences);
                os.close();
                fos.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private void sendProfileReco() {
        List<Integer> ids = new ArrayList<>();
        if(categoriesID!=null) {
            for (Map.Entry<String, Boolean> entry : userPreferences.entrySet()) {
                if (entry.getValue() && categoriesID.containsKey(entry.getKey())) {
                    ids.add(categoriesID.get(entry.getKey()));
                }
            }
        }

        Log.d("PyrAT", "Found this ids: " + ids.toString());

        if(isTheme && !ids.isEmpty() && MainActivity.USER_ID!=null)
            new PostUserInfo().postThematicPreferences(new ProfileRecoTheme(MainActivity.USER_ID, ids));
        else if(!isTheme && !ids.isEmpty() && MainActivity.USER_ID!=null)
            new PostUserInfo().postHistoricalPreferences(new ProfileRecoHistorical(MainActivity.USER_ID, ids));
        else
            Log.d("PyrAT", "DisplayRecoActivity:sendProfileReco : Préférences non envoyées.");
    }

    @Override
    protected void onStop() {
        super.onStop();

        sendProfileReco();

        if(isTheme)
            writeUserPreferences(RECO_THEME_PATH);
        else
            writeUserPreferences(RECO_HISTO_PATH);
    }

    private class CategoryIDAdapter extends TypeAdapter<Map<String, Integer>> {

        @Override
        public void write(JsonWriter jsonWriter, Map<String, Integer> stringIntegerMap) throws IOException {

        }

        @Override
        public Map<String, Integer> read(JsonReader jsonReader) throws IOException {
            String fieldname = "";
            Map<String, Integer> res = new HashMap<>();

            if(jsonReader.peek().equals(JsonToken.BEGIN_ARRAY)) {
                jsonReader.beginArray();
                while(jsonReader.hasNext()) {
                    if(jsonReader.peek().equals(JsonToken.BEGIN_OBJECT)) {
                        jsonReader.beginObject();
                        String key = null;
                        int value = 0;
                        while(jsonReader.hasNext()) {
                            if(jsonReader.peek().equals(JsonToken.NAME))
                                fieldname = jsonReader.nextName();

                            switch(fieldname) {
                                case "name":
                                    key = jsonReader.nextString();
                                    break;

                                case "id":
                                    value = jsonReader.nextInt();
                                    break;

                                default:
                                    jsonReader.skipValue();
                            }
                        }
                        if(key!=null && value!=0)
                            res.put(key, value);
                        jsonReader.endObject();
                    }
                }
                jsonReader.endArray();
            }

            if(res.isEmpty())
                return null;
            else
                return res;
        }
    }
}
