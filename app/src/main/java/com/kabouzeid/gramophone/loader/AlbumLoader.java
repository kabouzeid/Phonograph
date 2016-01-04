package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.provider.MediaStore.Audio.AudioColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumLoader {

    public static String getSongLoaderSortOrder(Context context) {
        return PreferenceUtil.getInstance(context).getAlbumSortOrder() + ", " + PreferenceUtil.getInstance(context).getAlbumSongSortOrder();
    }

    @NonNull
    public static ArrayList<Album> getAllAlbums(@NonNull final Context context) {
        ArrayList<Song> songs = SongLoader.getSongs(SongLoader.makeSongCursor(
                context,
                null,
                null,
                getSongLoaderSortOrder(context))
        );
        return splitIntoAlbums(songs);
    }

    @NonNull
    public static ArrayList<Album> getAlbums(@NonNull final Context context, String query) {
        ArrayList<Song> songs = SongLoader.getSongs(SongLoader.makeSongCursor(
                context,
                AudioColumns.ALBUM + " LIKE ?",
                new String[]{"%" + query + "%"},
                getSongLoaderSortOrder(context))
        );
        return splitIntoAlbums(songs);
    }

    @NonNull
    public static Album getAlbum(@NonNull final Context context, int albumId) {
        ArrayList<Song> songs = SongLoader.getSongs(SongLoader.makeSongCursor(context, AudioColumns.ALBUM_ID + "=?", new String[]{String.valueOf(albumId)}, getSongLoaderSortOrder(context)));
        return new Album(songs);
    }

    @NonNull
    public static ArrayList<Album> splitIntoAlbums(@Nullable final ArrayList<Song> songs) {
        ArrayList<Album> albums = new ArrayList<>();
        if (songs != null) {
            for (Song song : songs) {
                Album album = get(albums, song.albumId);
                if (album == null) {
                    album = new Album();
                    albums.add(album);
                }
                album.songs.add(song);
            }
        }
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
