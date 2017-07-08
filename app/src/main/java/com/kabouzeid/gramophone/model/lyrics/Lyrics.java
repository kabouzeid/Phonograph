package com.kabouzeid.gramophone.model.lyrics;

import com.kabouzeid.gramophone.model.Song;

public class Lyrics {
    public Song song;
    public String data;

    boolean parsed = false;
    boolean valid = false;

    public Lyrics(Song song, String data) {
        this.song = song;
        this.data = data;
    }

    public static Lyrics parse(Song song, String data) {
        Lyrics lyrics = new SynchronizedLyricsLRC(song, data);
        if (lyrics.isValid()) {
            return lyrics.parse(false);
        } else {
            return new Lyrics(song, data).parse(false);
        }
    }

    public static boolean isSynchronized(String data) {
        Lyrics lyrics = new SynchronizedLyricsLRC(null, data);
        return lyrics.isValid();
    }

    public Lyrics parse(boolean check) {
        this.valid = true;
        this.parsed = true;
        return this;
    }

    public boolean isSynchronized() {
        return false;
    }

    public boolean isValid() {
        this.parse(true);
        return this.valid;
    }

    public String getText() {
        return this.data;
    }
}
