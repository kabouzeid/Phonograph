package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PreferenceUtils;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumSongLoader {

    @NonNull
    public static ArrayList<Song> getAlbumSongList(@NonNull final Context context, final int albumId) {
        return SongLoader.getSongs(makeAlbumSongCursor(context, albumId));
    }

    public static Cursor makeAlbumSongCursor(@NonNull final Context context, final int albumId) {
        return SongLoader.makeSongCursor(
                context,
                MediaStore.Audio.AudioColumns.ALBUM_ID + "=?",
                new String[]{
                        String.valueOf(albumId)
                },
                PreferenceUtils.getInstance(context).getAlbumSongSortOrder()
        );
    }
}