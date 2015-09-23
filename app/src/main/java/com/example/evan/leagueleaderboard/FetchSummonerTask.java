package com.example.evan.leagueleaderboard;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import dto.Stats.AggregatedStats;
import dto.Stats.PlayerStatsSummary;
import dto.Stats.PlayerStatsSummaryList;
import dto.Summoner.Summoner;
import constant.Region;
import main.java.riotapi.RiotApi;
import main.java.riotapi.RiotApiException;

import com.example.evan.leagueleaderboard.data.SummonerDbHelper;
import com.google.gson.*;

import com.example.evan.leagueleaderboard.data.SummonerContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;

/**
 * Created by Evan on 8/30/2015.
 */
public class FetchSummonerTask extends AsyncTask<String[], Void, Void> {

    private final String LOG_TAG = FetchSummonerTask.class.getSimpleName();

    private final Context mContext;
    private SummonerDbHelper mOpenHelper;
    private boolean recordExists = false;

    public FetchSummonerTask (Context context){mContext = context;}

    long addSummoner(Summoner summoner){
        long summonerId = -1;

       // Checking if Summoner is already in database
        Cursor summonerCursor = mContext.getContentResolver().query(
                SummonerContract.SummonerEntry.CONTENT_URI,
                new String[] {SummonerContract.SummonerEntry._ID},
                SummonerContract.SummonerEntry.COLUMN_SUMMONER_SETTING + " = ?",
                new String[]{summoner.getName().toLowerCase()},
                null
        );

        if(summonerCursor.moveToFirst()){
            int summonerIdIndex = summonerCursor.getColumnIndex(SummonerContract.SummonerEntry._ID);
            summonerId = summonerCursor.getLong(summonerIdIndex);
            recordExists = true;
            Log.i(LOG_TAG, "Summoner" + summoner + " already in database ");
        } else{
            //Creating values to insert to database
            ContentValues summonerValues = new ContentValues();

            summonerValues.put(SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME, summoner.getName());
            summonerValues.put(SummonerContract.SummonerEntry.COLUMN_SUMMONER_LEVEL, summoner.getSummonerLevel());
            summonerValues.put(SummonerContract.SummonerEntry.COLUMN_RIOT_ID, summoner.getId());
            summonerValues.put(SummonerContract.SummonerEntry.COLUMN_PROFILE_ICON, summoner.getProfileIconId());
            summonerValues.put(SummonerContract.SummonerEntry.COLUMN_SUMMONER_SETTING, summoner.getName().toLowerCase());

            //Insert data into database
            Uri insertedUri = mContext.getContentResolver().insert(
                    SummonerContract.SummonerEntry.CONTENT_URI,
                    summonerValues
            );


            Log.i(LOG_TAG, "Summoner:"  + summoner +" added to database");

            summonerId = ContentUris.parseId(insertedUri);
        }

        summonerCursor.close();
        return summonerId;
    }

   @Override
    protected Void doInBackground(String[]... params){
       if(params[0].length == 0){
           return null;
       }

       mOpenHelper = new SummonerDbHelper(mContext);



       RiotApi api = new RiotApi("ef351397-bb4e-4983-979b-b0a23a4d34d8");
       api.setRegion(Region.NA);


       for(int i = 0; i < params[0].length; ++i) {
           Summoner summoner;
           Long summonerRow;
           ContentValues statsValues = new ContentValues();
           long statsId = 0;
           PlayerStatsSummary unrankedSummary = null;
           AggregatedStats unrankedStats;
           PlayerStatsSummary rankedSummary =  null;
           AggregatedStats rankedStats;

           try {

               summoner = api.getSummonerByName(params[0][i]);
               summonerRow = addSummoner(summoner);



               List<PlayerStatsSummary> statsList = api.getPlayerStatsSummary(summoner.getId())
                       .getPlayerStatSummaries();


               for(int j = 0; j < statsList.size(); ++ j) {
                   if(statsList.get(j).getPlayerStatSummaryType().equals("Unranked"))
                        {unrankedSummary = statsList.get(j);}
                   if (statsList.get(j).getPlayerStatSummaryType().equals("RankedSolo5x5"))
                        {rankedSummary = statsList.get(j);}

               }
               if(unrankedSummary == null || rankedSummary == null){
                   throw new RiotApiException(123);
               }
               unrankedStats = unrankedSummary.getAggregatedStats();
               rankedStats = rankedSummary.getAggregatedStats();
               //Inserting desired stats to ContentValues
               statsValues.put(SummonerContract.StatsEntry.COLUMN_SUM_KEY, summonerRow);
               statsValues.put(SummonerContract.StatsEntry.COLUMN_UNR_WINS, unrankedSummary.getWins());
               statsValues.put(SummonerContract.StatsEntry.COLUMN_UNR_KILLS, unrankedStats.getTotalChampionKills());
               statsValues.put(SummonerContract.StatsEntry.COLUMN_UNR_ASSISTS, unrankedStats.getTotalAssists());
               statsValues.put(SummonerContract.StatsEntry.COLUMN_UNR_MINIONS, unrankedStats.getTotalMinionKills());
               statsValues.put(SummonerContract.StatsEntry.COLUMN_UNR_NEUTRAL, unrankedStats.getTotalNeutralMinionsKilled());
               statsValues.put(SummonerContract.StatsEntry.COLUMN_UNR_TURRETS, unrankedStats.getTotalTurretsKilled());

               statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_WINS, rankedSummary.getWins());
               statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_KILLS, rankedStats.getTotalChampionKills());
               statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_ASSISTS, rankedStats.getTotalAssists());
               statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_MINIONS, rankedStats.getTotalMinionKills());
               statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_NEUTRAL, rankedStats.getTotalNeutralMinionsKilled());
               statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_TURRETS, rankedStats.getTotalTurretsKilled());


               if (recordExists){
                   statsId = mContext.getContentResolver().update(
                           SummonerContract.StatsEntry.buildStatsUri(),
                           statsValues,
                           SummonerContract.StatsEntry.COLUMN_SUM_KEY + " = ?",
                           new String[]{String.valueOf(summonerRow)});
                   recordExists = false;
               }
               else{
                   mContext.getContentResolver().insert(
                           SummonerContract.StatsEntry.buildStatsUri(),
                           statsValues
                   );
                   Log.i(LOG_TAG, "stats sucesffuly updated");
               }

               if (statsId > 0) {
                   Log.i(LOG_TAG, "stats successfully inserted");
               }

           } catch (RiotApiException e) {
               Log.e(LOG_TAG, e.getMessage(), e);
               e.printStackTrace();
           }


           Log.d(LOG_TAG,
                   mOpenHelper.getTableAsString(mOpenHelper.getReadableDatabase(),
                           SummonerContract.StatsEntry.TABLE_NAME));
           Log.d(LOG_TAG,
                   mOpenHelper.getTableAsString(mOpenHelper.getReadableDatabase(),
                           SummonerContract.SummonerEntry.TABLE_NAME));
       }
    return null;
   }



}
