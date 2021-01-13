package com.example.at_proto;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Permet de télécharger une image de façon asynchrone.
 * Il est nécessaire de passer un URL_EXTRA contenant l'url de l'image et un PENDING_RESULT_EXTRA content un PendingIntent
 * @author qcoudert
 */
public class DownloadIntentService extends IntentService {

    public static final String URL_EXTRA = "url";
    public static final String PENDING_RESULT_EXTRA = "pending_result";
    public static final String BITMAP_EXTRA = "bitmap";

    public static final int RESULT_CODE = 0;
    public static final int ERROR_CODE = -1;


    public DownloadIntentService(){
        super(DownloadIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PendingIntent reply = intent.getParcelableExtra(PENDING_RESULT_EXTRA);

            try {
                try {
                    URL url = new URL(intent.getStringExtra(URL_EXTRA));
                    Bitmap bm = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                    Intent result = new Intent();
                    result.putExtra(BITMAP_EXTRA, bm);

                    reply.send(this, RESULT_CODE, result);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("PyrAT", e.getMessage());
                    reply.send(ERROR_CODE);
                }
            } catch (PendingIntent.CanceledException exc) {
                Log.i(DownloadIntentService.class.getSimpleName(), "reply cancelled", exc);
            }
    }
}
