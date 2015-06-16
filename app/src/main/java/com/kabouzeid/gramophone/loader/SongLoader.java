package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PreferenceUtils;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongLoader {
    private static final String BASE_SELECTION = MediaStore.Audio.AudioColumns.IS_MUSIC + "=1" + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''";

    public static ArrayList<Song> getAllSongs(Context context) {
        Cursor cursor = makeSongCursor(context, null, null);
        return getSongs(cursor);
    }

    public static ArrayList<Song> getSongs(final Context context, final String query) {
        Cursor cursor = makeSongCursor(context, MediaStore.Audio.AudioColumns.TITLE + " LIKE ?", new String[]{"%" + query + "%"});
        return getSongs(cursor);
    }

    public static Song getSong(final Context context, final int queryId) {
        Cursor cursor = makeSongCursor(context, MediaStore.Audio.AudioColumns._ID + "=?", new String[]{String.valueOf(queryId)});
        return getSong(cursor);
    }

    public static ArrayList<Song> getSongs(final Cursor cursor) {
        ArrayList<Song> songs = new ArrayList<>();
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

        if (cursor != null)
            cursor.close();
        return songs;
    }

    public static Song getSong(Cursor cursor) {
        Song song = new Song();
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

    public static Cursor makeSongCursor(final Context context, final String selection, final String[] values) {
        String baseSelection = BASE_SELECTION;
        if (selection != null && !selection.trim().equals("")) {
            baseSelection += " AND " + selection;
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
                }, baseSelection, values, PreferenceUtils.getInstance(context).getSongSortOrder());
    }
}
