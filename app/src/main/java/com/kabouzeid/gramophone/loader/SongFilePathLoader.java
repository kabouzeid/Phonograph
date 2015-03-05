package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karim on 11.01.15.
 */
public class SongFilePathLoader {
    public static final String TAG = SongFilePathLoader.class.getSimpleName();

    public static List<String> getSongFilePaths(Context context, int[] queryIds) {
        StringBuilder selectionBuilder = new StringBuilder();
        selectionBuilder.append(BaseColumns._ID).append(" IN(");
        for (int i = 0; i < queryIds.length; i++) {
            selectionBuilder.append(queryIds[i]);
            if (i < queryIds.length - 1) {
                selectionBuilder.append(",");
            }
        }
        selectionBuilder.append(")");
        Cursor cursor = makeSongFilePathCursor(context, selectionBuilder.toString());
        List<String> songFiles = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                songFiles.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return songFiles;
    }

    public static String getSongFilePath(Context context, int queryId){
        try {
            return getSongFilePaths(context, new int[]{queryId}).get(0);
        } catch (Exception e){
            return "";
        }
    }

    public static final Cursor makeSongFilePathCursor(final Context context) {
        return makeSongFilePathCursor(context, null);
    }

    public static final Cursor makeSongFilePathCursor(final Context context, String selection) {
        return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        /* 0 */
                        MediaStore.Audio.AudioColumns.DATA,
                }, selection, null, null);
    }
}
