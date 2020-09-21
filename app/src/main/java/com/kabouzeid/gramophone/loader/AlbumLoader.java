package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.provider.MediaStore.Audio.AudioColumns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumLoader {

    public static String getSongLoaderSortOrder(Context context) {
        return PreferenceUtil.getInstance(context).getAlbumSortOrder() + ", " + PreferenceUtil.getInstance(context).getAlbumSongSortOrder();
    }

    @NonNull
    public static List<Album> getAllAlbums(@NonNull final Context context) {
        List<Song> songs = SongLoader.getSongs(SongLoader.makeSongCursor(
                context,
                null,
                null,
                getSongLoaderSortOrder(context))
        );
        return splitIntoAlbums(songs);
    }

    @NonNull
    public static List<Album> getAlbums(@NonNull final Context context, String query) {
        List<Song> songs = SongLoader.getSongs(SongLoader.makeSongCursor(
                context,
                AudioColumns.ALBUM + " LIKE ?",
                new String[]{"%" + query + "%"},
                getSongLoaderSortOrder(context))
        );
        return splitIntoAlbums(songs);
    }

    @NonNull
    public static Album getAlbum(@NonNull final Context context, long albumId) {
        List<Song> songs = SongLoader.getSongs(SongLoader.makeSongCursor(context, AudioColumns.ALBUM_ID + "=?", new String[]{String.valueOf(albumId)}, getSongLoaderSortOrder(context)));
        Album album = new Album(songs);
        sortSongsByTrackNumber(album);
        return album;
    }

    @NonNull
    public static List<Album> splitIntoAlbums(@Nullable final List<Song> songs) {
        List<Album> albums = new ArrayList<>();
        if (songs != null) {
            for (Song song : songs) {
                getOrCreateAlbum(albums, song.albumId).songs.add(song);
            }
        }
        for (Album album : albums) {
            sortSongsByTrackNumber(album);
        }
        return albums;
    }

    private static Album getOrCreateAlbum(List<Album> albums, long albumId) {
        for (Album album : albums) {
            if (!album.songs.isEmpty() && album.songs.get(0).albumId == albumId) {
                return album;
            }
        }
        Album album = new Album();
        albums.add(album);
        return album;
    }

    private static void sortSongsByTrackNumber(Album album) {
        Collections.sort(album.songs, (o1, o2) -> o1.trackNumber - o2.trackNumber);
    }
}
