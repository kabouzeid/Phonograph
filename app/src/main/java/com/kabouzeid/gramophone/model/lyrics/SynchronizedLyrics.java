package com.kabouzeid.gramophone.model.lyrics;

import android.util.SparseArray;

public abstract class SynchronizedLyrics {
    public final SparseArray<String> lines = new SparseArray<>();

    public static SynchronizedLyrics parse(String data)
    {
        return new SynchronizedLyricsLRC(data); // no another formats at the moment
    }

    public String getLine(int time)
    {
        time += 500; // small time adjustment to display line before it actually starts

        int lastLineTime = lines.keyAt(0);

        for(int i = 0; i < lines.size(); i++) {
            int lineTime = lines.keyAt(i);

            if(time >= lineTime) {
                lastLineTime = lineTime;
            }
            else {
                break;
            }
        }

        return lines.get(lastLineTime);
    }
}
