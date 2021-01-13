package com.example.at_proto.ItineraryRelated;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.at_proto.R;

public class TimeDurationPickerDialog extends AlertDialog implements DialogInterface.OnClickListener {

    private View dialogView;
    private EditText durationEd;
    private OnDurationListener durationListener;

    public interface OnDurationListener {
        void onDurationSet(View v, int duration);
    }

    public TimeDurationPickerDialog(Context context, OnDurationListener listener, int duration) {
        super(context);
        durationListener = listener;


        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = li.inflate(R.layout.time_duration_picker, null);
        setView(v);
        setButton(BUTTON_POSITIVE, "OK", this);
        setButton(BUTTON_NEGATIVE, "CANCEL", this);

        durationEd = (EditText) v.findViewById(R.id.time_duration_edit_text);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which){
            case BUTTON_POSITIVE:
                String duration = durationEd.getText().toString();
                if(durationListener!=null && !duration.isEmpty()){
                    durationListener.onDurationSet(dialogView, Integer.parseInt(durationEd.getText().toString()));
                }
                break;
            case BUTTON_NEGATIVE:
                cancel();
        }
    }
}
