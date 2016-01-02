package com.kabouzeid.gramophone.comparator;

import com.kabouzeid.gramophone.model.Song;

import java.util.Comparator;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongTrackComparator implements Comparator<Song> {
    @Override
    public int compare(Song lhs, Song rhs) {
        return lhs.trackNumber - rhs.trackNumber;
    }
}
