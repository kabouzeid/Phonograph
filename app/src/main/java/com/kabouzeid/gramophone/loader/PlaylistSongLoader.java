package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;

import com.kabouzeid.gramophone.model.PlaylistSong;

import java.util.ArrayList;

public class PlaylistSongLoader {

    public static ArrayList<PlaylistSong> getPlaylistSongList(final Context context, final int playlistID) {
        ArrayList<PlaylistSong> songs = new ArrayList<>();
        Cursor cursor = makePlaylistSongCursor(context, playlistID);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                final int id = cursor.getInt(0);
                final String songName = cursor.getString(1);
                final String artist = cursor.getString(2);
                final String album = cursor.getString(3);
                final long duration = cursor.getLong(4);
                final int trackNumber = cursor.getInt(5);
                final int albumId = cursor.getInt(6);
                final int artistId = cursor.getInt(7);
                final int idInPlaylist = cursor.getInt(8);
                final long dateModified = cursor.getInt(9);

                final PlaylistSong song = new PlaylistSong(id, albumId, artistId, songName, artist, album, duration, trackNumber, playlistID, idInPlaylist, dateModified);

                songs.add(song);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return songs;
    }

    public static Cursor makePlaylistSongCursor(final Context context, final int playlistID) {
        return context.getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistID),
                new String[]{
                        /* 0 */
                        MediaStore.Audio.Playlists.Members.AUDIO_ID,
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
                        AudioColumns.ALBUM_ID,
                        /* 7 */
                        AudioColumns.ARTIST_ID,
                        /* 8 */
                        MediaStore.Audio.Playlists.Members._ID,
                        /* 9 */
                        MediaStore.Audio.AudioColumns.DATE_MODIFIED
                }, (AudioColumns.IS_MUSIC + "=1") + " AND " + AudioColumns.TITLE + " != ''", null,
                MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
    }
}
