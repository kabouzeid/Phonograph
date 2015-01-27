package com.kabouzeid.materialmusic.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.kabouzeid.materialmusic.model.Album;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karim on 29.12.14.
 */
public class AlbumLoader {

    public static List<Album> getAllAlbums(Context context) {
        Cursor cursor = makeAlbumCursor(context);
        List<Album> albums = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final int id = cursor.getInt(0);
                final String albumName = cursor.getString(1);
                final String artist = cursor.getString(2);
                final int artistId = cursor.getInt(3);
                final int songCount = cursor.getInt(4);
                final int year = cursor.getInt(5);

                final Album album = new Album(id, albumName, artist, artistId, songCount, year);
                albums.add(album);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return albums;
    }

    private static Cursor makeAlbumCursor(final Context context) {
        return context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{
                        /* 0 */
                        BaseColumns._ID,
                        /* 1 */
                        MediaStore.Audio.AlbumColumns.ALBUM,
                        /* 2 */
                        MediaStore.Audio.AlbumColumns.ARTIST,
                        /* 3 */
                        MediaStore.Audio.Media.ARTIST_ID,
                        /* 4 */
                        MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS,
                        /* 5 */
                        MediaStore.Audio.AlbumColumns.FIRST_YEAR
                }, null, null, null);
    }

    public static Album getAlbum(Context context, int albumId) {
        Cursor cursor = makeAlbumCursor(context);
        Album album = new Album();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final int id = cursor.getInt(0);
                if (id == albumId) {
                    final String albumName = cursor.getString(1);
                    final String artist = cursor.getString(2);
                    final int artistId = cursor.getInt(3);
                    final int songCount = cursor.getInt(4);
                    final int year = cursor.getInt(5);

                    album = new Album(id, albumName, artist, artistId, songCount, year);
                }
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return album;
    }

    public static List<Album> getAlbums(Context context, String query) {
        Cursor cursor = makeAlbumCursor(context);
        List<Album> albums = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String albumName = cursor.getString(1);
                if (albumName.trim().toLowerCase().contains(query.trim().toLowerCase())) {
                    final int id = cursor.getInt(0);
                    final String artist = cursor.getString(2);
                    final int artistId = cursor.getInt(3);
                    final int songCount = cursor.getInt(4);
                    final int year = cursor.getInt(5);

                    final Album album = new Album(id, albumName, artist, artistId, songCount, year);
                    albums.add(album);
                }
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return albums;
    }
}
