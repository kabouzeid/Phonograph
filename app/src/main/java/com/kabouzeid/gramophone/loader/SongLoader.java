package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.provider.BlacklistStore;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongLoader {
    protected static final String BASE_SELECTION = AudioColumns.IS_MUSIC + "=1" + " AND " + AudioColumns.TITLE + " != ''";
    protected static final String[] BASE_PROJECTION = new String[]{
            BaseColumns._ID,// 0
            AudioColumns.TITLE,// 1
            AudioColumns.TRACK,// 2
            AudioColumns.YEAR,// 3
            AudioColumns.DURATION,// 4
            AudioColumns.DATA,// 5
            AudioColumns.DATE_MODIFIED,// 6
            AudioColumns.ALBUM_ID,// 7
            AudioColumns.ALBUM,// 8
            AudioColumns.ARTIST_ID,// 9
            AudioColumns.ARTIST,// 10
    };
    private static final int BATCH_SIZE = 900; // used in makeSongCursor* functions. SQLite limit on the number of ?argument is 999, we leave some to the other call sites.

    @NonNull
    public static ArrayList<Song> getAllSongs(@NonNull Context context) {
        Cursor cursor = makeSongCursor(context, null, null);
        return getSongs(cursor);
    }

    @NonNull
    public static ArrayList<Song> getSongs(@NonNull final Context context, final String query) {
        Cursor cursor = makeSongCursor(context, AudioColumns.TITLE + " LIKE ?", new String[]{"%" + query + "%"});
        return getSongs(cursor);
    }

    @NonNull
    public static Song getSong(@NonNull final Context context, final int queryId) {
        Cursor cursor = makeSongCursor(context, AudioColumns._ID + "=?", new String[]{String.valueOf(queryId)});
        return getSong(cursor);
    }

    @NonNull
    public static ArrayList<Song> getSongs(@Nullable final Cursor cursor) {
        ArrayList<Song> songs = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursorImpl(cursor));
            } while (cursor.moveToNext());
        }

        if (cursor != null)
            cursor.close();
        return songs;
    }

    @NonNull
    public static Song getSong(@Nullable Cursor cursor) {
        Song song;
        if (cursor != null && cursor.moveToFirst()) {
            song = getSongFromCursorImpl(cursor);
        } else {
            song = Song.EMPTY_SONG;
        }
        if (cursor != null) {
            cursor.close();
        }
        return song;
    }

    @NonNull
    private static Song getSongFromCursorImpl(@NonNull Cursor cursor) {
        final int id = cursor.getInt(0);
        final String title = cursor.getString(1);
        final int trackNumber = cursor.getInt(2);
        final int year = cursor.getInt(3);
        final long duration = cursor.getLong(4);
        final String data = cursor.getString(5);
        final long dateModified = cursor.getLong(6);
        final int albumId = cursor.getInt(7);
        final String albumName = cursor.getString(8);
        final int artistId = cursor.getInt(9);
        final String artistName = cursor.getString(10);

        return new Song(id, title, trackNumber, year, duration, data, dateModified, albumId, albumName, artistId, artistName);
    }

    @Nullable
    public static Cursor makeSongCursorFromPaths(@NonNull final Context context, @NonNull ArrayList<String> paths) {
        // Exclude blacklist
        paths.removeAll(BlacklistStore.getInstance(context).getPaths());

        int remaining = paths.size();
        int processed = 0;

        ArrayList<Cursor> cursors = new ArrayList<>();
        while (remaining > 0) {
            final int currentBatch = Math.min(BATCH_SIZE, remaining);

            StringBuilder selection = new StringBuilder();
            selection.append(BASE_SELECTION + " AND " + MediaStore.Audio.AudioColumns.DATA + " IN (?");
            for (int i = 1; i < currentBatch; i++) {
                selection.append(",?");
            }
            selection.append(")");

            try {
                Cursor cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    BASE_PROJECTION, 
                    selection.toString(), 
                    paths.subList(processed, processed + currentBatch).toArray(new String[currentBatch]),
                    PreferenceUtil.getInstance(context).getSongSortOrder()
                );
                if (cursor != null) {cursors.add(cursor);};
            } catch (SecurityException ignored) {
            }

            remaining -= currentBatch;
            processed += currentBatch;
        }
        return new MergeCursor(cursors.toArray(new Cursor[cursors.size()]));
    }

    @Nullable
    public static Cursor makeSongCursor(@NonNull final Context context, @Nullable final String selection, final String[] selectionValues) {
        return makeSongCursor(context, selection, selectionValues, PreferenceUtil.getInstance(context).getSongSortOrder());
    }

    @Nullable
    public static Cursor makeSongCursor(@NonNull final Context context, @Nullable String selection, String[] selectionValues, final String sortOrder) {
        if (selection != null && !selection.trim().equals("")) {
            selection = BASE_SELECTION + " AND " + selection;
        } else {
            selection = BASE_SELECTION;
        }

        // Blacklist
        final ArrayList<String> paths = BlacklistStore.getInstance(context).getPaths();
        int remaining = paths.size();
        int processed = 0;

        ArrayList<Cursor> cursors = new ArrayList<>();
        while (remaining > 0) {
            final int currentBatch = Math.min(BATCH_SIZE, remaining);

            // Enrich the base selection with the current batch parameters
            String batchSelection = generateBlacklistSelection(selection, currentBatch);
            ArrayList<String> batchSelectionValues = addBlacklistSelectionValues(selectionValues, paths.subList(processed, processed + currentBatch));

            try {
                Cursor cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    BASE_PROJECTION,
                    batchSelection,
                    batchSelectionValues.toArray(new String[batchSelectionValues.size()]),
                    sortOrder
                );
                if (cursor != null) {cursors.add(cursor);}
            } catch (SecurityException ignored) {
            }

            remaining -= currentBatch;
            processed += currentBatch;
        }
        return new MergeCursor(cursors.toArray(new Cursor[cursors.size()]));
    }

    private static String generateBlacklistSelection(String selection, int pathCount) {
        String newSelection = (selection != null && !selection.trim().equals("")) ? selection + " AND " : "";
        newSelection += AudioColumns.DATA + " NOT LIKE ?";
        for (int i = 1; i < pathCount; i++) {
            newSelection += " AND " + AudioColumns.DATA + " NOT LIKE ?";
        }
        return newSelection;
    }

    private static ArrayList<String> addBlacklistSelectionValues(String[] selectionValues, @NonNull final List<String> paths) {
        ArrayList<String> newSelectionValues = null;
        if (selectionValues == null) {
            newSelectionValues = new ArrayList<String>(paths.size());
        }
        else {
            newSelectionValues = new ArrayList<String>(selectionValues.length + paths.size());
            for (int i=0; i < selectionValues.length; ++i) {
                newSelectionValues.add(selectionValues[i]);
            }
        }

        for (int i = 0; i < paths.size(); i++) {
            newSelectionValues.add(paths.get(i) + "%");
        }
        return newSelectionValues;
    }
}
