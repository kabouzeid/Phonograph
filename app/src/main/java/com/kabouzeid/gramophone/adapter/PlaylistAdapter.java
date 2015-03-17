package com.kabouzeid.gramophone.adapter;

import android.app.Activity;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.NavigationUtil;

import java.util.List;

/**
 * Created by karim on 16.03.15.
 */
public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    public static final String TAG = PlaylistAdapter.class.getSimpleName();
    protected Activity activity;
    protected List<Playlist> dataSet;

    public PlaylistAdapter(Activity activity, List<Playlist> objects) {
        this.activity = activity;
        dataSet = objects;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_list_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.playlistName.setText(dataSet.get(position).playlistName);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView playlistName;

        public ViewHolder(View itemView) {
            super(itemView);
            playlistName = (TextView) itemView.findViewById(R.id.playlist_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Pair[] sharedViews = null;
            if (activity instanceof AbsFabActivity) sharedViews = ((AbsFabActivity)activity).getSharedViewsWithFab(sharedViews);
            NavigationUtil.goToPlaylist(activity, dataSet.get(getPosition()).id, sharedViews);
        }
    }
}
