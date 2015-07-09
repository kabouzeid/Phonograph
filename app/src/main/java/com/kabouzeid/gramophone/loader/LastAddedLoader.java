package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PreferenceUtils;

import java.util.ArrayList;

public class LastAddedLoader {

    public static ArrayList<Song> getLastAddedSongs(Context context) {
        return SongLoader.getSongs(makeLastAddedCursor(context));
    }

    public static Cursor makeLastAddedCursor(final Context context) {
        long fourWeeksAgo = (System.currentTimeMillis() / 1000) - (4 * 3600 * 24 * 7);
        // possible saved timestamp caused by user "clearing" the last added playlist
        long cutoff = PreferenceUtils.getInstance(context).getLastAddedCutOffTimestamp() / 1000;
        if (cutoff < fourWeeksAgo) {
            cutoff = fourWeeksAgo;
        }

        return SongLoader.makeSongCursor(
                context,
                MediaStore.Audio.Media.DATE_ADDED + ">",
                new String[]{String.valueOf(cutoff)},
                MediaStore.Audio.Media.DATE_ADDED + " DESC");
    }
}
