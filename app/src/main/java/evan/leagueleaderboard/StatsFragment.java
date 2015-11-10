package evan.leagueleaderboard;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DatabaseUtils;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;

import evan.leagueleaderboard.data.SummonerContract;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Evan on 9/2/2015.
 */
public class StatsFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    public StatsFragment(){}
    private static final int SUMMONER_LOADER = 0;

    public static final String[] STATS_COLUMNS = {
            SummonerContract.StatsEntry.TABLE_NAME + "." + SummonerContract.StatsEntry._ID,
            SummonerContract.StatsEntry.COLUMN_SUM_KEY,
            SummonerContract.StatsEntry.COLUMN_UNR_WINS,
            SummonerContract.StatsEntry.COLUMN_UNR_KILLS,
            SummonerContract.StatsEntry.COLUMN_UNR_ASSISTS,
            SummonerContract.StatsEntry.COLUMN_UNR_MINIONS,
            SummonerContract.StatsEntry.COLUMN_UNR_NEUTRAL,
            SummonerContract.StatsEntry.COLUMN_UNR_TURRETS,
            SummonerContract.StatsEntry.COLUMN_RANK_WINS,
            SummonerContract.StatsEntry.COLUMN_RANK_LOSSES,
            SummonerContract.StatsEntry.COLUMN_RANK_KILLS,
            SummonerContract.StatsEntry.COLUMN_RANK_ASSISTS,
            SummonerContract.StatsEntry.COLUMN_RANK_MINIONS,
            SummonerContract.StatsEntry.COLUMN_RANK_NEUTRAL,
            SummonerContract.StatsEntry.COLUMN_RANK_TURRETS,
            SummonerContract.StatsEntry.COLUMN_UNR_KILLS_AVG,
            SummonerContract.StatsEntry.COLUMN_UNR_ASSISTS_AVG,
            SummonerContract.StatsEntry.COLUMN_UNR_MINIONS_AVG,
            SummonerContract.StatsEntry.COLUMN_UNR_NEUTRAL_AVG,
            SummonerContract.StatsEntry.COLUMN_UNR_TURRETS_AVG,
            SummonerContract.StatsEntry.COLUMN_RANK_KILLS_AVG,
            SummonerContract.StatsEntry.COLUMN_RANK_ASSISTS_AVG,
            SummonerContract.StatsEntry.COLUMN_RANK_MINIONS_AVG,
            SummonerContract.StatsEntry.COLUMN_RANK_NEUTRAL_AVG,
            SummonerContract.StatsEntry.COLUMN_RANK_TURRETS_AVG,
            SummonerContract.SummonerEntry.TABLE_NAME + "." + SummonerContract.SummonerEntry._ID,
            SummonerContract.SummonerEntry.COLUMN_RIOT_ID,
            SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME,
            SummonerContract.SummonerEntry.COLUMN_SUMMONER_LEVEL,
            SummonerContract.SummonerEntry.COLUMN_PROFILE_ICON
    };

    static final int COL_STATS_ID = 0;
    static final int COL_SUM_KEY = 1;
    static final int COL_UNR_WINS = 2;
    static final int COL_UNR_KILLS = 3;
    static final int COL_UNR_ASSISTS = 4;
    static final int COL_UNR_MINIONS = 5;
    static final int COL_UNR_NEUTRAL = 6;
    static final int COL_UNR_TURRETS = 7;
    static final int COL_UNR_KILLS_AVG = 8;
    static final int COL_UNR_ASSISTS_AVG = 9;
    static final int COL_UNR_MINIONS_AVG = 10;
    static final int COL_UNR_NEUTRAL_AVG = 11;
    static final int COL_UNR_TURRETS_AVG = 12;
    static final int COL_RANK_WINS = 13;
    static final int COL_RANK_LOSSES = 14;
    static final int COL_RANK_KILLS = 15;
    static final int COL_RANK_ASSISTS = 16;
    static final int COL_RANK_MINIONS = 17;
    static final int COL_RANK_NEUTRAL = 18;
    static final int COL_RANK_TURRETS = 19;
    static final int COL_RANK_KILLS_AVG = 20;
    static final int COL_RANK_ASSISTS_AVG = 21;
    static final int COL_RANK_MINIONS_AVG = 22;
    static final int COL_RANK_NEUTRAL_AVG = 23;
    static final int COL_RANK_TURRETS_AVG = 24;
    static final int COL_SUM_ID = 25;
    static final int COL_RIOT_ID = 26;
    static final int COL_SUMMONER_NAME = 27;
    static final int COL_SUMMONER_LEVEL = 28;
    static final int COL_PROFILE_ICON = 29;

    private SummonerAdapter mSummonerAdapter;
    private String[] summonerList;
    private SharedPreferences pref;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        pref.registerOnSharedPreferenceChangeListener(mPrefsListener);

        Intent update = new Intent(getActivity(),StatsService.class);
        getActivity().startService(update);



        updateDb();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        pref.unregisterOnSharedPreferenceChangeListener(mPrefsListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.updateDB){
            updateDb();
            return true;
        }
        if (id == R.id.clearDB){
            clearDB();
            return true;
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        if (id == R.id.per_game_average){
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Boolean current = pref.getBoolean("game_averages", false);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("game_averages", !current);
            editor.apply();

            Bundle b = new Bundle();
            b.putInt("sortOrder", R.id.kills_header);
            getLoaderManager().restartLoader(SUMMONER_LOADER,b,this);


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        mSummonerAdapter = new SummonerAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview);

        listView.setAdapter(mSummonerAdapter);
        View addButtonView = inflater.inflate(R.layout.add_summoner_button,null);
        listView.addFooterView(addButtonView);
        addButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LayoutInflater li = LayoutInflater.from(getActivity());
                View promptsView = li.inflate(R.layout.addsummonerpref, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView.findViewById(R.id.add_summoner_prompt_editText);

                alertDialogBuilder.setCancelable(false)
                        .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(userInput.getText() == null){
                                    return;
                                }
                                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                SharedPreferences.Editor editor = pref.edit();
                                Set<String> values =
                                        pref.getStringSet("Add_Summoners_Set", new HashSet<String>());
                                String toAdd = String.valueOf(userInput.getText()).toLowerCase();
                                if(!values.contains(toAdd)) {
                                    values.add(toAdd);
                                }
                                editor.putStringSet("Add_Summoners_Set", values);
                                editor.apply();
                                updateDb();
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

        });

        // SETTING LISTENERS FOR COLUMN HEADERS TO SORT
        TextView click = (TextView) rootView.findViewById(R.id.summoner_header);
        click.setOnClickListener(this);
        click = (TextView) rootView.findViewById(R.id.wins_header);
        click.setOnClickListener(this);
        click = (TextView) rootView.findViewById(R.id.kills_header);
        click.setOnClickListener(this);
        click = (TextView) rootView.findViewById(R.id.assists_header);
        click.setOnClickListener(this);
        click = (TextView) rootView.findViewById(R.id.cs_header);
        if (click != null){
            click.setOnClickListener(this);
            click = (TextView) rootView.findViewById(R.id.neutrals_header);
            click.setOnClickListener(this);
            click = (TextView) rootView.findViewById(R.id.turrets_header);
            click.setOnClickListener(this);
        }



        if(pref.getBoolean("firstRun",true)){
            View promptsView = inflater.inflate(R.layout.first_run, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

            alertDialogBuilder.setView(promptsView);

            final EditText userInput = (EditText) promptsView.findViewById(R.id.first_run_prompt_editText);

            alertDialogBuilder.setCancelable(false)
                    .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(userInput.getText() == null){
                                return;
                            }
                            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            SharedPreferences.Editor editor = pref.edit();
                            Set<String> values =
                                    pref.getStringSet("Add_Summoners_Set", new HashSet<String>());
                            String toAdd = String.valueOf(userInput.getText()).toLowerCase();
                            if(!values.contains(toAdd)) {
                                values.add(toAdd);
                            }
                            editor.putStringSet("Add_Summoners_Set", values);
                            editor.putString("User",toAdd);
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

            pref.edit().putBoolean("firstRun",false);
            pref.edit().apply();

        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        //Bundling Default Sort order
        Bundle defaultSortOrder = new Bundle();
        defaultSortOrder.putInt("sortOrder", R.id.kills_header);

        getLoaderManager().initLoader(SUMMONER_LOADER, defaultSortOrder, this);
        super.onActivityCreated(savedInstanceState);
    }



    public  void updateDb(){

        if(mSummonerAdapter != null) {
            mSummonerAdapter.closeAllItems();
        }

        // Pretty inefficient, will improve later
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Set<String> s = pref.getStringSet("Add_Summoners_Set", new HashSet<String>());

        //Removing Summoners
        //NOTE: not the cleanest implementation, TODO improve
        Set<String> rem = pref.getStringSet("Remove_Summoner", new HashSet<String>());
        ContentResolver cr = getActivity().getContentResolver();

        for (Iterator<String> i = rem.iterator() ; i.hasNext() ; ){
            String toDelete = i.next();
            Cursor se = cr.query(SummonerContract.SummonerEntry.CONTENT_URI, new String[]{SummonerContract.SummonerEntry._ID},
                    SummonerContract.SummonerEntry.COLUMN_SUMMONER_SETTING + " = ?",
                    new String[]{toDelete},null);
            if(se.moveToNext()) {
                cr.delete(SummonerContract.SummonerEntry.CONTENT_URI,
                        SummonerContract.SummonerEntry.COLUMN_SUMMONER_SETTING + " = ?",
                        new String[]{toDelete}
                );
                cr.delete(SummonerContract.StatsEntry.CONTENT_URI,
                        SummonerContract.StatsEntry.COLUMN_SUM_KEY + " = ?",
                        new String[]{String.valueOf(se.getInt(0))}
                ); //TODO make add summoners only accept lower case
            }

        }
        s.removeAll(rem);

        //Saving correct ADD_SUMMONERS_PREF and clearing REMOVE SUMMOENRS
        SharedPreferences.Editor editor = pref.edit();
        editor.putStringSet("Add_Summoners_Set", s);
        editor.putStringSet("Remove_Summoner", new HashSet<String>());
        editor.apply();

        String[] summoners = s.toArray(new String[]{});
        summonerList = summoners;

        Intent update = new Intent(getActivity(), StatsService.class);
        update.putExtra("evan.leagueleaderboard/.StatsService.summoners", summoners);
        getActivity().startService(update);



        if(mSummonerAdapter != null) {
            mSummonerAdapter.closeAllItems();
        }
    }

    public void clearDB(){
        getActivity().getContentResolver().delete(SummonerContract.SummonerEntry.CONTENT_URI
                ,null,null);
        getActivity().getContentResolver().delete(SummonerContract.StatsEntry.CONTENT_URI
            ,null,null);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = pref.edit();
        editor.putStringSet("Add_Summoners_Set", new HashSet<String>(){});
        editor.apply();
    }


    @Override
    public void onResume(){
        super.onResume();

        // UGLY way to listen for change in ADD_SUMMONERS_SET
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Set<String> s = pref.getStringSet("Add_Summoners_Set", new HashSet<String>());
        String[] prefSet = s.toArray(new String[]{});
        if(summonerList != prefSet ){
            updateDb();
        }
    }





    public void onClick(View v){
        int viewId = v.getId();
        Bundle order = new Bundle();
        order.putInt("sortOrder",viewId);
        getLoaderManager().restartLoader(SUMMONER_LOADER,order,this);
    }



    private final SharedPreferences.OnSharedPreferenceChangeListener mPrefsListener=
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals("Add_Summoners_Set")) {
                        updateDb();
                        Log.d("LISTENER_TAG", "Add_Summoners_Set recognized and updateDb() called");
                    } else if (key.equals("Remove_Summoner")) {
                        Log.d("Preference Listener", "Remove_Summoner recognized and updatedDb() called");
                        updateDb();
                    } else if (key.equals("Queue_Type")) {
                        mSummonerAdapter.notifyDataSetChanged();
                    }
                    else{

                    }
                }
            };

    ///////////    LOADER METHODS  //////////////////////////////////
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri tableUri = SummonerContract.StatsEntry.buildStatsUri();
        String sortOrder = Utility.getSortOrder(getActivity(),args.getInt("sortOrder"));

        return new CursorLoader(getActivity(),
                tableUri,
                STATS_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(mSummonerAdapter != null) {
            mSummonerAdapter.closeAllItems();
        }

        mSummonerAdapter.swapCursor(cursor);

        Log.d("LOADER_TAG", DatabaseUtils.dumpCursorToString(cursor));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSummonerAdapter.swapCursor(null);
    }
}
