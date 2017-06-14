package com.kabouzeid.gramophone.model.lyrics;

import android.util.SparseArray;

public abstract class AbsSynchronizedLyrics {
    public final SparseArray<String> lines = new SparseArray<>();
    public boolean isValid = false;

    public static AbsSynchronizedLyrics parse(String data, boolean justCheck) {
        return new SynchronizedLyricsLRC(data, justCheck); // no another formats at the moment
    }

    public static AbsSynchronizedLyrics parse(String data) {
        return parse(data, false);
    }

    public static boolean isSynchronized(String data) {
        AbsSynchronizedLyrics lyrics = parse(data, true);
        return lyrics.isValid;
    }

    public String getLine(int time) {
        time += 500; // small time adjustment to display line before it actually starts

        int lastLineTime = lines.keyAt(0);

        for (int i = 0; i < lines.size(); i++) {
            int lineTime = lines.keyAt(i);

            if (time >= lineTime) {
                lastLineTime = lineTime;
            } else {
                break;
            }
        }

        return lines.get(lastLineTime);
    }
}
