package com.kabouzeid.materialmusic.comparator;

import com.kabouzeid.materialmusic.model.Artist;

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
