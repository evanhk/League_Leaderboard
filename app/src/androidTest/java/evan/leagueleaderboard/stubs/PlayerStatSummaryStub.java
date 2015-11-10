package evan.leagueleaderboard.stubs;

import dto.Stats.AggregatedStats;
import dto.Stats.PlayerStatsSummary;

/**
 * Created by Evan on 11/6/2015.
 */
public class PlayerStatSummaryStub extends PlayerStatsSummary {
    private static final long serialVersionUID = -3584187392263947778L;
    private AggregatedStats aggregatedStats;
    private int losses;
    private int wins;
    private long modifyDate;
    private String playerStatSummaryType;

    public PlayerStatSummaryStub(AggregatedStats as, int losses, int wins) {
        this.losses = losses;
        this.wins = wins;
        this.aggregatedStats = as;
    }

    public AggregatedStats getAggregatedStats() {
        return this.aggregatedStats;
    }

    public int getLosses() {
        return this.losses;
    }

    public int getWins() {
        return this.wins;
    }

    public long getModifyDate() {
        return this.modifyDate;
    }

    public String getPlayerStatSummaryType() {
        return this.playerStatSummaryType;
    }

}
