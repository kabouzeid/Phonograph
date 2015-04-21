package com.kabouzeid.gramophone.adapter.songadapter;

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
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.model.PlaylistSong;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PlaylistsUtil;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistSongAdapter extends RecyclerView.Adapter<PlaylistSongAdapter.ViewHolder> {

    public static final String TAG = AlbumSongAdapter.class.getSimpleName();
    protected final AppCompatActivity activity;
    protected final ArrayList<PlaylistSong> dataSet;

    public PlaylistSongAdapter(AppCompatActivity activity, ArrayList<PlaylistSong> objects) {
        this.activity = activity;
        dataSet = objects;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_list_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Song song = dataSet.get(position);

        holder.songTitle.setText(song.title);
        holder.songInfo.setText(song.artistName);

        holder.albumArt.setTag(
                Ion.with(activity)
                        .load(MusicUtil.getAlbumArtUri(song.albumId).toString())
                        .withBitmap()
                        .resize(holder.albumArt.getWidth(), holder.albumArt.getHeight())
                        .centerCrop()
                        .error(R.drawable.default_album_art)
                        .intoImageView(holder.albumArt)
        );
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView songTitle;
        final TextView songInfo;
        final ImageView overflowButton;
        final ImageView albumArt;

        public ViewHolder(View itemView) {
            super(itemView);
            songTitle = (TextView) itemView.findViewById(R.id.song_title);
            songInfo = (TextView) itemView.findViewById(R.id.song_info);
            albumArt = (ImageView) itemView.findViewById(R.id.album_art);
            overflowButton = (ImageView) itemView.findViewById(R.id.menu);
            overflowButton.setOnClickListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //noinspection unchecked
                    MusicPlayerRemote.openQueue((ArrayList<Song>) (List) dataSet, getAdapterPosition(), true);
                }
            });
        }

        @Override
        public void onClick(View v) {
            PopupMenu popupMenu = new PopupMenu(activity, v);
            popupMenu.inflate(R.menu.menu_item_playlist_song);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_delete_from_playlist:
                            int position = getAdapterPosition();
                            PlaylistsUtil.removeFromPlaylist(activity, dataSet.remove(position));
                            notifyItemRemoved(position);
                            return true;
                        case R.id.action_go_to_album:
                            Pair[] albumPairs = new Pair[]{
                                    Pair.create(albumArt, activity.getResources().getString(R.string.transition_album_cover))
                            };
                            if (activity instanceof AbsFabActivity)
                                albumPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(albumPairs);
                            NavigationUtil.goToAlbum(activity, dataSet.get(getAdapterPosition()).albumId, albumPairs);
                            return true;
                    }
                    return MenuItemClickHelper.handleSongMenuClick(activity, dataSet.get(getAdapterPosition()), item);
                }
            });
            popupMenu.show();
        }
    }
}
