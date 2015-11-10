package evan.leagueleaderboard;

/**
 * Created by Evan on 11/4/2015.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import dto.Stats.AggregatedStats;
import dto.Stats.PlayerStatsSummary;
import evan.leagueleaderboard.data.SummonerContract;

/**
 *  class for displaying notifications based on milestones reached
 *
 *  <p>
 *      Notifications will be sent for reaching multiples of 100 thru 1000 and then every 1000
 *      for each stat.
 *  </p>
 *
 */
public class Notifications {
    private static Context mContext;
    private static String mUser;

    public static void Notifications(Context context, String user){
        mContext = context;
        mUser = user;
    }

    public static void Notify(Cursor old,
                              PlayerStatsSummary unrankedSummaryNew, AggregatedStats unrankedStatsNew,
                              PlayerStatsSummary rankedSummaryNew, AggregatedStats rankedStatsNew){

        Integer[] milestones = new Integer[12];

        Cursor summonerCursor = mContext.getContentResolver().query(
                SummonerContract.StatsEntry.buildStatsUri(),
                StatsFragment.STATS_COLUMNS,
                SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME  + " = ?",
                new String[]{mUser},
                null
        );

        old.moveToFirst();


        //NORMALS MILESTONES
        milestones[0] = compare(old.getInt(StatsFragment.COL_UNR_WINS), unrankedSummaryNew.getWins());
        milestones[1] = compare(old.getInt(StatsFragment.COL_UNR_KILLS), unrankedStatsNew.getTotalChampionKills());
        milestones[2] = compare(old.getInt(StatsFragment.COL_UNR_ASSISTS), unrankedStatsNew.getTotalAssists());
        milestones[3] = compareCS(old.getInt(StatsFragment.COL_UNR_MINIONS), unrankedStatsNew.getTotalMinionKills());
        milestones[4] = compare(old.getInt(StatsFragment.COL_UNR_NEUTRAL), unrankedStatsNew.getTotalNeutralMinionsKilled());
        milestones[5] = compare(old.getInt(StatsFragment.COL_UNR_TURRETS), unrankedStatsNew.getTotalTurretsKilled());

        if(rankedStatsNew != null) {
            //RANKED MILESTONES
            milestones[6] = compare(old.getInt(StatsFragment.COL_RANK_WINS), rankedSummaryNew.getWins());
            milestones[7] = compare(old.getInt(StatsFragment.COL_RANK_KILLS), rankedStatsNew.getTotalChampionKills());
            milestones[8] = compare(old.getInt(StatsFragment.COL_RANK_ASSISTS), rankedStatsNew.getTotalAssists());
            milestones[9] = compareCS(old.getInt(StatsFragment.COL_RANK_MINIONS), rankedStatsNew.getTotalMinionKills());
            milestones[10] = compare(old.getInt(StatsFragment.COL_RANK_NEUTRAL), rankedStatsNew.getTotalNeutralMinionsKilled());
            milestones[11] = compare(old.getInt(StatsFragment.COL_RANK_TURRETS), rankedStatsNew.getTotalTurretsKilled());
        }

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        for (int i = 0; i <milestones.length ; i++) {
            if( milestones[i] == null || milestones[i] == -1 ){
                continue;
            }
            String message = "Congratuations on reaching " + milestones[i] + " in "
                    + milestoneStrings[i];
            PendingIntent contentIntent = PendingIntent.getActivity(mContext,0,
                    new Intent(mContext,MainActivity.class),0);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(mContext)
                    .setContentTitle("League Leaderboard")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_ll)
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.ic_braum_square));
            mBuilder.setContentIntent(contentIntent);
            notificationManager.notify(i, mBuilder.build());

            Log.d("STATS_SERVICE", notificationManager.toString());
        }


    }

    /**
     *
     * @param oldValue the old value of a statistic
     * @param newValue the new value of a statistic
     * @return -1 if 100,200,300,400,500,600,700,800,900 or a multiple of 1000 isn't reached on
     *           this update.
     *          else it returns the milestone reached
     */
    private static int compare(int oldValue, int newValue){
        if(oldValue < 1000){
            if(Math.round(oldValue / 100.0) < Math.round(newValue/100.0)){
                return (int) Math.round((newValue/100.0)) * 100;
            }
        }else{
            if(Math.round(oldValue / 1000.0) < Math.round(newValue/1000.0)){
                return (int) Math.round((newValue/1000.0))* 1000;
            }
        }
        return -1;
    }

    /**
     *
     * @param oldValue the old value of CS
     * @param newValue the new value of CS
     * @return The milestone if reached on this update , else returns 1
     */
    private static int compareCS(int oldValue, int newValue){
        if(oldValue < 100000){
            if(Math.round(oldValue/10000.0) < Math.round(newValue/10000.0)){
                return (int) Math.round(newValue/10000.0) * 10000;
            }
        }else{
            if(Math.round(oldValue/25000.0) < Math.round(newValue/25000.0)){
                return (int) Math.round(newValue/25000.0) * 25000;
            }
        }
        return newValue;
    }

    private static final String[] milestoneStrings = new String[]{
        "Unranked Wins",
        "Unranked Kills",
        "Unranked Assists",
        "Unranked Creep Score",
        "Unranked Neutrals Killed",
        "Unranked Turrets Destroyed",
        "Ranked Wins",
        "Ranked Kills",
        "Ranked Assists",
        "Ranked Creep Score",
        "Ranked Neutrals Killed",
        "Ranked Turrets Killed" };
}
