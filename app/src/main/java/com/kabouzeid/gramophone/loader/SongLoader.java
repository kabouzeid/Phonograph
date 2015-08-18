package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.util.ArrayList;

import hugo.weaving.DebugLog;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongLoader {
    protected static final String BASE_SELECTION = AudioColumns.IS_MUSIC + "=1" + " AND " + AudioColumns.TITLE + " != ''";

    @NonNull
    public static ArrayList<Song> getAllSongs(@NonNull Context context) {
        Cursor cursor = makeSongCursor(context, null, null);
        return getSongs(cursor);
    }

    @NonNull
    public static ArrayList<Song> getSongs(@NonNull final Context context, final String query) {
        Cursor cursor = makeSongCursor(context, AudioColumns.TITLE + " LIKE ?", new String[]{"%" + query + "%"});
        return getSongs(cursor);
    }

    @NonNull
    public static Song getSong(@NonNull final Context context, final int queryId) {
        Cursor cursor = makeSongCursor(context, AudioColumns._ID + "=?", new String[]{String.valueOf(queryId)});
        return getSong(cursor);
    }

    @NonNull
    public static ArrayList<Song> getSongs(@Nullable final Cursor cursor) {
        ArrayList<Song> songs = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursorImpl(cursor));
            } while (cursor.moveToNext());
        }

        if (cursor != null)
            cursor.close();
        return songs;
    }

    @NonNull
    public static Song getSong(@Nullable Cursor cursor) {
        Song song = new Song();
        if (cursor != null && cursor.moveToFirst()) {
            song = getSongFromCursorImpl(cursor);
        }
        if (cursor != null) {
            cursor.close();
        }
        return song;
    }

    @NonNull
    private static Song getSongFromCursorImpl(@NonNull Cursor cursor) {
        final int id = cursor.getInt(0);
        final String songName = cursor.getString(1);
        final String artist = cursor.getString(2);
        final String album = cursor.getString(3);
        final long duration = cursor.getLong(4);
        final int trackNumber = cursor.getInt(5);
        final int artistId = cursor.getInt(6);
        final int albumId = cursor.getInt(7);
        final String data = cursor.getString(8);
        return new Song(id, albumId, artistId, songName, artist, album, duration, trackNumber, data);
    }

    @Nullable
    public static Cursor makeSongCursor(@NonNull final Context context, final String selection, final String[] values) {
        return makeSongCursor(context, selection, values, PreferenceUtil.getInstance(context).getSongSortOrder());
    }

    @DebugLog
    @Nullable
    public static Cursor makeSongCursor(@NonNull final Context context, @Nullable final String selection, final String[] values, final String sortOrder) {
        String baseSelection = BASE_SELECTION;
        if (selection != null && !selection.trim().equals("")) {
            baseSelection += " AND " + selection;
        }

        try {
            return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                        /* 0 */
                            BaseColumns._ID,
                        /* 1 */
                            AudioColumns.TITLE,
                        /* 2 */
                            AudioColumns.ARTIST,
                        /* 3 */
                            AudioColumns.ALBUM,
                        /* 4 */
                            AudioColumns.DURATION,
                        /* 5 */
                            AudioColumns.TRACK,
                        /* 6 */
                            AudioColumns.ARTIST_ID,
                        /* 7 */
                            AudioColumns.ALBUM_ID,
                        /* 8 */
                            AudioColumns.DATA
                    }, baseSelection, values, sortOrder);
        } catch (SecurityException e) {
            return null;
        }
    }
}
