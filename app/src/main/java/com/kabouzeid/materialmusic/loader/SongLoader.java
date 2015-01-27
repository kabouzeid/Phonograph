package com.kabouzeid.materialmusic.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.kabouzeid.materialmusic.model.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karim on 29.12.14.
 */
public class SongLoader {
    public static List<Song> getAllSongs(Context context) {
        Cursor cursor = makeAlbumSongCursor(context);
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
                final int albumId = cursor.getInt(7);

                final Song song = new Song(id, albumId, artistId, songName, artist, album, duration, trackNumber);
                songs.add(song);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return songs;
    }

    public static final Cursor makeAlbumSongCursor(final Context context) {
        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1");
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
                        MediaStore.Audio.AudioColumns.ARTIST_ID,
                        /* 7 */
                        MediaStore.Audio.AudioColumns.ALBUM_ID
                }, selection.toString(), null, null);
    }

    public static List<Song> getSongs(Context context, String query) {
        Cursor cursor = makeAlbumSongCursor(context);
        List<Song> songs = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String songName = cursor.getString(1);
                if (songName.trim().toLowerCase().contains(query.trim().toLowerCase())) {
                    final int id = cursor.getInt(0);
                    final String artist = cursor.getString(2);
                    final String album = cursor.getString(3);
                    final long duration = cursor.getLong(4);
                    final int trackNumber = cursor.getInt(5);
                    final int artistId = cursor.getInt(6);
                    final int albumId = cursor.getInt(7);

                    final Song song = new Song(id, albumId, artistId, songName, artist, album, duration, trackNumber);
                    songs.add(song);
                }
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return songs;
    }

    public static Song getSong(Context context, int queryId) {
        Cursor cursor = makeAlbumSongCursor(context);
        Song song = null;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final int id = cursor.getInt(0);
                if (id == queryId) {
                    final String songName = cursor.getString(1);
                    final String artist = cursor.getString(2);
                    final String album = cursor.getString(3);
                    final long duration = cursor.getLong(4);
                    final int trackNumber = cursor.getInt(5);
                    final int artistId = cursor.getInt(6);
                    final int albumId = cursor.getInt(7);
                    song = new Song(id, albumId, artistId, songName, artist, album, duration, trackNumber);
                    break;
                }
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return song;
    }
}
