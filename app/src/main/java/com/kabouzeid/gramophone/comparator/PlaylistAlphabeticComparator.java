package com.kabouzeid.gramophone.comparator;

import com.kabouzeid.gramophone.model.Playlist;

import java.util.Comparator;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistAlphabeticComparator implements Comparator<Playlist> {
    @Override
    public int compare(Playlist lhs, Playlist rhs) {
        return lhs.name.trim().compareToIgnoreCase(rhs.name.trim());
    }
}
