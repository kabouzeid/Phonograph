package com.kabouzeid.gramophone.adapter;

import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.afollestad.materialdialogs.util.DialogUtils;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MenuItemClickHelper;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.Util;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlayingQueueAdapter extends ArrayAdapter<Song> {

    private final AppCompatActivity activity;

    public PlayingQueueAdapter(AppCompatActivity activity, ArrayList<Song> playList) {
        super(activity, R.layout.item_list_playlist_song, playList);
        this.activity = activity;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Song song = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.item_list_playlist_song, parent, false);
        }
        final TextView title = (TextView) convertView.findViewById(R.id.song_title);
        final ImageView playingIndicator = (ImageView) convertView.findViewById(R.id.playing_indicator);
        final ImageView overflowButton = (ImageView) convertView.findViewById(R.id.menu);

        title.setText(song.title);
        if (MusicPlayerRemote.getPosition() == position) {
            playingIndicator.setVisibility(View.VISIBLE);
            playingIndicator.setImageDrawable(Util.getTintedDrawable(getContext(), R.drawable.ic_speaker_white_48dp, DialogUtils.resolveColor(getContext(), R.attr.themed_drawable_color)));
        } else {
            playingIndicator.setVisibility(View.GONE);
        }

        overflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                PopupMenu popupMenu = new PopupMenu(activity, v);
                popupMenu.inflate(R.menu.menu_item_playing_queue_song);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.action_remove_from_playing_queue) {
                            MusicPlayerRemote.removeFromQueue(position);
                            notifyDataSetChanged();
                            return true;
                        }
                        return MenuItemClickHelper.handleSongMenuClick(activity, song, item);
                    }
                });
                popupMenu.show();
            }
        });
        return convertView;
    }
}
