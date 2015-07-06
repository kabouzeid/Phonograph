package com.kabouzeid.gramophone.adapter.songadapter;

import android.support.annotation.Nullable;
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
import com.kabouzeid.gramophone.adapter.AbsMultiSelectAdapter;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.DeleteSongsDialog;
import com.kabouzeid.gramophone.helper.MenuItemClickHelper;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.MusicUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumSongAdapter extends AbsMultiSelectAdapter<AlbumSongAdapter.ViewHolder, Song> {

    public static final String TAG = AlbumSongAdapter.class.getSimpleName();
    protected final AppCompatActivity activity;
    protected ArrayList<Song> dataSet;

    public AlbumSongAdapter(AppCompatActivity activity, ArrayList<Song> objects, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        dataSet = objects;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).id;
    }

    public void updateDataSet(ArrayList<Song> objects){
        dataSet = objects;
        notifyDataSetChanged();
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
        holder.view.setActivated(isChecked(song));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected Song getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected void onMultipleItemAction(MenuItem menuItem, ArrayList<Song> selection) {
        switch (menuItem.getItemId()) {
            case R.id.action_delete_from_disk:
                DeleteSongsDialog.create(selection).show(activity.getSupportFragmentManager(), "DELETE_SONGS");
                break;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(selection).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                break;
            case R.id.action_add_to_current_playing:
                MusicPlayerRemote.enqueue(selection);
                break;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        final TextView songTitle;
        final TextView trackNumber;
        final TextView artistName;
        final ImageView overflowButton;
        final View view;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            songTitle = (TextView) itemView.findViewById(R.id.song_title);
            trackNumber = (TextView) itemView.findViewById(R.id.track_number);
            artistName = (TextView) itemView.findViewById(R.id.song_info);
            overflowButton = (ImageView) itemView.findViewById(R.id.menu);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            overflowButton.setOnClickListener(new View.OnClickListener() {
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
            });
        }

        @Override
        public void onClick(View v) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                MusicPlayerRemote.openQueue(dataSet, getAdapterPosition(), true);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
