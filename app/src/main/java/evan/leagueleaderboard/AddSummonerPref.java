package evan.leagueleaderboard;

import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Evan on 9/14/2015.
 */
public class AddSummonerPref extends EditTextPreference {
    private final Context mcontext;

    public AddSummonerPref(Context context, AttributeSet attr){
        super(context, attr);
        mcontext = context;
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
            if(!values.contains(getText().toLowerCase())) {
                values.add(getText().toLowerCase());
            }
            editor.putStringSet("Add_Summoners_Set", values);
            editor.apply();


            //manually notifying listener of change



        }
    }


}
