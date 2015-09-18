package com.example.evan.leagueleaderboard;

import android.content.SharedPreferences;
import android.database.DatabaseUtils;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.evan.leagueleaderboard.data.SummonerContract;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Evan on 9/2/2015.
 */
public class StatsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public StatsFragment(){}
    private static final int SUMMONER_LOADER = 0;

    private static final String[] STATS_COLUMNS = {
            SummonerContract.StatsEntry.TABLE_NAME + "." + SummonerContract.StatsEntry._ID,
            SummonerContract.StatsEntry.COLUMN_SUM_KEY,
            SummonerContract.StatsEntry.COLUMN_UNR_WINS,
            SummonerContract.StatsEntry.COLUMN_UNR_KILLS,
            SummonerContract.StatsEntry.COLUMN_UNR_ASSISTS,
            SummonerContract.StatsEntry.COLUMN_UNR_MINIONS,
            SummonerContract.StatsEntry.COLUMN_UNR_NEUTRAL,
            SummonerContract.StatsEntry.COLUMN_UNR_TURRETS,
            SummonerContract.StatsEntry.COLUMN_RANK_WINS,
            SummonerContract.StatsEntry.COLUMN_RANK_KILLS,
            SummonerContract.StatsEntry.COLUMN_RANK_ASSISTS,
            SummonerContract.StatsEntry.COLUMN_RANK_MINIONS,
            SummonerContract.StatsEntry.COLUMN_RANK_NEUTRAL,
            SummonerContract.StatsEntry.COLUMN_RANK_TURRETS,
            SummonerContract.SummonerEntry.TABLE_NAME + "." + SummonerContract.SummonerEntry._ID,
            SummonerContract.SummonerEntry.COLUMN_RIOT_ID,
            SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME,
            SummonerContract.SummonerEntry.COLUMN_SUMMONER_LEVEL
    };

    static final int COL_STATS_ID = 0;
    static final int COL_SUM_KEY = 1;
    static final int COL_UNR_WINS = 2;
    static final int COL_UNR_KILLS = 3;
    static final int COL_UNR_ASSISTS = 4;
    static final int COL_UNR_MINIONS = 5;
    static final int COL_UNR_NEUTRAL = 6;
    static final int COL_UNR_TURRETS = 7;
    static final int COL_RANK_WINS = 8;
    static final int COL_RANK_KILLS = 9;
    static final int COL_RANK_ASSISTS = 10;
    static final int COL_RANK_MINIONS = 11;
    static final int COL_RANK_NEUTRAL = 12;
    static final int COL_RANK_TURRETS = 13;
    static final int COL_SUM_ID = 14;
    static final int COL_RIOT_ID = 15;
    static final int COL_SUMMONER_NAME = 16;
    static final int COL_SUMMONER_LEVEL = 17;

    private SummonerAdapter mSummonerAdapter;
    private String[] summonerList;
    private SharedPreferences pref;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState){
        mSummonerAdapter = new SummonerAdapter(getActivity(), null ,0);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview);

        listView.setAdapter(mSummonerAdapter);
        //TODO add setOnItemClickListener

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        getLoaderManager().initLoader(SUMMONER_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    public void updateDb(){
        FetchSummonerTask summonerTask = new FetchSummonerTask(getActivity());
        // Pretty inefficient, will improve later
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Set<String> s = pref.getStringSet("Add_Summoners_Set", new HashSet<String>());
        String[] summoners = s.toArray(new String[]{});
        summonerTask.execute(summoners);
        //Log.d("Stats tag", "Called asynctask");
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
        updateDb();
    }





    ///////////    LOADER METHODS  //////////////////////////////////
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri tableUri = SummonerContract.StatsEntry.buildStatsUri();
        String sortOrder = SummonerContract.StatsEntry.COLUMN_UNR_KILLS + " DESC";

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
        mSummonerAdapter.swapCursor(cursor);
        Log.d("LOADER_TAG", DatabaseUtils.dumpCursorToString(cursor));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSummonerAdapter.swapCursor(null);
    }
}
