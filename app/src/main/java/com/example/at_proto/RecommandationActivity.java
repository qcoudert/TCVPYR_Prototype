package com.example.at_proto;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

import com.example.at_proto.RecommandationRelated.DisplayRecoActivity;
import com.example.at_proto.RecommandationRelated.ExpandableListAdapter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommandationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommandation);
    }

    public void onClickExit(View v) {
        finish();
    }

    public void recoThemeClick(View v) {
        Intent i = new Intent(this, DisplayRecoActivity.class);
        i.putExtra(DisplayRecoActivity.RECO_TYPE_KEY, DisplayRecoActivity.RECO_TYPE_THEME);
        startActivity(i);
    }

    public void recoHistoClick(View v) {
        Intent i = new Intent(this, DisplayRecoActivity.class);
        i.putExtra(DisplayRecoActivity.RECO_TYPE_KEY, DisplayRecoActivity.RECO_TYPE_HISTO);
        startActivity(i);
    }
}