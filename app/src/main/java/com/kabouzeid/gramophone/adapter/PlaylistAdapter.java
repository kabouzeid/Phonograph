package com.kabouzeid.gramophone.adapter;

import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MenuItemClickHelper;
import com.kabouzeid.gramophone.loader.PlaylistLoader;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.NavigationUtil;

import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    public static final String TAG = PlaylistAdapter.class.getSimpleName();
    protected final AppCompatActivity activity;
    protected List<Playlist> dataSet;

    public PlaylistAdapter(AppCompatActivity activity) {
        this.activity = activity;
        loadDataSet();
    }

    public void loadDataSet() {
        dataSet = PlaylistLoader.getAllPlaylists(activity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_list_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.playlistName.setText(dataSet.get(position).name);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView playlistName;
        private final ImageView menu;

        public ViewHolder(View itemView) {
            super(itemView);
            playlistName = (TextView) itemView.findViewById(R.id.playlist_name);
            menu = (ImageView) itemView.findViewById(R.id.menu);
            itemView.setOnClickListener(this);
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.inflate(R.menu.menu_item_playlist);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            return MenuItemClickHelper.handlePlaylistMenuClick(
                                    activity, dataSet.get(getAdapterPosition()), item);
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        @Override
        public void onClick(View view) {
            Pair[] sharedViews = null;
            if (activity instanceof AbsFabActivity)
                sharedViews = ((AbsFabActivity) activity).getSharedViewsWithFab(null);
            NavigationUtil.goToPlaylist(activity, dataSet.get(getAdapterPosition()).id, sharedViews);
        }
    }
}
