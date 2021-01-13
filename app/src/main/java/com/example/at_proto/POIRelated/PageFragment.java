package com.example.at_proto.POIRelated;


import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.at_proto.DownloadIntentService;
import com.example.at_proto.LocationTracking.GeofencingPOI;
import com.example.at_proto.MainActivity;
import com.example.at_proto.R;
import com.example.at_proto.RecommandationRelated.PostPOJO.RatingPOI;
import com.example.at_proto.RecommandationRelated.PostUserInfo;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Une sous-classe de {@link Fragment}.
 * Elle permet d'afficher les informations d'un POI sous la forme d'une fiche à l'aide des informations passées
 * lors de l'instanciation. Ces fragments sont sensés êtres utilisés dans un ViewPager.
 */
public class PageFragment extends Fragment {

    private static final String KEY_POI = "poi";
    private POI poi;

    public PageFragment() {}

    /**
     * Crée une nouvelle instance de PageFragment.
     * @param postion La position du fragment dans le ViewPager.
     * @param poi POI qui sera utilisé pour créer le fragment
     * @return Fragment créé.
     */
    public static PageFragment newInstance(int postion, POI poi) {

        PageFragment fragment = new PageFragment();

        Bundle args = new Bundle();
        args.putParcelable(KEY_POI, poi);

        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_page, container, false);

        poi = getArguments().getParcelable(KEY_POI);

        final TextView poiLabel = (TextView)result.findViewById(R.id.poiLabelTV);
        final RatingBar ratingBar = (RatingBar)result.findViewById(R.id.poi_rating_bar);
        final ExpandableListView listView = (ExpandableListView)result.findViewById(R.id.content_exp_list_view);
        final ImageView imgView = (ImageView) result.findViewById(R.id.poiImage);

        poiLabel.setText(poi.getTitre());

        ContentExpendableListView celv = new ContentExpendableListView(getContext(), poi);
        listView.setAdapter(celv);
        listView.setGroupIndicator(getResources().getDrawable(R.drawable.group_state_list));

        if(GeofencingPOI.getInstance().getVisitingList().contains(poi.getId())) {
            ratingBar.setClickable(true);
            ratingBar.setEnabled(true);
        }
        else {
            ratingBar.setVisibility(View.GONE);
            ratingBar.setEnabled(false);
            ((TextView)result.findViewById(R.id.ratingLabel)).setVisibility(View.GONE);
        }

        return result;
    }

    @Override
    public void onStop() {
        super.onStop();
        RatingBar rt = (RatingBar)getView().findViewById(R.id.poi_rating_bar);
        if(rt.isEnabled()) {
            if(rt.getRating()>=1) {
                RatingPOI ratingPOI = new RatingPOI(MainActivity.USER_ID, (int) rt.getRating(), poi.getId(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                Log.d("PyrAT", "POI rated: " + ratingPOI);
                PostUserInfo post = new PostUserInfo();
                post.postPOIRating(ratingPOI);
                //TODO: Stocker même en cas de réussite
            }
        }
    }
}
