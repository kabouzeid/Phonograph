package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.provider.MediaStore.Audio.AudioColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kabouzeid.gramophone.comparator.AlbumASCComparator;
import com.kabouzeid.gramophone.comparator.SongTrackComparator;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import hugo.weaving.DebugLog;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumLoader {

    @DebugLog
    @NonNull
    public static ArrayList<Album> getAllAlbums(@NonNull final Context context) {
        ArrayList<Song> songs = SongLoader.getSongs(SongLoader.makeSongCursor(
                context,
                null,
                null,
                PreferenceUtil.getInstance(context).getAlbumSongSortOrder())
        );
        return splitIntoAlbums(songs, new AlbumASCComparator());
    }

    @NonNull
    public static ArrayList<Album> getAlbums(@NonNull final Context context, String query) {
        ArrayList<Song> songs = SongLoader.getSongs(SongLoader.makeSongCursor(
                context,
                AudioColumns.ALBUM + " LIKE ?",
                new String[]{"%" + query + "%"},
                PreferenceUtil.getInstance(context).getAlbumSongSortOrder())
        );
        return splitIntoAlbums(songs, new AlbumASCComparator());
    }

    @NonNull
    public static Album getAlbum(@NonNull final Context context, int albumId) {
        ArrayList<Song> songs = SongLoader.getSongs(SongLoader.makeSongCursor(context, AudioColumns.ALBUM_ID + "=?", new String[]{String.valueOf(albumId)}, PreferenceUtil.getInstance(context).getAlbumSongSortOrder()));
        Collections.sort(songs, new SongTrackComparator());
        return new Album(songs);
    }

    @NonNull
    public static ArrayList<Album> splitIntoAlbums(@Nullable final ArrayList<Song> songs, Comparator<Album> albumComparator) {
        ArrayList<Album> albums = new ArrayList<>();
        if (songs != null) {
            Collections.sort(songs, new SongTrackComparator());
            for (Song song : songs) {
                Album album = get(albums, song.albumId);
                if (album == null) {
                    album = new Album();
                    albums.add(album);
                }
                album.songs.add(song);
            }
        }
        Collections.sort(albums, albumComparator);
        return albums;
    }

    private static Album get(ArrayList<Album> albums, int albumId) {
        for (Album album : albums) {
            if (!album.songs.isEmpty() && album.songs.get(0).albumId == albumId) {
                return album;
            }
        }
        return null;
    }
}
