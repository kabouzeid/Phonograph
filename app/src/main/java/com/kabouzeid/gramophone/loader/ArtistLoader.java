package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.util.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karim on 29.12.14.
 */
public class ArtistLoader {

    public static List<Artist> getAllArtists(Context context) {
        Cursor cursor = makeArtistCursor(context);
        List<Artist> artists = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final int id = cursor.getInt(0);
                final String artistName = cursor.getString(1);
                final int albumCount = cursor.getInt(2);
                final int songCount = cursor.getInt(3);

                final Artist artist = new Artist(id, artistName, albumCount, songCount);
                artists.add(artist);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return artists;
    }

    public static final Cursor makeArtistCursor(final Context context) {
        return makeArtistCursor(context, null, null);
    }

    public static final Cursor makeArtistCursor(final Context context, final String selection, final String[] values) {
        return context.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                new String[]{
                        /* 0 */
                        BaseColumns._ID,
                        /* 1 */
                        MediaStore.Audio.ArtistColumns.ARTIST,
                        /* 2 */
                        MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS,
                        /* 3 */
                        MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS
                }, selection, values, PreferenceUtils.getInstance(context).getArtistSortOrder());
    }

    public static Artist getArtist(Context context, int artistId) {
        Cursor cursor = makeArtistCursor(context, BaseColumns._ID + "=?", new String[]{String.valueOf(artistId)});
        Artist artist = new Artist();
        if (cursor != null && cursor.moveToFirst()) {
            final int id = cursor.getInt(0);
            final String artistName = cursor.getString(1);
            final int albumCount = cursor.getInt(2);
            final int songCount = cursor.getInt(3);

            artist = new Artist(id, artistName, albumCount, songCount);
        }

        if (cursor != null) {
            cursor.close();
        }
        return artist;
    }

    public static List<Artist> getArtists(Context context, String query) {
        Cursor cursor = makeArtistCursor(context, MediaStore.Audio.ArtistColumns.ARTIST + " LIKE ?", new String[]{"%" + query + "%"});
        List<Artist> artists = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String artistName = cursor.getString(1);
                final int id = cursor.getInt(0);
                final int albumCount = cursor.getInt(2);
                final int songCount = cursor.getInt(3);

                final Artist artist = new Artist(id, artistName, albumCount, songCount);
                artists.add(artist);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return artists;
    }
}
