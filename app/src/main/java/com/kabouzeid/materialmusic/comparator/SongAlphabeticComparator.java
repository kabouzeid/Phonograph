package com.kabouzeid.materialmusic.comparator;

import com.kabouzeid.materialmusic.model.Song;

import java.util.Comparator;

/**
 * Created by karim on 28.12.14.
 */
public class SongAlphabeticComparator implements Comparator<Song> {
    @Override
    public int compare(Song lhs, Song rhs) {
        return lhs.title.trim().compareToIgnoreCase(rhs.title.trim());
    }
}
