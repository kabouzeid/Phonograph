package com.kabouzeid.materialmusic.adapter.songadapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.helper.SongDetailDialogHelper;
import com.kabouzeid.materialmusic.loader.SongFilePathLoader;
import com.kabouzeid.materialmusic.misc.AppKeys;
import com.kabouzeid.materialmusic.model.Song;
import com.kabouzeid.materialmusic.ui.activities.tageditor.SongTagEditorActivity;
import com.kabouzeid.materialmusic.util.ImageLoaderUtil;
import com.kabouzeid.materialmusic.util.MusicUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.List;

/**
 * Created by karim on 27.11.14.
 */
public class SongViewListAdapter extends SongAdapter {
    public static final String TAG = SongViewListAdapter.class.getSimpleName();

    public SongViewListAdapter(Context context, SongAdapter.GoToAble goToAble, List<Song> objects) {
        super(context, goToAble, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Song song = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_song_view, parent, false);
        }
        TextView songTitle = (TextView) convertView.findViewById(R.id.song_title);
        final ImageView albumArt = (ImageView) convertView.findViewById(R.id.album_art);
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
                                String songFilePath = SongFilePathLoader.getSongFilePath(context, song.id);
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
        ImageLoader.getInstance().displayImage(MusicUtil.getAlbumArtUri(song.albumId).toString(), albumArt, new ImageLoaderUtil.defaultAlbumArtOnFailed());

        return convertView;
    }
}
