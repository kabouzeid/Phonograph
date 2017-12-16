package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio.Genres;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.Genre;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.util.ArrayList;

public class GenreLoader {

    @NonNull
    public static ArrayList<Genre> getAllGenres(@NonNull final Context context) {
        return getGenresFromCursor(context, makeGenreCursor(context));
    }

    @NonNull
    public static ArrayList<Song> getSongs(@NonNull final Context context, final int genreId) {
        // The genres table only stores songs that have a genre specified,
        // so we need to get songs without a genre a different way.
        if (genreId == -1) {
            return getSongsWithNoGenre(context);
        }

        return SongLoader.getSongs(makeGenreSongCursor(context, genreId));
    }

    @NonNull
    private static ArrayList<Genre> getGenresFromCursor(@NonNull final Context context, @Nullable final Cursor cursor) {
        final ArrayList<Genre> genres = new ArrayList<>();

        if (hasSongsWithNoGenre(context)) {
            genres.add(new Genre(context.getResources().getString(R.string.unknown_genre)));
        }

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    genres.add(getGenreFromCursor(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return genres;
    }

    @NonNull
    private static Genre getGenreFromCursor(@NonNull final Cursor cursor) {
        final int id = cursor.getInt(0);
        final String name = cursor.getString(1);
        return new Genre(id, name);
    }

    @NonNull
    private static ArrayList<Song> getSongsWithNoGenre(@NonNull final Context context) {
        String selection = BaseColumns._ID + " NOT IN " +
                "(SELECT " + Genres.Members.AUDIO_ID + " FROM audio_genres_map)";
        return SongLoader.getSongs(SongLoader.makeSongCursor(context, selection, null));
    }

    private static boolean hasSongsWithNoGenre(@NonNull final Context context) {
        final Cursor allSongsCursor = SongLoader.makeSongCursor(context, null, null);
        final Cursor allSongsWithGenreCursor = makeAllSongsWithGenreCursor(context);

        if (allSongsCursor == null || allSongsWithGenreCursor == null) {
            return false;
        }

        final boolean hasSongsWithNoGenre = allSongsCursor.getCount() > allSongsWithGenreCursor.getCount();
        allSongsCursor.close();
        allSongsWithGenreCursor.close();
        return hasSongsWithNoGenre;
    }

    @Nullable
    private static Cursor makeAllSongsWithGenreCursor(@NonNull final Context context) {
        try {
            return context.getContentResolver().query(
                    Uri.parse("content://media/external/audio/genres/all/members"),
                    new String[]{Genres.Members.AUDIO_ID}, null, null, null);
        } catch (SecurityException e) {
            return null;
        }
    }

    @Nullable
    private static Cursor makeGenreSongCursor(@NonNull final Context context, int genreId) {
        try {
            return context.getContentResolver().query(
                    Genres.Members.getContentUri("external", genreId),
                    SongLoader.BASE_PROJECTION, SongLoader.BASE_SELECTION, null, PreferenceUtil.getInstance(context).getSongSortOrder());
        } catch (SecurityException e) {
            return null;
        }
    }

    @Nullable
    private static Cursor makeGenreCursor(@NonNull final Context context) {
        final String[] projection = new String[]{
                Genres._ID,
                Genres.NAME
        };
        // Genres that actually have songs
        final String selection = Genres._ID + " IN" +
                " (SELECT " + Genres.Members.GENRE_ID + " FROM audio_genres_map WHERE " + Genres.Members.AUDIO_ID + " IN" +
                " (SELECT " + Genres._ID + " FROM audio_meta WHERE " + SongLoader.BASE_SELECTION + "))";

        try {
            return context.getContentResolver().query(
                    Genres.EXTERNAL_CONTENT_URI,
                    projection, selection, null, PreferenceUtil.getInstance(context).getGenreSortOrder());
        } catch (SecurityException e) {
            return null;
        }
    }
}
