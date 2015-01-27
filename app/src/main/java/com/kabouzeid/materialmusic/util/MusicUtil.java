package com.kabouzeid.materialmusic.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

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
}
