package com.kabouzeid.gramophone.adapter.song;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.MusicUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumSongAdapter extends SongAdapter {

    public AlbumSongAdapter(AppCompatActivity activity, ArrayList<Song> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, itemLayoutRes, usePalette, cabHolder);
    }

    @Override
    protected SongAdapter.ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final Song song = dataSet.get(position);

        if (holder.imageText != null) {
            final int trackNumber = MusicUtil.getFixedTrackNumber(song.trackNumber);
            final String trackNumberString = trackNumber > 0 ? String.valueOf(trackNumber) : "-";
            holder.imageText.setText(trackNumberString);
        }
    }

    @Override
    protected String getSongText(Song song) {
        return MusicUtil.getReadableDurationString(song.duration);
    }

    public class ViewHolder extends SongAdapter.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            if (imageText != null) {
                imageText.setVisibility(View.VISIBLE);
            }
            if (image != null) {
                image.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void loadAlbumCover(Song song, SongAdapter.ViewHolder holder) {
        // We don't want to load it in this adapter
    }
}
