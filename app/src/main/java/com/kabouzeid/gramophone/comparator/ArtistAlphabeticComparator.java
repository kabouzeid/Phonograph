package com.kabouzeid.gramophone.comparator;

import com.kabouzeid.gramophone.model.Artist;

import java.util.Comparator;

/**
 * Created by karim on 29.12.14.
 */
public class ArtistAlphabeticComparator implements Comparator<Artist> {
    @Override
    public int compare(Artist lhs, Artist rhs) {
        return lhs.name.trim().compareToIgnoreCase(rhs.name.trim());
    }
}
