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
        boolean average = pref.getBoolean("game_averages", false);
        String order;

        switch (selectedStat){
            case R.id.summoner_header:
                return   SummonerContract.SummonerEntry.COLUMN_SUMMONER_SETTING  + " ASC";
            case R.id.kills_header:
                if(queueType.equals("unranked")){
                    order =  SummonerContract.StatsEntry.COLUMN_UNR_KILLS ;
                }
                else if (queueType.equals("ranked")){
                    order =  SummonerContract.StatsEntry.COLUMN_RANK_KILLS ;
                }
                else{
                    order =  SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME ;
                }
                break;
            case R.id.assists_header:
                if(queueType.equals("unranked")){
                    order =  SummonerContract.StatsEntry.COLUMN_UNR_ASSISTS ;
                }
                else if (queueType.equals("ranked")){
                    order =  SummonerContract.StatsEntry.COLUMN_RANK_ASSISTS ;
                }
                else{
                    order =  SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME ;
                }
                break;
            case R.id.wins_header:
                if(queueType.equals("unranked")){
                    return   SummonerContract.StatsEntry.COLUMN_UNR_WINS + " DESC";
                }
                else if (queueType.equals("ranked")){
                    return   SummonerContract.StatsEntry.COLUMN_RANK_WINS + " DESC";
                }
                else{
                    order =  SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME ;
                }
                break;


            case R.id.cs_header:
                if(queueType.equals("unranked")){
                    order =  SummonerContract.StatsEntry.COLUMN_UNR_MINIONS ;
                }
                else if (queueType.equals("ranked")){
                    order =  SummonerContract.StatsEntry.COLUMN_RANK_MINIONS ;
                }
                else{
                    order =  SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME ;
                }
                break;

            case R.id.neutrals_header:
                if(queueType.equals("unranked")){
                    order =  SummonerContract.StatsEntry.COLUMN_UNR_NEUTRAL ;
                }
                else if (queueType.equals("ranked")){
                    order =  SummonerContract.StatsEntry.COLUMN_RANK_NEUTRAL ;
                }
                else{
                    order =  SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME ;
                }
                break;

            case R.id.turrets_header:
                if(queueType.equals("unranked")){
                    order =  SummonerContract.StatsEntry.COLUMN_UNR_TURRETS ;
                }
                else if (queueType.equals("ranked")){
                    order =  SummonerContract.StatsEntry.COLUMN_RANK_TURRETS ;
                }
                else{
                    order =  SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME ;
                }
                break;

            default:
                return   SummonerContract.SummonerEntry.COLUMN_SUMMONER_NAME  + " ASC";
        }

        if(average){
            order = order + "_average";
        }
        order = order + " DESC";

        return order;

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
