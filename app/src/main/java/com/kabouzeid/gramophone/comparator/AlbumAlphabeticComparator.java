package com.kabouzeid.gramophone.comparator;

import com.kabouzeid.gramophone.model.Album;

import java.util.Comparator;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumAlphabeticComparator implements Comparator<Album> {
    @Override
    public int compare(Album lhs, Album rhs) {
        return lhs.title.trim().compareToIgnoreCase(rhs.title.trim());
    }
}