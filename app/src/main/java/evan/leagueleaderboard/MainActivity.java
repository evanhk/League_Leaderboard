package evan.leagueleaderboard;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;


public class MainActivity extends ActionBarActivity  {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new StatsFragment()).commit();
        }


    }

    public void onClickAddSummoner(View arg0){
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.addsummonerpref, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.add_summoner_prompt_editText);

        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(userInput.getText() == null){
                            return;
                        }
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = pref.edit();
                        Set<String> values =
                                pref.getStringSet("Add_Summoners_Set", new HashSet<String>());
                        String toAdd = String.valueOf(userInput.getText()).toLowerCase();
                        if(!values.contains(toAdd)) {
                            values.add(toAdd);
                        }
                        editor.putStringSet("Add_Summoners_Set", values);
                        editor.apply();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void onClickRemoveSummoner(View arg0){
        View parent = (View) arg0.getParent();
        TextView removeView = (TextView) parent.findViewById(R.id.unranked_summoner_textview);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> summoners =  new HashSet<>();
        summoners.add(removeView.getText().toString().toLowerCase());
        Log.d("ONCLICKREMOVESUMMONER:", "removing summoner:" + removeView.getText().toString());

        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet("Remove_Summoner", summoners);
        editor.apply();



    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            startActivity(new Intent(this, SettingsActivity.class));
//            return true;
//        }
//
//
//
//
//
//
//
//        return super.onOptionsItemSelected(item);
//    }


}
