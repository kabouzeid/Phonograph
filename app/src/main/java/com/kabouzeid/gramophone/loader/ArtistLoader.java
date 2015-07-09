package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.ArtistColumns;

import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.util.PreferenceUtils;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistLoader {

    public static ArrayList<Artist> getAllArtists(Context context) {
        Cursor cursor = makeArtistCursor(context, null, null);
        return getArtists(cursor);
    }

    public static ArrayList<Artist> getArtists(Context context, String query) {
        Cursor cursor = makeArtistCursor(context, ArtistColumns.ARTIST + " LIKE ?", new String[]{"%" + query + "%"});
        return getArtists(cursor);
    }

    public static Artist getArtist(Context context, int artistId) {
        Cursor cursor = makeArtistCursor(context, BaseColumns._ID + "=?", new String[]{String.valueOf(artistId)});
        return getArtist(cursor);
    }

    public static ArrayList<Artist> getArtists(Cursor cursor) {
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

    public static Artist getArtist(Cursor cursor) {
        Artist artist = new Artist();
        if (cursor != null && cursor.moveToFirst()) {
            artist = getArtistFromCursorImpl(cursor);
        }

        if (cursor != null) {
            cursor.close();
        }
        return artist;
    }

    private static Artist getArtistFromCursorImpl(Cursor cursor) {
        final int id = cursor.getInt(0);
        final String artistName = cursor.getString(1);
        final int albumCount = cursor.getInt(2);
        final int songCount = cursor.getInt(3);

        return new Artist(id, artistName, albumCount, songCount);
    }

    public static Cursor makeArtistCursor(final Context context, final String selection, final String[] values) {
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
                }, selection, values, PreferenceUtils.getInstance(context).getArtistSortOrder());
    }
}
