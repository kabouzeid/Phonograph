package com.kabouzeid.gramophone.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.util.DialogUtils;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.menu.SongMenuHelper;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.Util;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlayingQueueAdapterDeprecated extends ArrayAdapter<Song> {

    @NonNull
    private final AppCompatActivity activity;

    public PlayingQueueAdapterDeprecated(@NonNull AppCompatActivity activity, @NonNull ArrayList<Song> playList) {
        super(activity, R.layout.item_list_single_row, playList);
        this.activity = activity;
    }

    @Nullable
    @Override
    public View getView(final int position, @Nullable View convertView, ViewGroup parent) {
        final Song song = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.item_list_single_row, parent, false);
        }
        final TextView title = (TextView) convertView.findViewById(R.id.title);
        final ImageView playingIndicator = (ImageView) convertView.findViewById(R.id.image);
        final ImageView overflowButton = (ImageView) convertView.findViewById(R.id.menu);

        title.setText(song.title);
        if (MusicPlayerRemote.getPosition() == position) {
            int iconPadding = activity.getResources().getDimensionPixelSize(R.dimen.list_item_image_icon_padding);
            playingIndicator.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
            playingIndicator.setImageDrawable(Util.getTintedDrawable(getContext(), R.drawable.ic_volume_up_white_24dp, DialogUtils.resolveColor(getContext(), android.R.attr.textColorSecondary)));
            playingIndicator.setVisibility(View.VISIBLE);
        } else {
            playingIndicator.setVisibility(View.GONE);
        }

        convertView.findViewById(R.id.short_separator).setVisibility(View.GONE);

        overflowButton.setOnClickListener(new SongMenuHelper.OnClickSongMenu(activity) {
            @Override
            public Song getSong() {
                return song;
            }

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_remove_from_playing_queue) {
                    MusicPlayerRemote.removeFromQueue(position);
                    notifyDataSetChanged();
                    return true;
                }
                return super.onMenuItemClick(item);
            }

            @Override
            public int getMenuRes() {
                return R.menu.menu_item_playing_queue_song;
            }
        });
        return convertView;
    }
}
