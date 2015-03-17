package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;

import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.PlaylistSong;
import com.kabouzeid.gramophone.model.Song;

import java.util.ArrayList;
import java.util.List;

public class PlaylistSongLoader {

    public static List<PlaylistSong> getPlaylistSongList(final Context context, final int playlistID) {
        List<PlaylistSong> songs = new ArrayList<>();
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

                final PlaylistSong song = new PlaylistSong(id, albumId, artistId, songName, artist, album, duration, trackNumber, playlistID, idInPlaylist);

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
                        MediaStore.Audio.Playlists.Members._ID
                }, (AudioColumns.IS_MUSIC + "=1") + " AND " + AudioColumns.TITLE + " != ''", null,
                MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
    }
}
