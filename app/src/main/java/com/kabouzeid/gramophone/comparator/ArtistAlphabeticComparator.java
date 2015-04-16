package com.kabouzeid.gramophone.comparator;

import com.kabouzeid.gramophone.model.Artist;

import java.util.Comparator;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistAlphabeticComparator implements Comparator<Artist> {
    @Override
    public int compare(Artist lhs, Artist rhs) {
        return lhs.name.trim().compareToIgnoreCase(rhs.name.trim());
    }
}
