package com.example.at_proto;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.at_proto.ItineraryRelated.ItineraryRequest;

import java.io.File;

public class ItHubActivity extends AppCompatActivity {

    public static final int ITINERARY_GAVE = 420;
    public static final String ITINERARY_STRING_KEY = "iti_string_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_it_hub);
    }

    @Override
    protected void onResume() {
        super.onResume();

        File folder = this.getDir("itineraries", MODE_PRIVATE);
        String[] list_view_content = folder.list();

        if(list_view_content.length==0)
            list_view_content = new String[]{"Itinéraire exemple"};

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, list_view_content);

        final ListView listView = (ListView)findViewById(R.id.iti_list_view);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String value = (String)listView.getItemAtPosition(i);
                Log.d("PyrAT","Value: " + value);
                ItHubActivity.this.setResult(1, new Intent().putExtra(ITINERARY_STRING_KEY, value));
                ItHubActivity.this.finish();
            }
        });
    }

    public void createItinerary(View v) {
        this.startActivity(new Intent(this, ItActivity.class));
    }

    public void onClickExitIti(View v) {
        finish();
    }
}
