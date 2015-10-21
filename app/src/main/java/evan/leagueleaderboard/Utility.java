package evan.leagueleaderboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Iterator;

import evan.leagueleaderboard.data.SummonerContract;

/**
 * Created by Evan on 9/24/2015.
 */
public class Utility {

    public static String getSortOrder(Context context, Integer sort){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        String queueType = pref.getString("Queue_Type", "unranked");
        Integer selectedStat = sort;

        switch (selectedStat){
            case R.id.summoner_header:
                return SummonerContract.SummonerEntry.COLUMN_SUMMONER_SETTING + " DESC";
            case R.id.kills_header:
                if(queueType.equals("unranked")){
                    return SummonerContract.StatsEntry.COLUMN_UNR_KILLS + " DESC";
                }
                else if (queueType.equals("ranked")){
                    return SummonerContract.StatsEntry.COLUMN_RANK_KILLS + " DESC";
                }
                else{
                    return SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME + " ASC";
                }
            case R.id.assists_header:
                if(queueType.equals("unranked")){
                    return SummonerContract.StatsEntry.COLUMN_UNR_ASSISTS + " DESC";
                }
                else if (queueType.equals("ranked")){
                    return SummonerContract.StatsEntry.COLUMN_RANK_ASSISTS + " DESC";
                }
                else{
                    return SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME + " ASC";
                }

            case R.id.wins_header:
                if(queueType.equals("unranked")){
                    return SummonerContract.StatsEntry.COLUMN_UNR_WINS + " DESC";
                }
                else if (queueType.equals("ranked")){
                    return SummonerContract.StatsEntry.COLUMN_RANK_WINS + " DESC";
                }
                else{
                    return SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME + " ASC";
                }


            case R.id.cs_header:
                if(queueType.equals("unranked")){
                    return SummonerContract.StatsEntry.COLUMN_UNR_MINIONS + " DESC";
                }
                else if (queueType.equals("ranked")){
                    return SummonerContract.StatsEntry.COLUMN_RANK_MINIONS + " DESC";
                }
                else{
                    return SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME + " ASC";
                }

            case R.id.neutrals_header:
                if(queueType.equals("unranked")){
                    return SummonerContract.StatsEntry.COLUMN_UNR_NEUTRAL + " DESC";
                }
                else if (queueType.equals("ranked")){
                    return SummonerContract.StatsEntry.COLUMN_RANK_NEUTRAL + " DESC";
                }
                else{
                    return SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME + " ASC";
                }

            case R.id.turrets_header:
                if(queueType.equals("unranked")){
                    return SummonerContract.StatsEntry.COLUMN_UNR_TURRETS + " DESC";
                }
                else if (queueType.equals("ranked")){
                    return SummonerContract.StatsEntry.COLUMN_RANK_TURRETS + " DESC";
                }
                else{
                    return SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME + " ASC";
                }

            default:
                return SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME + " DESC";
        }

    }

    public static String formatSummonersToString(String[] summoners){
        String returnString = "";
        int i = 0;
        for (; i < summoners.length -1; ++i){
            returnString = returnString + summoners[i] + ",";
        }
        returnString = returnString + summoners [i];

        return returnString;
    }
}
