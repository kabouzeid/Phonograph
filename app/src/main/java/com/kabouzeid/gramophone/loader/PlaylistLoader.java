package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.PlaylistsColumns;

import com.kabouzeid.gramophone.model.Playlist;

import java.util.ArrayList;
import java.util.List;

public class PlaylistLoader {
    public static Playlist getPlaylist(final Context context, final int playlistId) {
        Playlist playlist = new Playlist();
        Cursor cursor = makePlaylistCursor(context, BaseColumns._ID + "=?", new String[]{String.valueOf(playlistId)});

        if (cursor != null && cursor.moveToFirst()) {
            final int id = cursor.getInt(0);
            final String name = cursor.getString(1);
            playlist = new Playlist(id, name);
        }
        if (cursor != null) {
            cursor.close();
        }
        return playlist;
    }

    public static List<Playlist> getAllPlaylists(final Context context) {
        List<Playlist> playlists = new ArrayList<>();
        Cursor cursor = makePlaylistCursor(context, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                final int id = cursor.getInt(0);
                final String name = cursor.getString(1);
                final Playlist playlist = new Playlist(id, name);
                playlists.add(playlist);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return playlists;
    }

    public static Cursor makePlaylistCursor(final Context context, final String selection, final String[] values) {
        return context.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[]{
                        /* 0 */
                        BaseColumns._ID,
                        /* 1 */
                        PlaylistsColumns.NAME
                }, selection, values, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
    }
}
