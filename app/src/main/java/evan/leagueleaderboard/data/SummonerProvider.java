package evan.leagueleaderboard.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Evan on 9/1/2015.
 */
public class SummonerProvider extends ContentProvider{

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private SummonerDbHelper mOpenHelper;

    static final int STATS = 1000;
    static final int STATS_WITH_SUMMONER = 1001;
    static final int SUMMONER = 2000;

    private static final SQLiteQueryBuilder sStatsQueryBuilder;

    static {
        sStatsQueryBuilder = new SQLiteQueryBuilder();

        //Inner join between two tables
        sStatsQueryBuilder.setTables(
                SummonerContract.StatsEntry.TABLE_NAME + " INNER JOIN " +
                        SummonerContract.SummonerEntry.TABLE_NAME +
                        " ON " + SummonerContract.StatsEntry.TABLE_NAME +
                        "." + SummonerContract.StatsEntry.COLUMN_SUM_KEY +
                        " = " + SummonerContract.SummonerEntry.TABLE_NAME +
                        "." + SummonerContract.SummonerEntry._ID
        );
    }

    private static final String sSummonerSettingSelection =
            SummonerContract.SummonerEntry.TABLE_NAME +
                    "." + SummonerContract.SummonerEntry.COLUMN_SUMMONER_SETTING + " = ?";

    private Cursor getStatsBySummonerSetting(Uri uri, String[] projection, String sortOrder){
        String summonerSetting = SummonerContract.StatsEntry.getSummonerSettingFromUri(uri);
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        return sStatsQueryBuilder.query(
                db,
            null,
            sSummonerSettingSelection,
            new String[] {summonerSetting},
            null,
            null,
            sortOrder
        );
    }

    static UriMatcher buildUriMatcher(){

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = SummonerContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, SummonerContract.PATH_STATS, STATS);
        matcher.addURI(authority, SummonerContract.PATH_STATS + "/*", STATS_WITH_SUMMONER);
        matcher.addURI(authority, SummonerContract.PATH_SUMMONER, SUMMONER);

        return matcher;
    }

    @Override
    public boolean onCreate(){
        mOpenHelper = new SummonerDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri){
        final int match = sUriMatcher.match(uri);

        switch (match){
            case STATS_WITH_SUMMONER:
                return SummonerContract.StatsEntry.CONTENT_ITEM_TYPE;
            case STATS:
                return SummonerContract.StatsEntry.CONTENT_TYPE;
            case SUMMONER:
                return SummonerContract.SummonerEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unkown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder){
        Cursor returnCursor;

        switch (sUriMatcher.match(uri)){

            case STATS_WITH_SUMMONER:
                returnCursor = getStatsBySummonerSetting(uri, projection, sortOrder);
                Log.d("LOADER_TAG", DatabaseUtils.dumpCursorToString(returnCursor));
                break;
            case STATS:
                returnCursor = sStatsQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        sortOrder
                );
                Log.d("LOADER_TAG", DatabaseUtils.dumpCursorToString(returnCursor));
                break;
            case SUMMONER:
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        SummonerContract.SummonerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            default:
                throw new UnsupportedOperationException("Unkown uri:" + uri);

        }
        Log.d("LOADER_TAG", "Query uri: " + uri);
        returnCursor.setNotificationUri(getContext().getContentResolver(), SummonerContract.BASE_CONTENT_URI);
        return returnCursor;

    }

    @Override
    public Uri insert(Uri uri, ContentValues values){
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match){
            case STATS: {
                long _id = db.insert(SummonerContract.StatsEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = SummonerContract.StatsEntry.buildStatsUri(_id);
                else
                    throw new SQLException("Failed to insert row into  " + uri);
                break;
            }
            case SUMMONER: {
                long _id = db.insert(SummonerContract.SummonerEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = SummonerContract.SummonerEntry.buildSummonerUri(_id);
                else
                    throw new SQLException("Failed to insert row into  " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        Log.d("LOADER_TAG", "Insert uri: " + uri);
        getContext().getContentResolver().notifyChange(returnUri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs){
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if( selection == null) selection = "1";

        switch (match){
            case STATS:
                rowsDeleted = db.delete(SummonerContract.StatsEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case SUMMONER:
                rowsDeleted = db.delete(SummonerContract.SummonerEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unkown Uri: "+ uri);
        }
        if( rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            Log.d("LOADER_TAG", "Delete uri: " + uri);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        if( selection == null) selection = "1";

        switch (match){
            case STATS:
                rowsUpdated = db.update(SummonerContract.StatsEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case SUMMONER:
                rowsUpdated = db.update(SummonerContract.SummonerEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unkown Uri: "+ uri);
        }
        if( rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }



}
