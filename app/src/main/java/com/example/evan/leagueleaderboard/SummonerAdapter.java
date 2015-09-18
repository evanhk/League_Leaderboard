package com.example.evan.leagueleaderboard;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import dto.Static.Stats;

/**
 * Created by Evan on 9/2/2015.
 */
public class SummonerAdapter extends CursorAdapter{
    public SummonerAdapter(Context context, Cursor c, int flags)
        {super(context,c,flags);}

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent){
        //int viewType = getItemViewType(cursor.getPosition());
        int layoutId =  R.layout.unranked_list_item;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);


        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor){
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.summonerView.setText(
               cursor.getString(StatsFragment.COL_SUMMONER_NAME)
        );

        viewHolder.winsView.setText(
                String.valueOf(cursor.getInt(StatsFragment.COL_UNR_WINS))
        );

        viewHolder.killsView.setText(
                String.valueOf(cursor.getInt(StatsFragment.COL_UNR_KILLS))
        );
        viewHolder.assistsView.setText(
                String.valueOf(cursor.getInt(StatsFragment.COL_UNR_ASSISTS))
        );
        viewHolder.minionsView.setText(
                String.valueOf(cursor.getInt(StatsFragment.COL_UNR_MINIONS))
        );
        viewHolder.neutralsView.setText(
                String.valueOf(cursor.getInt(StatsFragment.COL_UNR_NEUTRAL))
        );
        viewHolder.turretsView.setText(
                String.valueOf(cursor.getInt(StatsFragment.COL_UNR_TURRETS))
        );
    }

    public static class ViewHolder {
        public final TextView summonerView;
        public final TextView winsView;
        public final TextView killsView;
        public final TextView assistsView;
        public final TextView minionsView;
        public final TextView neutralsView;
        public final TextView turretsView;

        public ViewHolder(View view){
            summonerView = (TextView) view.findViewById(R.id.unranked_summoner_textview);
            winsView = (TextView) view.findViewById(R.id.unranked_wins_textview);
            killsView = (TextView) view.findViewById(R.id.unranked_kills_textview);
            assistsView = (TextView) view.findViewById(R.id.unranked_assists_textview);
            minionsView = (TextView) view.findViewById(R.id.unranked_minions_textview);
            neutralsView = (TextView) view.findViewById(R.id.unranked_neutrals_textview);
            turretsView = (TextView) view.findViewById(R.id.unranked_towers_textview);
        }
    }
}
