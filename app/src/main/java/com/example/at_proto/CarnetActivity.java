package com.example.at_proto;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.at_proto.CarnetVoyageRelated.DisplayCarnetActivity;

import java.io.File;

public class CarnetActivity extends AppCompatActivity {

    public static final String CARNET_DIRECTORY = "carnets";
    public static final String CARNET_KEY = "cdv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carnet);

        File folder = this.getDir(CARNET_DIRECTORY, MODE_PRIVATE);
        String[] list_view_content = folder.list();

        if(list_view_content.length==0)
            ((TextView)findViewById(R.id.aucun_carnet_tv)).setVisibility(View.VISIBLE);
        else {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, list_view_content);

            final ListView listView = (ListView) findViewById(R.id.cdv_list_view);
            listView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String value = (String) listView.getItemAtPosition(i);
                    Log.d("PyrAT", "Value: " + value);
                    startActivity(new Intent(CarnetActivity.this, DisplayCarnetActivity.class).putExtra(CARNET_KEY, value));
                }
            });
        }

    }

    public void onClickExitCarnetAct(View v) {
        finish();
    }
}
