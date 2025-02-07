package evan.leagueleaderboard.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import evan.leagueleaderboard.data.SummonerContract.SummonerEntry;
import evan.leagueleaderboard.data.SummonerContract.StatsEntry;
/**
 * Created by Evan on 8/28/2015.
 */
public class SummonerDbHelper extends SQLiteOpenHelper{
    String TAG = "DbHelper";
    private static final int DATABASE_VERSION = 8;

    static final String DATABASE_NAME = "summoner.db";

    public SummonerDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase){
        //Creating table

        final String SQL_CREATE_SUMMONTER_TABLE = "CREATE TABLE "
                + SummonerEntry.TABLE_NAME + " (" + SummonerEntry._ID + " INTEGER PRIMARY KEY," +
                SummonerEntry.COLUMN_RIOT_ID + " INTEGER NOT NULL," +
                SummonerEntry.COLUMN_SUMMONER_NAME + " TEXT NOT NULL," +
                SummonerEntry.COLUMN_SUMMONER_LEVEL + " INTEGER NOT NULL," +
                SummonerEntry.COLUMN_PROFILE_ICON + " INTEGER NOT NULL," +
                SummonerEntry.COLUMN_SUMMONER_SETTING + " TEXT UNIQUE NOT NULL," +
                SummonerEntry.COLUMN_REVISION_DATE + " LONG NOT NULL" +
                " );";

        final String SQL_CREATE_STATS_TABLE = "CREATE TABLE " + StatsEntry.TABLE_NAME + " (" +
                StatsEntry._ID + " INTEGER PRIMARY KEY," +
                StatsEntry.COLUMN_SUM_KEY + " INTEGER NOT NULL," +
                StatsEntry.COLUMN_UNR_WINS + " INTEGER NOT NULL," +
                StatsEntry.COLUMN_UNR_KILLS + " INTEGER NOT NULL," +
                StatsEntry.COLUMN_UNR_ASSISTS + " INTEGER NOT NULL," +
                StatsEntry.COLUMN_UNR_MINIONS + " INTEGER NOT NULL," +
                StatsEntry.COLUMN_UNR_NEUTRAL + " INTEGER NOT NULL," +
                StatsEntry.COLUMN_UNR_TURRETS + " INTEGER NOT NULL," +
                StatsEntry.COLUMN_UNR_KILLS_AVG + " REAL NOT NULL," +
                StatsEntry.COLUMN_UNR_ASSISTS_AVG + " REAL NOT NULL," +
                StatsEntry.COLUMN_UNR_MINIONS_AVG + " REAL NOT NULL," +
                StatsEntry.COLUMN_UNR_NEUTRAL_AVG + " REAL NOT NULL," +
                StatsEntry.COLUMN_UNR_TURRETS_AVG + " REAL NOT NULL," +

                StatsEntry.COLUMN_RANK_WINS + " INTEGER NOT NULL," +
                StatsEntry.COLUMN_RANK_LOSSES + " INTEGER NOT NULL," +
                StatsEntry.COLUMN_RANK_KILLS + " INTEGER NOT NULL," +
                StatsEntry.COLUMN_RANK_ASSISTS + " INTEGER NOT NULL," +
                StatsEntry.COLUMN_RANK_MINIONS + " INTEGER NOT NULL," +
                StatsEntry.COLUMN_RANK_NEUTRAL + " INTEGER NOT NULL," +
                StatsEntry.COLUMN_RANK_TURRETS + " INTEGER NOT NULL," +
                StatsEntry.COLUMN_RANK_KILLS_AVG + " REAL NOT NULL," +
                StatsEntry.COLUMN_RANK_ASSISTS_AVG + " REAL NOT NULL," +
                StatsEntry.COLUMN_RANK_MINIONS_AVG + " REAL NOT NULL," +
                StatsEntry.COLUMN_RANK_NEUTRAL_AVG + " REAL NOT NULL," +
                StatsEntry.COLUMN_RANK_TURRETS_AVG + " REAL NOT NULL," +

                " FOREIGN KEY (" + StatsEntry.COLUMN_SUM_KEY + ") REFERENCES " +
                SummonerEntry.TABLE_NAME + " (" + SummonerEntry._ID +"), " +

                //asuuring no duplicates and replacing in case of conflict
                " UNIQUE (" + StatsEntry.COLUMN_SUM_KEY + ") ON CONFLICT REPLACE);";


        sqLiteDatabase.execSQL(SQL_CREATE_SUMMONTER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_STATS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SummonerEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + StatsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public String getTableAsString(SQLiteDatabase db, String tableName) {
        Log.d(TAG, "getTableAsString called");
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows  = db.rawQuery("SELECT * FROM " + tableName, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }

        return tableString;
    }

}
