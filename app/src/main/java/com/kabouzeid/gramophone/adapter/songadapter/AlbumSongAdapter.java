package com.kabouzeid.gramophone.adapter.songadapter;

import android.support.v7.app.ActionBarActivity;
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
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.MusicUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumSongAdapter extends RecyclerView.Adapter<AlbumSongAdapter.ViewHolder> {

    public static final String TAG = AlbumSongAdapter.class.getSimpleName();
    protected final ActionBarActivity activity;
    protected final ArrayList<Song> dataSet;

    public AlbumSongAdapter(ActionBarActivity activity, ArrayList<Song> objects) {
        this.activity = activity;
        dataSet = objects;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_list_album_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Song song = dataSet.get(position);

        final int trackNumber = MusicUtil.getFixedTrackNumber(song.trackNumber);
        final String trackNumberString = trackNumber > 0 ? String.valueOf(trackNumber) : "-";
        holder.trackNumber.setText(trackNumberString);
        holder.songTitle.setText(song.title);
        holder.artistName.setText(MusicUtil.getReadableDurationString(song.duration));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView songTitle;
        final TextView trackNumber;
        final TextView artistName;
        final ImageView overflowButton;

        public ViewHolder(View itemView) {
            super(itemView);
            songTitle = (TextView) itemView.findViewById(R.id.song_title);
            trackNumber = (TextView) itemView.findViewById(R.id.track_number);
            artistName = (TextView) itemView.findViewById(R.id.song_info);
            overflowButton = (ImageView) itemView.findViewById(R.id.menu);
            overflowButton.setOnClickListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MusicPlayerRemote.openQueue(dataSet, getAdapterPosition(), true);
                }
            });
        }

        @Override
        public void onClick(View v) {
            PopupMenu popupMenu = new PopupMenu(activity, v);
            popupMenu.inflate(R.menu.menu_item_song);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return MenuItemClickHelper.handleSongMenuClick(activity, dataSet.get(getAdapterPosition()), item);
                }
            });
            popupMenu.show();
        }
    }
}
