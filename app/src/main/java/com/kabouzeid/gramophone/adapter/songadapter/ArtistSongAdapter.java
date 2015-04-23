package com.kabouzeid.gramophone.adapter.songadapter;

import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MenuItemClickHelper;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;

import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistSongAdapter extends ArrayAdapter<Song> {

    private final AppCompatActivity activity;

    public ArtistSongAdapter(AppCompatActivity activity, List<Song> songs) {
        super(activity, R.layout.item_list_song, songs);
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Song song = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_artist_song, parent, false);
        }

        final TextView songTitle = (TextView) convertView.findViewById(R.id.song_title);
        final TextView songInfo = (TextView) convertView.findViewById(R.id.song_info);
        final ImageView albumArt = (ImageView) convertView.findViewById(R.id.album_art);

        songTitle.setText(song.title);
        songInfo.setText(song.albumName);

        Glide.with(activity)
                .loadFromMediaStore(MusicUtil.getAlbumArtUri(song.albumId))
                .signature(new StringSignature(String.valueOf(song.dateModified)))
                .error(R.drawable.default_album_art)
                .placeholder(R.drawable.default_album_art)
                .into(albumArt);

        final ImageView overflowButton = (ImageView) convertView.findViewById(R.id.menu);
        overflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(activity, v);
                popupMenu.inflate(R.menu.menu_item_song);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_go_to_album:
                                Pair[] albumPairs = new Pair[]{
                                        Pair.create(albumArt, activity.getResources().getString(R.string.transition_album_cover))
                                };
                                if (activity instanceof AbsFabActivity)
                                    albumPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(albumPairs);
                                NavigationUtil.goToAlbum(activity, song.albumId, albumPairs);
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
