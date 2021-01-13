package com.example.at_proto.RecommandationRelated;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.at_proto.R;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> dataHeaders;
    private Map<String, List<String>> listHashMap;
    private Map<String, Boolean> checkedBoxes;


    public ExpandableListAdapter(Context context, List<String> dataHeaders, Map<String, List<String>> listHashMap, Map<String, Boolean> checkedBoxes){
        this.context = context;
        this.dataHeaders = dataHeaders;
        this.listHashMap = listHashMap;
        this.checkedBoxes = checkedBoxes;
    }

    @Override
    public int getGroupCount() {
        return dataHeaders.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if(listHashMap.get(dataHeaders.get(groupPosition))!=null) {
            return listHashMap.get(dataHeaders.get(groupPosition)).size();
        }
        else{
            return 0;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        return dataHeaders.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listHashMap.get(dataHeaders.get(groupPosition)).get(childPosition);
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

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.expandable_list_view_group, null);
        }

        TextView groupLabel = (TextView)convertView.findViewById(R.id.groupTextView);
        groupLabel.setText((String)getGroup(groupPosition));

        ImageView gp = (ImageView) convertView.findViewById(R.id.group_indicator);
        CheckBox cb = (CheckBox) convertView.findViewById(R.id.groupCheckBox);
        if(getChildrenCount(groupPosition)==0){
            gp.setVisibility(View.INVISIBLE);
            cb.setVisibility(View.VISIBLE);
            cb.setContentDescription((String)getGroup(groupPosition));
            if(checkedBoxes.get(getGroup(groupPosition)))
                cb.setChecked(true);
            else
                cb.setChecked(false);
            cb.setOnCheckedChangeListener(new CheckBoxFunction());
        }
        else{
            gp.setVisibility(View.VISIBLE);
            cb.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if(getChildrenCount(groupPosition)==0)
            return null;

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.expandable_list_view_content, null);
        }
        CheckBox checkBoxChild = (CheckBox)convertView.findViewById(R.id.itemCheckBox);
        checkBoxChild.setText((String)getChild(groupPosition, childPosition));
        if(checkedBoxes.get(getChild(groupPosition, childPosition))) {
            checkBoxChild.setChecked(true);
        }
        else {
            checkBoxChild.setChecked(false);
        }
        checkBoxChild.setOnCheckedChangeListener(new CheckBoxFunction());
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public Map<String, Boolean> getCheckedBoxes() {
        return checkedBoxes;
    }

    private class CheckBoxFunction implements CompoundButton.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(buttonView.getContentDescription()==null)
                checkedBoxes.put(buttonView.getText().toString(), isChecked);
            else
                checkedBoxes.put(buttonView.getContentDescription().toString(), isChecked);
        }
    }
}
