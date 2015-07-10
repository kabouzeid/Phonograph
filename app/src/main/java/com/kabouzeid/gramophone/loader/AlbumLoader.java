package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.provider.MediaStore.Audio.AudioColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.util.PreferenceUtils;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumLoader {

    @NonNull
    public static ArrayList<Album> getAllAlbums(@NonNull final Context context) {
        Cursor cursor = makeAlbumCursor(context, null, null);
        return getAlbums(cursor);
    }

    @NonNull
    public static ArrayList<Album> getAlbums(@NonNull final Context context, String query) {
        Cursor cursor = makeAlbumCursor(context, AlbumColumns.ALBUM + " LIKE ?", new String[]{"%" + query + "%"});
        return getAlbums(cursor);
    }

    @NonNull
    public static Album getAlbum(@NonNull final Context context, int albumId) {
        Cursor cursor = makeAlbumCursor(context, BaseColumns._ID + "=?", new String[]{String.valueOf(albumId)});
        return getAlbum(cursor);
    }

    @NonNull
    public static ArrayList<Album> getAlbums(@Nullable final Cursor cursor) {
        ArrayList<Album> albums = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                albums.add(getAlbumFromCursorImpl(cursor));
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return albums;
    }

    @NonNull
    public static Album getAlbum(@Nullable final Cursor cursor) {
        Album album = new Album();
        if (cursor != null && cursor.moveToFirst()) {
            album = getAlbumFromCursorImpl(cursor);
        }

        if (cursor != null) {
            cursor.close();
        }
        return album;
    }

    @NonNull
    private static Album getAlbumFromCursorImpl(@NonNull final Cursor cursor) {
        final int id = cursor.getInt(0);
        final String albumName = cursor.getString(1);
        final String artist = cursor.getString(2);
        final int artistId = cursor.getInt(3);
        final int songCount = cursor.getInt(4);
        final int year = cursor.getInt(5);

        return new Album(id, albumName, artist, artistId, songCount, year);
    }

    public static Cursor makeAlbumCursor(@NonNull final Context context, final String selection, final String[] values) {
        return makeAlbumCursor(context, selection, values, PreferenceUtils.getInstance(context).getAlbumSortOrder());
    }

    public static Cursor makeAlbumCursor(@NonNull final Context context, final String selection, final String[] values, final String sortOrder) {
        return makeAlbumCursor(context, MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, selection, values, sortOrder);
    }

    public static Cursor makeAlbumCursor(@NonNull final Context context, @NonNull final Uri contentUri, final String selection, final String[] values, final String sortOrder) {
        return context.getContentResolver().query(contentUri,
                new String[]{
                        /* 0 */
                        BaseColumns._ID,
                        /* 1 */
                        AlbumColumns.ALBUM,
                        /* 2 */
                        AlbumColumns.ARTIST,
                        /* 3 */
                        AudioColumns.ARTIST_ID,
                        /* 4 */
                        AlbumColumns.NUMBER_OF_SONGS,
                        /* 5 */
                        AlbumColumns.FIRST_YEAR,
                }, selection, values, sortOrder);
    }
}
