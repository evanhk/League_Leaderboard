package evan.leagueleaderboard;

import android.database.Cursor;
import android.test.AndroidTestCase;
import android.test.mock.*;

import dto.Stats.AggregatedStats;
import dto.Stats.PlayerStatsSummary;
import evan.leagueleaderboard.stubs.AggregatedStatsStub;
import evan.leagueleaderboard.stubs.PlayerStatSummaryStub;

/**
 * Created by Evan on 11/6/2015.
 */
public class NotificationsTest extends AndroidTestCase {
    MockCursor curosr;
    PlayerStatsSummary unrankedSummary;
    PlayerStatsSummary rankedSummary;
    AggregatedStats unrankedStats;
    AggregatedStats rankedStats;


    public void setUp(){
        unrankedStats = new AggregatedStatsStub(600,400,30000,1000,200);
        rankedStats = new AggregatedStatsStub(300,200,20000,1000,100);
        unrankedSummary = new PlayerStatSummaryStub(unrankedStats,200,200);
        rankedSummary = new PlayerStatSummaryStub(rankedStats,100,100);


    }

    public void testNotify(){
         Notifications.Notifications(getContext(),"lazybum35");

    }
}
