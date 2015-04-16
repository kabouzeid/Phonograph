package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongLoader {
    private static final String BASE_SELECTION = MediaStore.Audio.AudioColumns.IS_MUSIC + "=1" + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''";

    public static ArrayList<Song> getAllSongs(Context context) {
        Cursor cursor = makeSongCursor(context);
        ArrayList<Song> songs = new ArrayList<>();
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

    public static Cursor makeSongCursor(final Context context) {
        return makeSongCursor(context, MediaStore.Audio.AudioColumns.IS_MUSIC + "=?", new String[]{"1"});
    }

    public static Cursor makeSongCursor(final Context context, final String selection, final String[] values) {
        String finalSelection = BASE_SELECTION;
        if (selection != null) {
            finalSelection += " AND " + selection;
        }

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
                }, finalSelection, values, PreferenceUtils.getInstance(context).getSongSortOrder());
    }

    public static List<Song> getSongs(final Context context, final String query) {
        Cursor cursor = makeSongCursor(context, MediaStore.Audio.AudioColumns.TITLE + " LIKE ?", new String[]{"%" + query + "%"});
        List<Song> songs = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String songName = cursor.getString(1);
                final int id = cursor.getInt(0);
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

    public static Song getSong(final Context context, final int queryId) {
        Cursor cursor = makeSongCursor(context, MediaStore.Audio.AudioColumns._ID + "=?", new String[]{String.valueOf(queryId)});
        Song song = null;
        if (cursor != null && cursor.moveToFirst()) {
            final int id = cursor.getInt(0);
            final String songName = cursor.getString(1);
            final String artist = cursor.getString(2);
            final String album = cursor.getString(3);
            final long duration = cursor.getLong(4);
            final int trackNumber = cursor.getInt(5);
            final int artistId = cursor.getInt(6);
            final int albumId = cursor.getInt(7);
            song = new Song(id, albumId, artistId, songName, artist, album, duration, trackNumber);
        }
        if (cursor != null) {
            cursor.close();
        }
        return song;
    }
}
