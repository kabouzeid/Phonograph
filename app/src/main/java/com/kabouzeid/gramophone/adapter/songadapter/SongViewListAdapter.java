package com.kabouzeid.gramophone.adapter.songadapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.SongDetailDialogHelper;
import com.kabouzeid.gramophone.loader.SongFilePathLoader;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.SongTagEditorActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * Created by karim on 27.11.14.
 */
public class SongViewListAdapter extends SongAdapter {
    public static final String TAG = SongViewListAdapter.class.getSimpleName();

    public SongViewListAdapter(Activity activity, List<Song> objects) {
        super(activity, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Song song = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_song_view, parent, false);
        }
        TextView songTitle = (TextView) convertView.findViewById(R.id.song_title);
        TextView songInfo = (TextView) convertView.findViewById(R.id.song_info);
        final ImageView albumArt = (ImageView) convertView.findViewById(R.id.album_art);
        ImageView overflowButton = (ImageView) convertView.findViewById(R.id.menu);

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
                                Pair[] albumPairs = new Pair[]{Pair.create(albumArt, getContext().getResources().getString(R.string.transition_album_cover))};
                                if (activity instanceof AbsFabActivity)
                                    albumPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(albumPairs);
                                NavigationUtil.goToAlbum(activity, song.albumId, albumPairs);
                                return true;
                            case R.id.action_go_to_artist:
                                Pair[] artistPairs = null;
                                if (activity instanceof AbsFabActivity)
                                    artistPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(artistPairs);
                                NavigationUtil.goToArtist(activity, song.artistId, artistPairs);
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        songTitle.setText(song.title);
        songInfo.setText(song.getSubTitle());

        Picasso.with(getContext())
                .load(MusicUtil.getAlbumArtUri(song.albumId))
                .placeholder(R.drawable.default_album_art)
                .into(albumArt);

        return convertView;
    }
}
