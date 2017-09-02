package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
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
        final String[] projection = new String[]{
                Genres._ID,
                Genres.NAME
        };
        final String selection = Genres._ID + " IN" +
                " (SELECT " + Genres.Members.GENRE_ID + " FROM audio_genres_map WHERE " + Genres.Members.AUDIO_ID + " IN" +
                " (SELECT " + Genres._ID + " FROM audio_meta WHERE " + SongLoader.BASE_SELECTION + "))";

        final Cursor cursor = context.getContentResolver().query(
                Genres.EXTERNAL_CONTENT_URI,
                projection, selection, null, PreferenceUtil.getInstance(context).getGenreSortOrder());

        return getGenresFromCursor(context, cursor);
    }

    @NonNull
    public static ArrayList<Song> getSongs(@NonNull final Context context, final int genreId) {
        // The genres table only stores songs that have a genre specified,
        // so we need to get songs without a genre a different way.
        if (genreId == -1) {
            return getSongsWithNoGenre(context);
        }

        final Cursor cursor = context.getContentResolver().query(
                Genres.Members.getContentUri("external", genreId),
                SongLoader.BASE_PROJECTION, SongLoader.BASE_SELECTION, null, null);

        return SongLoader.getSongs(cursor);
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
        final Cursor cursor = makeAllSongsWithGenreCursor(context);
        ArrayList<Song> songs = new ArrayList<>();
        final int[] songIds = getSongIdsFromCursor(cursor);
        if (songIds.length > 0) {
            songs = SongLoader.getSongsNotIn(context, songIds);
        }
        return songs;
    }

    private static int[] getSongIdsFromCursor(@Nullable final Cursor cursor) {
        if (cursor == null) {
            return new int[]{};
        }

        int[] songIds = new int[cursor.getCount()];
        if (cursor.moveToFirst()) {
            int i = 0;
            do {
                songIds[i] = cursor.getInt(0);
                i++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return songIds;
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
}
