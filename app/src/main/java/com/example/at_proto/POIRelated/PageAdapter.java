package com.example.at_proto.POIRelated;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PageAdapter extends FragmentPagerAdapter {

    private List<POI> pois;

    public PageAdapter(FragmentManager manager, List<POI> pois){
        super(manager);
        this.pois = pois;
    }

    @Override
    public Fragment getItem(int i) {
        return PageFragment.newInstance(i, pois.get(i));
    }

    @Override
    public int getCount() {
        return pois.size();
    }
}
