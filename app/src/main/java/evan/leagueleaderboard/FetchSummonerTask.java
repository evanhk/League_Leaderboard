package evan.leagueleaderboard;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import dto.Static.Stats;
import dto.Stats.AggregatedStats;
import dto.Stats.PlayerStatsSummary;
import dto.Summoner.Summoner;
import constant.Region;
import main.java.riotapi.RiotApi;
import main.java.riotapi.RiotApiException;

import evan.leagueleaderboard.data.SummonerDbHelper;

import evan.leagueleaderboard.data.SummonerContract;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Evan on 8/30/2015.
 */
public class FetchSummonerTask extends AsyncTask<String[], Void, Set<String> > {

    private final String LOG_TAG = FetchSummonerTask.class.getSimpleName();

    private final Context mContext;
    private SummonerDbHelper mOpenHelper;
    private Set<String> recordExists;
    private RiotApi api;
    private Set<String> incorrectSummoners;
    private ArrayList<String> needStats;

    public FetchSummonerTask (Context context){mContext = context;}


    /**
     * Retrieves summoner's info from Riot Server and stores it in database if it
     * is not there already. It also checks if the stored stats need to be updated.
     *
     * @param summonerNames
     * @throws RiotApiException
     */
    void addSummoner(String[] summonerNames) throws RiotApiException{
        long summonerId = -1;
        String summonersToFetch = "";

        for(int i = 0; i < summonerNames.length ; ++i) {
            summonersToFetch = summonersToFetch + summonerNames[i] + ",";
        }


        Map summonerMap = new HashMap();

        summonerMap = api.getSummonersByName(summonersToFetch);
        if(summonerMap == null){
            throw new RiotApiException (1);
        }


        Set<Map.Entry<String,Summoner>> summonerSet = summonerMap.entrySet();
        Iterator<Map.Entry<String,Summoner>> i = summonerSet.iterator();

        //Inserting new Summoner Objects into DB
        for(;i.hasNext();){
            Summoner toAdd = i.next().getValue();

            Cursor summonerCursor = mContext.getContentResolver().query(
                    SummonerContract.SummonerEntry.CONTENT_URI,
                    new String[]{SummonerContract.SummonerEntry.COLUMN_REVISION_DATE},
                    SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME + " = ?",
                    new String[]{toAdd.getName()},
                    null
            );

            //Creating values to insert to database
            ContentValues summonerValues = new ContentValues();

            //Checking to see if data has changed from whats cached
            if(!summonerCursor.moveToFirst() ) {





                summonerValues.put(SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME, toAdd.getName());
                summonerValues.put(SummonerContract.SummonerEntry.COLUMN_SUMMONER_LEVEL, toAdd.getSummonerLevel());
                summonerValues.put(SummonerContract.SummonerEntry.COLUMN_RIOT_ID, toAdd.getId());
                summonerValues.put(SummonerContract.SummonerEntry.COLUMN_PROFILE_ICON, toAdd.getProfileIconId());
                summonerValues.put(SummonerContract.SummonerEntry.COLUMN_SUMMONER_SETTING, toAdd.getName().toLowerCase());
                summonerValues.put(SummonerContract.SummonerEntry.COLUMN_REVISION_DATE, toAdd.getRevisionDate());

                //Insert data into database
                Uri insertedUri = mContext.getContentResolver().insert(
                        SummonerContract.SummonerEntry.CONTENT_URI,
                        summonerValues
                );


                Log.i(LOG_TAG, "Summoner:" + toAdd.getName() + " added to database");

                needStats.add(toAdd.getName());
            }
            else if( toAdd.getRevisionDate() != summonerCursor.getLong(0)){
                needStats.add(toAdd.getName());

                summonerValues.put(SummonerContract.SummonerEntry.COLUMN_REVISION_DATE, toAdd.getRevisionDate());
                int updatedRows =mContext.getContentResolver().update(SummonerContract.SummonerEntry.CONTENT_URI,
                                                        summonerValues,
                        SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME + " = ?",
                        new String[]{toAdd.getName()});

            }

        }

    }

   @Override
    protected Set<String> doInBackground(String[]... params){
       if(params[0] == null || params[0].length == 0){
           return null;
       }



       incorrectSummoners = new HashSet<>();
       mOpenHelper = new SummonerDbHelper(mContext);
       recordExists = new HashSet<>();
       needStats = new ArrayList<>();



       api = new RiotApi("ae96eef8-09be-427d-ac80-a2b961c5c4f7");
       api.setRegion(Region.NA);

       //    ADDING SUMMONERS TO DB
       try {
           addSummoner(params[0]);
       }catch (RiotApiException e ){
           Log.w(LOG_TAG, "Riot Exception thrown from addSummoner");
           return null;
       }
       Log.i(LOG_TAG, "Fetching stats for " + String.valueOf(needStats.size()) + " summoners");
       for(int i = 0; i < needStats.size(); ++i) {
           Summoner summoner;
           Long summonerRow;
           ContentValues statsValues = new ContentValues();
           long statsId = 0;
           PlayerStatsSummary unrankedSummary = null;
           AggregatedStats unrankedStats;
           PlayerStatsSummary rankedSummary =  null;
           AggregatedStats rankedStats;

               //Retrieving RiotID to make next API call
               Cursor summonerCursor = mContext.getContentResolver().query(
                       SummonerContract.SummonerEntry.CONTENT_URI,
                       new String[]{SummonerContract.SummonerEntry._ID,
                               SummonerContract.SummonerEntry.COLUMN_RIOT_ID},
                       SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME  + " = ?",
                       new String[]{needStats.get(i)},
                       null
               );

               if (summonerCursor.moveToFirst()) {
                   try {
                       List<PlayerStatsSummary> statsList =
                               api.getPlayerStatsSummary(summonerCursor.getLong(1))
                                       .getPlayerStatSummaries();

                       if(statsList == null){
                           incorrectSummoners.add(needStats.get(i));
                           continue;
                       }

                       for (int j = 0; j < statsList.size(); ++j) {
                           if (statsList.get(j).getPlayerStatSummaryType().equals("Unranked")) {
                               unrankedSummary = statsList.get(j);
                           }
                           if (statsList.get(j).getPlayerStatSummaryType().equals("RankedSolo5x5")) {
                               rankedSummary = statsList.get(j);
                           }

                       }
                       if (unrankedSummary == null ) {
                           throw new RiotApiException(123);
                       }


                       unrankedStats = unrankedSummary.getAggregatedStats();

                       //Inserting desired stats to ContentValues
                       statsValues.put(SummonerContract.StatsEntry.COLUMN_SUM_KEY, summonerCursor.getLong(0));
                       statsValues.put(SummonerContract.StatsEntry.COLUMN_UNR_WINS, unrankedSummary.getWins());
                       statsValues.put(SummonerContract.StatsEntry.COLUMN_UNR_KILLS, unrankedStats.getTotalChampionKills());
                       statsValues.put(SummonerContract.StatsEntry.COLUMN_UNR_ASSISTS, unrankedStats.getTotalAssists());
                       statsValues.put(SummonerContract.StatsEntry.COLUMN_UNR_MINIONS, unrankedStats.getTotalMinionKills());
                       statsValues.put(SummonerContract.StatsEntry.COLUMN_UNR_NEUTRAL, unrankedStats.getTotalNeutralMinionsKilled());
                       statsValues.put(SummonerContract.StatsEntry.COLUMN_UNR_TURRETS, unrankedStats.getTotalTurretsKilled());

                       //approximate averages for normals
                       double aprxTotalGames = unrankedSummary.getWins() * 2;
                       statsValues.put(SummonerContract.StatsEntry.COLUMN_UNR_KILLS_AVG, unrankedStats.getTotalChampionKills()/ aprxTotalGames);
                       statsValues.put(SummonerContract.StatsEntry.COLUMN_UNR_ASSISTS_AVG, unrankedStats.getTotalAssists() / aprxTotalGames);
                       statsValues.put(SummonerContract.StatsEntry.COLUMN_UNR_MINIONS_AVG, unrankedStats.getTotalMinionKills() / aprxTotalGames);
                       statsValues.put(SummonerContract.StatsEntry.COLUMN_UNR_NEUTRAL_AVG, unrankedStats.getTotalNeutralMinionsKilled() / aprxTotalGames);
                       statsValues.put(SummonerContract.StatsEntry.COLUMN_UNR_TURRETS_AVG, unrankedStats.getTotalTurretsKilled()/ aprxTotalGames);

                       if(rankedSummary.getWins() + rankedSummary.getLosses() != 0){
                           rankedStats = rankedSummary.getAggregatedStats();
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_WINS, rankedSummary.getWins());
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_LOSSES, rankedSummary.getLosses());
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_KILLS, rankedStats.getTotalChampionKills());
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_ASSISTS, rankedStats.getTotalAssists());
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_MINIONS, rankedStats.getTotalMinionKills());
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_NEUTRAL, rankedStats.getTotalNeutralMinionsKilled());
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_TURRETS, rankedStats.getTotalTurretsKilled());

                           //averages
                           double totalGames = rankedSummary.getWins() + rankedSummary.getLosses();
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_KILLS_AVG, rankedStats.getTotalChampionKills()/ totalGames);
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_ASSISTS_AVG, rankedStats.getTotalAssists() / totalGames);
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_MINIONS_AVG, rankedStats.getTotalMinionKills() / totalGames);
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_NEUTRAL_AVG, rankedStats.getTotalNeutralMinionsKilled() / totalGames);
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_TURRETS_AVG, rankedStats.getTotalTurretsKilled()/ totalGames);
                       }
                       else{
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_WINS, 0);
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_LOSSES, 0);
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_KILLS, 0);
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_ASSISTS,0);
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_MINIONS, 0);
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_NEUTRAL, 0);
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_TURRETS, 0);

                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_KILLS_AVG, 0);
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_ASSISTS_AVG,0);
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_MINIONS_AVG, 0);
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_NEUTRAL_AVG, 0);
                           statsValues.put(SummonerContract.StatsEntry.COLUMN_RANK_TURRETS_AVG, 0);
                       }



                       if (recordExists.contains(needStats.get(i))) {
                           statsId = mContext.getContentResolver().update(
                                   SummonerContract.StatsEntry.buildStatsUri(),
                                   statsValues,
                                   SummonerContract.StatsEntry.COLUMN_SUM_KEY + " = ?",
                                   new String[]{String.valueOf(summonerCursor.getLong(0))});
                       } else {
                           mContext.getContentResolver().insert(
                                   SummonerContract.StatsEntry.buildStatsUri(),
                                   statsValues
                           );
                           Log.i(LOG_TAG, "stats sucesffuly updated");
                       }

                       if (statsId > 0) {
                           Log.i(LOG_TAG, "stats successfully inserted");
                       }

                   }
                   catch (RiotApiException e){
                       e.printStackTrace();
                   }
               }


           Log.d(LOG_TAG,
                   mOpenHelper.getTableAsString(mOpenHelper.getReadableDatabase(),
                           SummonerContract.StatsEntry.TABLE_NAME));
           Log.d(LOG_TAG,
                   mOpenHelper.getTableAsString(mOpenHelper.getReadableDatabase(),
                           SummonerContract.SummonerEntry.TABLE_NAME));
       }
    return incorrectSummoners;
   }


    //Displays Toast for invalid summoner names and removes them from preference
    @Override
    public void onPostExecute(Set<String> invalidSummoners){
        if(invalidSummoners != null) {
            Iterator<String> it = invalidSummoners.iterator();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
            Set<String> s = pref.getStringSet("Add_Summoners_Set", new HashSet<String>());

            for (; it.hasNext(); ) {
                String name = it.next();
                Toast toast = Toast.makeText(mContext, "Invalid Summoner Name: " +
                        name, Toast.LENGTH_LONG);
                toast.show();
                s.remove(name);
            }

            SharedPreferences.Editor editor = pref.edit();
            editor.putStringSet("Add_Summoners_Set", s);
            editor.apply();
        }
    }


}
