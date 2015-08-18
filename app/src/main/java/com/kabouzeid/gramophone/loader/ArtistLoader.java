package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.ArtistColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistLoader {

    @NonNull
    public static ArrayList<Artist> getAllArtists(@NonNull Context context) {
        Cursor cursor = makeArtistCursor(context, null, null);
        return getArtists(cursor);
    }

    @NonNull
    public static ArrayList<Artist> getArtists(@NonNull Context context, String query) {
        Cursor cursor = makeArtistCursor(context, ArtistColumns.ARTIST + " LIKE ?", new String[]{"%" + query + "%"});
        return getArtists(cursor);
    }

    @NonNull
    public static Artist getArtist(@NonNull Context context, int artistId) {
        Cursor cursor = makeArtistCursor(context, BaseColumns._ID + "=?", new String[]{String.valueOf(artistId)});
        return getArtist(cursor);
    }

    @NonNull
    public static ArrayList<Artist> getArtists(@Nullable Cursor cursor) {
        ArrayList<Artist> artists = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                artists.add(getArtistFromCursorImpl(cursor));
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return artists;
    }

    @NonNull
    public static Artist getArtist(@Nullable Cursor cursor) {
        Artist artist = new Artist();
        if (cursor != null && cursor.moveToFirst()) {
            artist = getArtistFromCursorImpl(cursor);
        }

        if (cursor != null) {
            cursor.close();
        }
        return artist;
    }

    @NonNull
    private static Artist getArtistFromCursorImpl(@NonNull Cursor cursor) {
        final int id = cursor.getInt(0);
        final String artistName = cursor.getString(1);
        final int albumCount = cursor.getInt(2);
        final int songCount = cursor.getInt(3);

        return new Artist(id, artistName, albumCount, songCount);
    }

    @Nullable
    public static Cursor makeArtistCursor(@NonNull final Context context, final String selection, final String[] values) {
        try {
            return context.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                    new String[]{
                        /* 0 */
                            BaseColumns._ID,
                        /* 1 */
                            ArtistColumns.ARTIST,
                        /* 2 */
                            ArtistColumns.NUMBER_OF_ALBUMS,
                        /* 3 */
                            ArtistColumns.NUMBER_OF_TRACKS
                    }, selection, values, PreferenceUtil.getInstance(context).getArtistSortOrder());
        } catch (SecurityException e) {
            return null;
        }
    }
}
