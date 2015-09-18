package com.example.evan.leagueleaderboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.EditText;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Evan on 9/14/2015.
 */
public class AddSummonerPref extends EditTextPreference {
    public AddSummonerPref(Context context, AttributeSet attr){
        super(context, attr);
    }

    private Set<String> mSummonerSet;

    @Override
    protected void onDialogClosed(boolean positiveResult){
        super.onDialogClosed(positiveResult);
        if (positiveResult){
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = pref.edit();
            Set<String> values =
                    getSharedPreferences().getStringSet("Add_Summoners_Set", new HashSet<String>());
            values.add(getText());

            editor.putStringSet("Add_Summoners_Set", values);
            editor.apply();
        }
    }


}
