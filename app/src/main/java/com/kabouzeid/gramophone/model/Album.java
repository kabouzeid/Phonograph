package com.kabouzeid.gramophone.model;

import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Album {

    public final int id;
    public final int artistId;
    public final String title;
    public final String artistName;
    public final int songCount;
    public final int year;

    public Album(final int id, final String title, final String artistName, final int artistId,
                 final int songNumber, final int albumYear) {
        this.id = id;
        this.title = title;
        this.artistName = artistName;
        this.artistId = artistId;
        songCount = songNumber;
        year = albumYear;
    }

    public Album() {
        this.id = -1;
        this.title = "";
        this.artistName = "";
        this.artistId = -1;
        songCount = -1;
        year = -1;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + (title == null ? 0 : title.hashCode());
        result = prime * result + (artistName == null ? 0 : artistName.hashCode());
        result = prime * result + songCount;
        result = prime * result + year;
        return result;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Album other = (Album) obj;
        if (id != other.id) {
            return false;
        }
        if (!TextUtils.equals(title, other.title)) {
            return false;
        }
        if (!TextUtils.equals(artistName, other.artistName)) {
            return false;
        }
        if (songCount != other.songCount) {
            return false;
        }
        return year == other.year;
    }

    @Override
    public String toString() {
        return title;
    }
}
