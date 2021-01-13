package com.example.at_proto.POIRelated;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.at_proto.DownloadIntentService;
import com.example.at_proto.MainActivity;
import com.example.at_proto.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PopUpActivity extends AppCompatActivity {

    public static final String POI_ID_EXTRA = "poi_id";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up);

        /*Diminue la taille de la fenêtre
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.95), (int)(height*.8));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        getWindow().setAttributes(params);*/

        String poi = getIntent().getStringExtra(POI_ID_EXTRA);
        QueryPOI queryPOI = new QueryPOI();
        queryPOI.getPoiFromID(poi, new Callback<List<POI>>() {
            @Override
            public void onResponse(Call<List<POI>> call, Response<List<POI>> response) {
                if(response.isSuccessful() && response.body()!=null) {
                    ViewPager viewPager = (ViewPager)findViewById(R.id.viewpagerPopup);
                    viewPager.setOffscreenPageLimit(2);
                    viewPager.setAdapter(new PageAdapter(getSupportFragmentManager(), response.body()));

                    TabLayout tabLayout = findViewById(R.id.tablayoutPopup);
                    tabLayout.setupWithViewPager(viewPager, true);

                    for(int i = 0; i < viewPager.getAdapter().getCount(); i++){
                        updateImageView(i, "http://tcvpyr.univ-pau.fr/TCVPyrWebService/resources/poi_image/" + response.body().get(i).getId());
                    }
                }
                else
                    finish();
            }

            @Override
            public void onFailure(Call<List<POI>> call, Throwable t) {
                finish();
            }
        });
    }

    public void updateImageView(int fragPos, String url){
        PendingIntent pendingIntent = createPendingResult(MainActivity.BITMAP_REQUEST_CODE+fragPos, new Intent(), 0);
        Intent intent = new Intent(this, DownloadIntentService.class);
        intent.putExtra(DownloadIntentService.URL_EXTRA, url);
        intent.putExtra(DownloadIntentService.PENDING_RESULT_EXTRA, pendingIntent);
        startService(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("PyrAT", "Received a result: " + resultCode);
        if(requestCode>=MainActivity.BITMAP_REQUEST_CODE && requestCode<=MainActivity.BITMAP_REQUEST_CODE+9){
            if(resultCode==DownloadIntentService.RESULT_CODE){
                Bitmap bm = (Bitmap) data.getParcelableExtra(DownloadIntentService.BITMAP_EXTRA);
                Bitmap resizedBm;

                Log.d("DebugImage", "Width: "+bm.getWidth()+" Height: "+bm.getHeight());
                if (bm.getWidth()>bm.getHeight()) {
                    resizedBm = Bitmap.createScaledBitmap(bm, 640, 480, false);
                }
                else if (bm.getHeight()>bm.getWidth()){
                    resizedBm = Bitmap.createScaledBitmap(bm, 480, 640, false);
                }
                else{
                    resizedBm = Bitmap.createScaledBitmap(bm, 480, 480, false);
                }

                PageFragment fragment = (PageFragment)getSupportFragmentManager().getFragments().get(requestCode-MainActivity.BITMAP_REQUEST_CODE);
                ImageView imageView = (ImageView) fragment.getView().findViewById(R.id.poiImage);
                imageView.setImageBitmap(resizedBm);

                bm.recycle();
            }
        }

    }
}
