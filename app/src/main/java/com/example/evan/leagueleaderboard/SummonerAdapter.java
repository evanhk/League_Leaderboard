package com.example.evan.leagueleaderboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by Evan on 9/2/2015.
 */
public class SummonerAdapter extends CursorAdapter{
    boolean landsacpe;
    public SummonerAdapter(Context context, Cursor c, int flags)
        {super(context,c,flags);
            Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            if(display.getRotation() != Surface.ROTATION_0
                    && display.getRotation() != Surface.ROTATION_180 ){
                landsacpe = true;
            }
            else{
                landsacpe = false;
            }
        }

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
        String iconURL = "http://ddragon.leagueoflegends.com/cdn/5.18.1/img/profileicon/" +
                String.valueOf(cursor.getInt(StatsFragment.COL_PROFILE_ICON)) +".png";

        //Determining Queue Type
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String qType = pref.getString("Queue_Type", "unranked");

        //Summoner Icon
        Picasso.with(context).load(iconURL).into(viewHolder.iconview);
        //Summoner Name
        viewHolder.summonerView.setText(
               cursor.getString(StatsFragment.COL_SUMMONER_NAME)
        );

        if(qType.equals("unranked")) {
            viewHolder.winsView.setText(
                    String.valueOf(cursor.getInt(StatsFragment.COL_UNR_WINS))
            );

            viewHolder.killsView.setText(
                    String.valueOf(cursor.getInt(StatsFragment.COL_UNR_KILLS))
            );
            viewHolder.assistsView.setText(
                    String.valueOf(cursor.getInt(StatsFragment.COL_UNR_ASSISTS))
            );
            if (landsacpe) {
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
        }
        else if (qType.equals("ranked")){
            viewHolder.winsView.setText(
                    String.valueOf(cursor.getInt(StatsFragment.COL_RANK_WINS))
            );

            viewHolder.killsView.setText(
                    String.valueOf(cursor.getInt(StatsFragment.COL_RANK_KILLS))
            );
            viewHolder.assistsView.setText(
                    String.valueOf(cursor.getInt(StatsFragment.COL_RANK_ASSISTS))
            );
            if (landsacpe) {
                viewHolder.minionsView.setText(
                        String.valueOf(cursor.getInt(StatsFragment.COL_RANK_MINIONS))
                );
                viewHolder.neutralsView.setText(
                        String.valueOf(cursor.getInt(StatsFragment.COL_RANK_NEUTRAL))
                );
                viewHolder.turretsView.setText(
                        String.valueOf(cursor.getInt(StatsFragment.COL_RANK_TURRETS))
                );
            }
        }
    }

    public static class ViewHolder {
        public final ImageView iconview;
        public final TextView summonerView;
        public final TextView winsView;
        public final TextView killsView;
        public final TextView assistsView;
        public final TextView minionsView;
        public final TextView neutralsView;
        public final TextView turretsView;

        public ViewHolder(View view){
            iconview = (ImageView) view.findViewById(R.id.unranked_icon_imageview);
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
