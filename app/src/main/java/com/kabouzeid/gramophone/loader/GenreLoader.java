package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Genres;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kabouzeid.gramophone.model.Genre;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.util.ArrayList;

public class GenreLoader {

    @NonNull
    public static ArrayList<Genre> getAllGenres(@NonNull final Context context) {
        final String[] projection = new String[]{
                Genres._ID,
                Genres.NAME
        };

        final Cursor cursor = context.getContentResolver().query(
                Genres.EXTERNAL_CONTENT_URI,
                projection, null, null, PreferenceUtil.getInstance(context).getGenreSortOrder());

        return getGenresFromCursor(context, cursor);
    }

    @NonNull
    public static ArrayList<Song> getSongs(@NonNull final Context context, final int genreId) {
        final Cursor cursor = context.getContentResolver().query(
                Genres.Members.getContentUri("external", genreId),
                SongLoader.BASE_PROJECTION, SongLoader.BASE_SELECTION, null, null);

        return SongLoader.getSongs(cursor);
    }

    @NonNull
    private static ArrayList<Genre> getGenresFromCursor(@NonNull final Context context, @Nullable final Cursor cursor) {
        final ArrayList<Genre> genres = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    genres.add(getGenreFromCursor(context, cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return genres;
    }

    @NonNull
    private static Genre getGenreFromCursor(@NonNull final Context context, @NonNull final Cursor cursor) {
        final int id = cursor.getInt(0);
        final String name = cursor.getString(1);
        final int songs = getSongs(context, id).size();
        return new Genre(id, name, songs);
    }
}
