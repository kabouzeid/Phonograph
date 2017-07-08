package com.kabouzeid.gramophone.model.lyrics;

import android.util.SparseArray;

import com.kabouzeid.gramophone.model.Song;

public abstract class AbsSynchronizedLyrics extends Lyrics {
    private static final int TIME_OFFSET_MS = 500; // time adjustment to display line before it actually starts

    public final SparseArray<String> lines = new SparseArray<>();
    public int offset = 0;

    AbsSynchronizedLyrics(Song song, String data) {
        super(song, data);
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

    public boolean isSynchronized() {
        return true;
    }

    public boolean isValid() {
        this.parse(true);
        return this.valid;
    }

    @Override
    public String getText() {
        if (isValid()) {
            parse(false);

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.valueAt(i);
                sb.append(line).append('\n');
            }

            return sb.toString();
        }

        return super.getText();
    }
}
