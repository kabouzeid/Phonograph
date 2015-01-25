package com.kabouzeid.materialmusic.adapter.songadapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.helper.SongDetailDialogHelper;
import com.kabouzeid.materialmusic.loader.SongFileLoader;
import com.kabouzeid.materialmusic.misc.AppKeys;
import com.kabouzeid.materialmusic.model.Song;
import com.kabouzeid.materialmusic.ui.activities.tageditor.SongTagEditorActivity;
import com.kabouzeid.materialmusic.util.MusicUtil;

import java.io.File;
import java.util.List;

/**
 * Created by karim on 27.11.14.
 */
public class SongAdapter extends ArrayAdapter<Song> {
    public static final String TAG = SongAdapter.class.getSimpleName();
    protected Context context;
    protected GoToAble goToAble;

    public SongAdapter(Context context, GoToAble goToAble, List<Song> objects) {
        super(context, R.layout.item_song, objects);
        this.context = context;
        this.goToAble = goToAble;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Song song = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_song, parent, false);
        }
        TextView songTitle = (TextView) convertView.findViewById(R.id.song_title);
        TextView trackNumber = (TextView) convertView.findViewById(R.id.track_number);
        TextView songDuration = (TextView) convertView.findViewById(R.id.song_duration);
        ImageView overflowButton = (ImageView) convertView.findViewById(R.id.menu);

        overflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.inflate(R.menu.menu_song);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_tag_editor:
                                Intent intent = new Intent(context, SongTagEditorActivity.class);
                                intent.putExtra(AppKeys.E_ID, song.id);
                                context.startActivity(intent);
                                return true;
                            case R.id.action_details:
                                String songFilePath = SongFileLoader.getSongFile(context, song.id);
                                File songFile = new File(songFilePath);
                                SongDetailDialogHelper.getDialog(context, songFile).show();
                                return true;
                            case R.id.action_go_to_album:
                                if (goToAble != null) {
                                    goToAble.goToAlbum(song.albumId);
                                }
                                return true;
                            case R.id.action_go_to_artist:
                                if (goToAble != null) {
                                    goToAble.goToArtist(song.artistId);
                                }
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        songTitle.setText(song.title);
        trackNumber.setText(String.valueOf(MusicUtil.getFixedTrackNumber(song.trackNumber)));
        songDuration.setText(MusicUtil.getReadableDurationString(song.duration));

        return convertView;
    }

    public static interface GoToAble {
        public void goToAlbum(int albumId);

        public void goToArtist(int artistId);
    }

}
