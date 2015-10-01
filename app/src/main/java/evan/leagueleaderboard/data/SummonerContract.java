package evan.leagueleaderboard.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Evan on 8/28/2015.
 */
public class SummonerContract {
    public static final String CONTENT_AUTHORITY = "evan.leagueleaderboard";

    //Base URI which used to contact the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //POSSIBLE PATHS (yes only one right now but who knows....)
    public static final String PATH_SUMMONER = "summoner";
    public static final String PATH_STATS = "stats";

    public static  final class SummonerEntry implements BaseColumns{

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SUMMONER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUMMONER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +CONTENT_AUTHORITY +"/" + PATH_SUMMONER;

        //TABLE NAME
        public  static final String TABLE_NAME = "summoner";

        //String sent to RIOT API as summoner query
        public static final String COLUMN_SUMMONER_SETTING = "summoner_setting";

        public static final String COLUMN_SUMMONER_NAME = "summoner_name";
        public static final String COLUMN_RIOT_ID = "riot_id";
        public static final String COLUMN_SUMMONER_LEVEL = "summoner_level";
        public static final String COLUMN_PROFILE_ICON= "profile_icon";


        // id is the table id ' _ID'
        public static Uri buildSummonerUri (long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

    public static final class StatsEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STATS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STATS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +CONTENT_AUTHORITY + "/" + PATH_STATS;

        public static final String TABLE_NAME = "stats";

        public static final String COLUMN_SUM_KEY = "summoner_id";

        public static final String COLUMN_UNR_WINS = "unranked_wins";
        public static final String COLUMN_UNR_NEUTRAL = "unranked_neutral_minions";
        public static final String COLUMN_UNR_MINIONS = "unranked_minions_killed";
        public static final String COLUMN_UNR_KILLS = "unranked_champion_kills";
        public static final String COLUMN_UNR_ASSISTS = "unranked_assists";
        public static final String COLUMN_UNR_TURRETS = "unranked_turrets_killed";

        public static final String COLUMN_RANK_WINS = "ranked_wins"   ;
        public static final String COLUMN_RANK_NEUTRAL = "ranked_neutral_minions";
        public static final String COLUMN_RANK_MINIONS = "ranked_minions_killed";
        public static final String COLUMN_RANK_KILLS =   "ranked_champion_kills";
        public static final String COLUMN_RANK_ASSISTS = "ranked_assists";
        public static final String COLUMN_RANK_TURRETS = "ranked_turrets_killed";

        public static Uri buildStatsUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static  Uri buildStatsUri(){
            return CONTENT_URI;
        }

        public static String getSummonerSettingFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static Uri buildStatsSummoner(String summonerSetting){
            return CONTENT_URI.buildUpon().appendPath(summonerSetting).build();
        }






    }
}
