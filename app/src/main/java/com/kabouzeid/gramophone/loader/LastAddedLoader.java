package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import androidx.annotation.NonNull;

import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

public class LastAddedLoader {
    public static final int LIMIT_OF_LAST_ADDED_TRACKS = 20;

    @NonNull
    public static List<Song> getLastAddedSongs(@NonNull Context context) {
        List<Song> songs = SongLoader.getSongs(makeLastAddedCursor(context));
        return songs.subList(0, Math.min(songs.size(), LIMIT_OF_LAST_ADDED_TRACKS));
    }

    public static Cursor makeLastAddedCursor(@NonNull final Context context) {
        long cutoff = PreferenceUtil.getInstance(context).getLastAddedCutoff();

        return SongLoader.makeSongCursor(
                context,
                MediaStore.Audio.Media.DATE_ADDED + ">?",
                new String[]{String.valueOf(cutoff)},
                MediaStore.Audio.Media.DATE_ADDED + " DESC");
    }
}
