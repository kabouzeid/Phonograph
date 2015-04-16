package com.kabouzeid.gramophone.comparator;

import com.kabouzeid.gramophone.model.Song;

import java.util.Comparator;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongTrackNumberComparator implements Comparator<Song> {
    @Override
    public int compare(Song lhs, Song rhs) {
        // 0 gleich
        // -1 steht über dem anderen
        // 1 steht unter dem anderen
        if (lhs.trackNumber == rhs.trackNumber) {
            return 0;
        }
        if (lhs.trackNumber > rhs.trackNumber) {
            return 1;
        }
        return -1;
    }
}
