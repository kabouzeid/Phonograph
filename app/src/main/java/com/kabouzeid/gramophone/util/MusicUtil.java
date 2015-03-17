package com.kabouzeid.gramophone.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.model.Song;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Created by karim on 29.12.14.
 */
public class MusicUtil {
    public static final String TAG = MusicUtil.class.getSimpleName();

    public static Uri getAlbumArtUri(int album_id) {
        final Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");

        return ContentUris.withAppendedId(sArtworkUri, album_id);
    }

    public static boolean hasAlbumArt(final Context context, int album_id) {
        try {
            context.getContentResolver().openFileDescriptor(getAlbumArtUri(album_id), "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String getReadableDurationString(long songDurationMillis) {
        long minutes = (songDurationMillis / 1000) / 60;
        long seconds = (songDurationMillis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }


    //iTunes uses for example 1002 for track 2 CD1 or 3011 for track 11 CD3.
    //this method converts those values to normal tracknumbers
    public static int getFixedTrackNumber(int trackNumberToFix) {
        return trackNumberToFix % 1000;
    }

    public static void insertAlbumArt(Context context, int albumId, String path) {
        ContentResolver contentResolver = context.getContentResolver();

        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        contentResolver.delete(ContentUris.withAppendedId(artworkUri, albumId), null, null);

        ContentValues values = new ContentValues();
        values.put("album_id", albumId);
        values.put("_data", path);

        contentResolver.insert(artworkUri, values);
    }

    public static void deleteAlbumArt(Context context, int albumId) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri localUri = Uri.parse("content://media/external/audio/albumart");
        contentResolver.delete(ContentUris.withAppendedId(localUri, albumId), null, null);
    }

    public static File getAlbumArtFile(Context context, String name)
            throws IOException {
        return new File(createAlbumArtDir(context), name + System.currentTimeMillis());
    }

    public static File createAlbumArtDir(Context paramContext) {
        File albumArtDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/.albumart/");
        if (!albumArtDir.exists()) {
            albumArtDir.mkdirs();
            try {
                new File(albumArtDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "error while creating .nomedia file", e);
            }
        }
        return albumArtDir;
    }

    public static void deleteTracks(final Context context, final List<Song> songs) {
        final String[] projection = new String[] {
                BaseColumns._ID, MediaStore.MediaColumns.DATA
        };
        final StringBuilder selection = new StringBuilder();
        selection.append(BaseColumns._ID + " IN (");
        for (int i = 0; i < songs.size(); i++) {
            selection.append(songs.get(i).id);
            if (i < songs.size() - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
        final Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection.toString(),
                null, null);
        if (cursor != null) {
            // Step 1: Remove selected tracks from the current playlist, as well
            // as from the album art cache
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                final int id = cursor.getInt(0);
                final Song song = SongLoader.getSong(context, id);
                MusicPlayerRemote.removeFromQueue(song);
            }

            // Step 2: Remove selected tracks from the database
            context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    selection.toString(), null);

            // Step 3: Remove files from card
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                final String name = cursor.getString(1);
                final File f = new File(name);
                try { // File.delete can throw a security exception
                    if (!f.delete()) {
                        // I'm not sure if we'd ever get here (deletion would
                        // have to fail, but no exception thrown)
                        Log.e("MusicUtils", "Failed to delete file " + name);
                    }
                    cursor.moveToNext();
                } catch (final SecurityException ex) {
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
        Toast.makeText(context, "Deleted " + songs.size() + " songs", Toast.LENGTH_SHORT).show();
        //TODO add resource string
    }
}
