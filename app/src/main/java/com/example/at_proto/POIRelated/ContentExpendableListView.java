package com.example.at_proto.POIRelated;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.example.at_proto.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class ContentExpendableListView extends BaseExpandableListAdapter {

    private Context context;
    private List<String> headers;
    private List<String> content;
    private POI poi;

    public ContentExpendableListView(Context context, POI poi) {
        this.context = context;
        this.poi = poi;

        headers = new ArrayList<>();
        content = new ArrayList<>();

        if (poi.getResume()!=null && !poi.getResume().isEmpty()) {
            headers.add(context.getString(R.string.resume));
            content.add(poi.getResume());
        }
        if(poi.getDescription()!=null && !poi.getDescription().isEmpty()) {
            headers.add(context.getString(R.string.desciption));
            content.add(poi.getDescription());
        }
        if(poi.getHistorique()!=null && !poi.getHistorique().isEmpty()) {
            headers.add(context.getString(R.string.historique));
            content.add(poi.getHistorique());
        }
        if(poi.getPersonnes()!=null && !poi.getPersonnes().isEmpty()) {
            headers.add(context.getString(R.string.personnes));
            String personnes = "";
            for (String s : poi.getPersonnes()) {
                personnes = personnes + s + "\n";
            }
            content.add(personnes);
        }

        headers.add(context.getString(R.string.otherInfo));
        content.add("Adresse: " + poi.getAdresse() + "\nType du POI: " + poi.getType() + "\nSaisi le: " + poi.getDateSaisie());
    }

    @Override
    public int getGroupCount() {
        return headers.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return headers.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return content.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if(convertView==null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.content_exp_list_view_group, null);
        }

        TextView groupLabel = (TextView) convertView.findViewById(R.id.content_group_tv);
        groupLabel.setText((String)getGroup(groupPosition));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if(convertView==null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.content_exp_list_view_content, null);
        }

        TextView childContent = (TextView) convertView.findViewById(R.id.content_content_tv);
        childContent.setText((String)getChild(groupPosition, childPosition));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
