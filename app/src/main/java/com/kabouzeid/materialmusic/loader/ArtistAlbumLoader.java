package com.kabouzeid.materialmusic.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.kabouzeid.materialmusic.model.Album;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karim on 04.01.15.
 */
public class ArtistAlbumLoader {
    public static List<Album> getArtistAlbumList(final Context context, final int artistId) {
        Cursor cursor = makeArtistAlbumCursor(context, artistId);
        List<Album> albums = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final int id = cursor.getInt(0);
                final String albumName = cursor.getString(1);
                final String artist = cursor.getString(2);
                final int songCount = cursor.getInt(3);
                final int year = cursor.getInt(4);

                final Album album = new Album(id, albumName, artist, artistId, songCount, year);
                albums.add(album);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return albums;
    }

    public static Cursor makeArtistAlbumCursor(final Context context, final int artistId) {
        return context.getContentResolver().query(
                MediaStore.Audio.Artists.Albums.getContentUri("external", artistId), new String[]{
                        /* 0 */
                        BaseColumns._ID,
                        /* 1 */
                        MediaStore.Audio.AlbumColumns.ALBUM,
                        /* 2 */
                        MediaStore.Audio.AlbumColumns.ARTIST,
                        /* 3 */
                        MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS,
                        /* 4 */
                        MediaStore.Audio.AlbumColumns.FIRST_YEAR
                }, null, null, null);
    }
}
