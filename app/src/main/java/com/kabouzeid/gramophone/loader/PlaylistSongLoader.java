package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.support.annotation.NonNull;

import com.kabouzeid.gramophone.model.PlaylistSong;

import java.util.ArrayList;

public class PlaylistSongLoader {

    @NonNull
    public static ArrayList<PlaylistSong> getPlaylistSongList(@NonNull final Context context, final int playlistId) {
        ArrayList<PlaylistSong> songs = new ArrayList<>();
        Cursor cursor = makePlaylistSongCursor(context, playlistId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getPlaylistSongFromCursorImpl(cursor, playlistId));
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return songs;
    }

    @NonNull
    private static PlaylistSong getPlaylistSongFromCursorImpl(@NonNull Cursor cursor, int playlistId) {
        final int id = cursor.getInt(0);
        final String songName = cursor.getString(1);
        final String artist = cursor.getString(2);
        final String album = cursor.getString(3);
        final long duration = cursor.getLong(4);
        final int trackNumber = cursor.getInt(5);
        final int albumId = cursor.getInt(6);
        final int artistId = cursor.getInt(7);
        final String data = cursor.getString(8);
        final int idInPlaylist = cursor.getInt(9);

        return new PlaylistSong(id, albumId, artistId, songName, artist, album, duration, trackNumber, data, playlistId, idInPlaylist);
    }

    public static Cursor makePlaylistSongCursor(@NonNull final Context context, final int playlistId) {
        try {
            return context.getContentResolver().query(
                    MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
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
                            AudioColumns.DATA,
                        /* 9 */
                            MediaStore.Audio.Playlists.Members._ID
                    }, SongLoader.BASE_SELECTION, null,
                    MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
        } catch (SecurityException e) {
            return null;
        }
    }
}
