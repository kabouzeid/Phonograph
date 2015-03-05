package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.kabouzeid.gramophone.model.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by karim on 29.12.14.
 */
public class AlbumSongLoader {

    public static List<Song> getAlbumSongList(final Context context, final int albumId, Comparator<Song> comparator) {
        List<Song> songs = getAlbumSongList(context, albumId);
        Collections.sort(songs, comparator);
        return songs;
    }

    public static List<Song> getAlbumSongList(final Context context, final int albumId) {
        Cursor cursor = makeAlbumSongCursor(context, albumId);
        List<Song> songs = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final int id = cursor.getInt(0);
                final String songName = cursor.getString(1);
                final String artist = cursor.getString(2);
                final String album = cursor.getString(3);
                final long duration = cursor.getLong(4);
                final int trackNumber = cursor.getInt(5);
                final int artistId = cursor.getInt(6);

                final Song song = new Song(id, albumId, artistId, songName, artist, album, duration, trackNumber);
                songs.add(song);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return songs;
    }

    public static final Cursor makeAlbumSongCursor(final Context context, final int albumId) {
        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1");
        selection.append(" AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''");
        selection.append(" AND " + MediaStore.Audio.AudioColumns.ALBUM_ID + "=" + albumId);
        return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        /* 0 */
                        BaseColumns._ID,
                        /* 1 */
                        MediaStore.Audio.AudioColumns.TITLE,
                        /* 2 */
                        MediaStore.Audio.AudioColumns.ARTIST,
                        /* 3 */
                        MediaStore.Audio.AudioColumns.ALBUM,
                        /* 4 */
                        MediaStore.Audio.AudioColumns.DURATION,
                        /* 5 */
                        MediaStore.Audio.AudioColumns.TRACK,
                        /* 6 */
                        MediaStore.Audio.AudioColumns.ARTIST_ID
                }, selection.toString(), null, null);
    }
}
