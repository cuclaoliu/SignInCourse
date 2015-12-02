package edu.cuc.stephen.signincourse;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.security.acl.Group;

import edu.cuc.stephen.signincourse.utils.AssetsDatabaseManager;

import static edu.cuc.stephen.signincourse.R.id.radio_group;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment {


    public SettingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radio_group);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String dbName = preferences.getString("course", "eda");
        if (dbName.equals("eda"))
            ((RadioButton)view.findViewById(R.id.radio_button_eda)).setChecked(true);
        else if(dbName.equals("mcu"))
            ((RadioButton)view.findViewById(R.id.radio_button_mcu)).setChecked(true);
        else if(dbName.equals("ee2014"))
            ((RadioButton)view.findViewById(R.id.radio_button_2014ee)).setChecked(true);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = preferences.edit();
                if(R.id.radio_button_eda == checkedId){
                    editor.putString("course", "eda");
                    editor.commit();
                }else if(R.id.radio_button_mcu == checkedId){
                    editor.putString("course", "mcu");
                    editor.commit();
                }else if(R.id.radio_button_2014ee == checkedId){
                    editor.putString("course", "ee2014");
                    editor.commit();
                }
            }
        });
        return view;
    }
}
