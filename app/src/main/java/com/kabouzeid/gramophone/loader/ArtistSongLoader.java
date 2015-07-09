package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PreferenceUtils;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistSongLoader {

    public static ArrayList<Song> getArtistSongList(final Context context, final int artistId) {
        return SongLoader.getSongs(makeArtistSongCursor(context, artistId));
    }

    public static Cursor makeArtistSongCursor(final Context context, final int artistId) {
        return SongLoader.makeSongCursor(
                context,
                MediaStore.Audio.AudioColumns.ARTIST_ID + "=?",
                new String[]{
                        String.valueOf(artistId)
                },
                PreferenceUtils.getInstance(context).getArtistSongSortOrder()
        );
    }
}