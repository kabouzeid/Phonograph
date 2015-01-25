package com.kabouzeid.materialmusic.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karim on 11.01.15.
 */
public class SongFileLoader {
    public static final String TAG = SongFileLoader.class.getSimpleName();

    public static List<String> getSongFiles(Context context, List<Integer> queryIds) {
        Cursor cursor = makeSongFileCursor(context);
        List<String> songFiles = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final int id = cursor.getInt(0);
                if (queryIds.contains(id)) {
                    songFiles.add(cursor.getString(1));
                }
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return songFiles;
    }

    public static String getSongFile(Context context, int queryId) {
        Cursor cursor = makeSongFileCursor(context);
        String filePath = "";
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final int id = cursor.getInt(0);
                if (id == queryId) {
                    filePath = cursor.getString(1);
                    break;
                }
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return filePath;
    }

    public static final Cursor makeSongFileCursor(final Context context) {
        return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        /* 0 */
                        BaseColumns._ID,
                        /* 1 */
                        MediaStore.Audio.AudioColumns.DATA,
                }, null, null, null);
    }
}
