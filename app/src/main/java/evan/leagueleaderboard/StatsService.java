package evan.leagueleaderboard;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import constant.Region;
import dto.Stats.AggregatedStats;
import dto.Stats.PlayerStatsSummary;
import dto.Summoner.Summoner;
import evan.leagueleaderboard.data.SummonerContract;
import evan.leagueleaderboard.data.SummonerDbHelper;
import main.java.riotapi.RiotApi;
import main.java.riotapi.RiotApiException;

public class StatsService extends IntentService {
    private final String LOG_TAG = "STATS_SERVICE";
    private SharedPreferences pref;
    private String[] summonerList;
    private final Context mContext;
    private SummonerDbHelper mOpenHelper;
    private Set<String> recordExists;
    private RiotApi api;
    private Set<String> incorrectSummoners;
    private ArrayList<String> needStats;

    public StatsService() {
        super("StatsService");
        mContext = getBaseContext();
    }

    @Override
    public void onHandleIntent(Intent intent){
        String[] params = intent.getStringArrayExtra("evan.leagueleaderboard/.StatsService.summoners");
        incorrectSummoners = new HashSet<>();
        mOpenHelper = new SummonerDbHelper(mContext);
        recordExists = new HashSet<>();
        needStats = new ArrayList<>();



        api = new RiotApi("ef351397-bb4e-4983-979b-b0a23a4d34d8");
        api.setRegion(Region.NA);

        //    ADDING SUMMONERS TO DB
        try {
            addSummoner(params);
        }catch (RiotApiException e ){
            Log.w(LOG_TAG, "Riot Exception thrown from addSummoner");
            return;
        }
        Log.i(LOG_TAG, "Fetching stats for " + String.valueOf(needStats.size()) + " summoners");


        String user = PreferenceManager.getDefaultSharedPreferences(this).getString("User", "lazybum35");
        for(int i = 0; i < needStats.size(); ++i) {
            Summoner summoner;
            Long summonerRow;
            ContentValues statsValues = new ContentValues();
            long statsId = 0;
            PlayerStatsSummary unrankedSummary = null;
            AggregatedStats unrankedStats;
            PlayerStatsSummary rankedSummary =  null;
            AggregatedStats rankedStats = null;

            //Retrieving RiotID to make next API call
            Cursor summonerCursor = getContentResolver().query(
                    SummonerContract.SummonerEntry.CONTENT_URI,
                    new String[]{SummonerContract.SummonerEntry._ID,
                            SummonerContract.SummonerEntry.COLUMN_RIOT_ID},
                    SummonerContract.SummonerEntry.COLUMN_SUMMONER_SETTING  + " = ?",
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
                        try {
                            Cursor userCursor = null;
                            if (needStats.get(i).equals(user)) {
                                userCursor = getContentResolver().query(
                                        SummonerContract.StatsEntry.buildStatsSummoner(user),
                                        StatsFragment.STATS_COLUMNS,
                                        SummonerContract.SummonerEntry.COLUMN_SUMMONER_SETTING + " = ?",
                                        new String[]{user},
                                        null
                                );
                                Log.d(LOG_TAG, DatabaseUtils.dumpCursorToString(userCursor));

                                if(userCursor.moveToFirst()) {
                                    //Sending Notifications of Milestones (if any)
                                    Notifications.Notifications(this, user);
                                    Notifications.Notify(userCursor, unrankedSummary, unrankedStats, rankedSummary, rankedStats);
                                }

                            }
                        }catch (SQLiteException e){

                        }


                        statsId = getContentResolver().update(
                                SummonerContract.StatsEntry.buildStatsUri(),
                                statsValues,
                                SummonerContract.StatsEntry.COLUMN_SUM_KEY + " = ?",
                                new String[]{String.valueOf(summonerCursor.getLong(0))});


                    }
                    else {
                        getContentResolver().insert(
                                SummonerContract.StatsEntry.buildStatsUri(),
                                statsValues
                        );
                        Log.i(LOG_TAG, "stats sucesffuly inserted");
                    }

                    if (statsId > 0) {
                        Log.i(LOG_TAG, "stats successfully updated");
                    }

                }
                catch (RiotApiException e){
                    e.printStackTrace();
                }
            }
            summonerCursor.close();
        }

        scheduleNext();
    }




    private void addSummoner(String[] summonerNames) throws RiotApiException {
        long summonerId = -1;
        String summonersToFetch = "";
        if(summonerNames == null){
            return;
        }

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
        for(;i.hasNext();) {
            Summoner toAdd = i.next().getValue();

            Cursor summonerCursor = getContentResolver().query(
                    SummonerContract.SummonerEntry.CONTENT_URI,
                    new String[]{SummonerContract.SummonerEntry.COLUMN_REVISION_DATE},
                    SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME + " = ?",
                    new String[]{toAdd.getName()},
                    null
            );


            //Creating values to insert to database
            ContentValues summonerValues = new ContentValues();

            if (!summonerCursor.moveToFirst()) {

                summonerValues.put(SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME, toAdd.getName());
                summonerValues.put(SummonerContract.SummonerEntry.COLUMN_SUMMONER_LEVEL, toAdd.getSummonerLevel());
                summonerValues.put(SummonerContract.SummonerEntry.COLUMN_RIOT_ID, toAdd.getId());
                summonerValues.put(SummonerContract.SummonerEntry.COLUMN_PROFILE_ICON, toAdd.getProfileIconId());
                summonerValues.put(SummonerContract.SummonerEntry.COLUMN_SUMMONER_SETTING, toAdd.getName().toLowerCase());
                summonerValues.put(SummonerContract.SummonerEntry.COLUMN_REVISION_DATE, toAdd.getRevisionDate());

                //Insert data into database
                Uri insertedUri = getContentResolver().insert(
                        SummonerContract.SummonerEntry.CONTENT_URI,
                        summonerValues
                );


                Log.i(LOG_TAG, "Summoner:" + toAdd.getName() + " added to database");

                needStats.add(toAdd.getName().toLowerCase());
            } else {
                recordExists.add(toAdd.getName().toLowerCase());
                if (toAdd.getRevisionDate() != summonerCursor.getLong(0)) {
                    needStats.add(toAdd.getName().toLowerCase());

                    summonerValues.put(SummonerContract.SummonerEntry.COLUMN_REVISION_DATE, toAdd.getRevisionDate());
                    int updatedRows = getContentResolver().update(SummonerContract.SummonerEntry.CONTENT_URI,
                            summonerValues,
                            SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME + " = ?",
                            new String[]{toAdd.getName()});

                }
                summonerCursor.close();
            }
        }

    }

    private void scheduleNext(){
        Intent intent = new Intent(this, this.getClass());
        PendingIntent pendingIntent =
                PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //getting refrsh rate
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Integer refreshRate = Integer.valueOf(pref.getString("Refresh_Rate_Pref", "1800000"));

        long currentTimeMillis = System.currentTimeMillis();
        long nextUpdateTimeMillis = currentTimeMillis + refreshRate;
        Time nextUpdateTime = new Time();
        nextUpdateTime.set(nextUpdateTimeMillis);


        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, nextUpdateTimeMillis, pendingIntent);
    }
}
