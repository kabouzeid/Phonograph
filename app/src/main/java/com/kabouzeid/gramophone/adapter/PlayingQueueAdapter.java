package com.kabouzeid.gramophone.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.SongDetailDialogHelper;
import com.kabouzeid.gramophone.loader.SongFilePathLoader;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.tageditor.SongTagEditorActivity;
import com.kabouzeid.gramophone.util.NavigationUtil;

import java.io.File;
import java.util.List;

/**
 * Created by karim on 24.01.15.
 */
public class PlayingQueueAdapter extends ArrayAdapter<Song> {
    private Activity activity;

    public PlayingQueueAdapter(Activity activity, List<Song> playList) {
        super(activity, R.layout.item_list_playlist, playList);
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Song song = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.item_list_playlist, parent, false);
        }
        final TextView title = (TextView) convertView.findViewById(R.id.song_title);
        final ImageView playingIndicator = (ImageView) convertView.findViewById(R.id.playing_indicator);
        final ImageView overflowButton = (ImageView) convertView.findViewById(R.id.menu);

        title.setText(song.title);
        if (MusicPlayerRemote.getPosition() == position) {
            playingIndicator.setVisibility(View.VISIBLE);
            playingIndicator.setImageResource(R.drawable.ic_speaker_white_48dp);
        } else {
            playingIndicator.setVisibility(View.GONE);
            playingIndicator.setImageBitmap(null);
        }

        overflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                PopupMenu popupMenu = new PopupMenu(activity, v);
                popupMenu.inflate(R.menu.menu_song);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_tag_editor:
                                Intent intent = new Intent(activity, SongTagEditorActivity.class);
                                intent.putExtra(AppKeys.E_ID, song.id);
                                activity.startActivity(intent);
                                return true;
                            case R.id.action_details:
                                String songFilePath = SongFilePathLoader.getSongFilePath(activity, song.id);
                                File songFile = new File(songFilePath);
                                SongDetailDialogHelper.getDialog(activity, songFile).show();
                                return true;
                            case R.id.action_go_to_album:
                                NavigationUtil.goToAlbum(activity, song.albumId, null);
                                return true;
                            case R.id.action_go_to_artist:
                                NavigationUtil.goToAlbum(activity, song.artistId, null);
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
        return convertView;
    }
}
