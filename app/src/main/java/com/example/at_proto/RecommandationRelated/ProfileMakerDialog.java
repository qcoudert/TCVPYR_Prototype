package com.example.at_proto.RecommandationRelated;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.provider.Telephony;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import com.example.at_proto.R;
import com.example.at_proto.RecommandationRelated.PostPOJO.SentProfile;

import java.util.UUID;

public class ProfileMakerDialog extends AlertDialog implements DialogInterface.OnClickListener {

    private View dialogView;
    private OnProfileListener profileListener;


    public interface OnProfileListener {
         void onProfileSet(View v, SentProfile sp);
    }

    public ProfileMakerDialog(Context context,OnProfileListener profileListener) {
        super(context);
        this.profileListener = profileListener;

        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dialogView = layoutInflater.inflate(R.layout.profile_maker_dialog, null);
        setView(dialogView);
        setButton(BUTTON_POSITIVE, context.getString(R.string.ok), this);

    }
    
    @Override
    public void onClick(DialogInterface dialog, int which) {
        final RadioGroup rgGender = (RadioGroup) dialogView.findViewById(R.id.radioGroupGender);
        final RadioGroup rgAge = (RadioGroup) dialogView.findViewById(R.id.radioGroupAge);

        String gender = "homme";
        int age_category = SentProfile.ADULT_ID;

                if (profileListener!=null) {
                    switch (rgGender.getCheckedRadioButtonId()) {
                        case R.id.radio_gender_homme:
                            gender = "homme";
                            break;
                        case R.id.radio_gender_femme:
                            gender = "femme";
                            break;
                    }

                    switch (rgAge.getCheckedRadioButtonId()) {
                        case R.id.radio_button_jeune:
                            age_category = SentProfile.YOUNG_ID;
                            break;
                        case R.id.radio_button_adulte:
                            age_category = SentProfile.ADULT_ID;
                            break;
                        case R.id.radio_button_senior:
                            age_category = SentProfile.SENIOR_ID;
                            break;
                    }

                    profileListener.onProfileSet(dialogView, new SentProfile(UUID.randomUUID().toString(), gender, age_category));
                }
    }
}
