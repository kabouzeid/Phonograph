package com.kabouzeid.gramophone.model.lyrics;

import android.util.SparseArray;

public abstract class AbsSynchronizedLyrics {
    private static final int TIME_OFFSET_MS = 500; // time adjustment to display line before it actually starts

    public final SparseArray<String> lines = new SparseArray<>();
    public boolean isValid = false;
    public int offset = 0;

    /**
     * @param data      Lyrics string
     * @param justCheck Set isValid = true and stop parsing if lyrics appears to be valid
     *                  and has at least 1 line
     */
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
        time += offset + AbsSynchronizedLyrics.TIME_OFFSET_MS;

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
