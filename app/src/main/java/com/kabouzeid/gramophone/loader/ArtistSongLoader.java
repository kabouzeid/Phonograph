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
public class ArtistSongLoader {

    public static ArrayList<Song> getArtistSongList(final Context context, final int artistId) {
        Cursor cursor = makeArtistSongCursor(context, artistId);
        ArrayList<Song> songs = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final int id = cursor.getInt(0);
                final String songName = cursor.getString(1);
                final String artist = cursor.getString(2);
                final String album = cursor.getString(3);
                final long duration = cursor.getLong(4);
                final int trackNumber = cursor.getInt(5);
                final int albumId = cursor.getInt(6);
                final long dateModified = cursor.getInt(7);

                final Song song = new Song(id, albumId, artistId, songName, artist, album, duration, trackNumber, dateModified);
                songs.add(song);
            } while (cursor.moveToNext());
        }
        if (cursor != null)
            cursor.close();
        return songs;
    }

    public static Cursor makeArtistSongCursor(final Context context, final int artistId) {
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
                        MediaStore.Audio.AudioColumns.ALBUM_ID,
                        /* 7 */
                        MediaStore.Audio.AudioColumns.DATE_MODIFIED
                }, (MediaStore.Audio.AudioColumns.IS_MUSIC + "=1") + " AND " +
                        MediaStore.Audio.AudioColumns.TITLE + " != ''" + " AND " +
                        MediaStore.Audio.AudioColumns.ARTIST_ID + "=" + artistId, null,
                PreferenceUtils.getInstance(context).getArtistSongSortOrder());
    }
}
