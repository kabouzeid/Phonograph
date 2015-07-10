package com.kabouzeid.gramophone.model;

import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Artist {
    public final int id;
    public final String name;
    public final int albumCount;
    public final int songCount;

    public Artist(final int id, final String name, final int albumCount, final int songCount) {
        this.id = id;
        this.name = name;
        this.songCount = songCount;
        this.albumCount = albumCount;
    }

    public Artist() {
        id = -1;
        name = "";
        songCount = -1;
        albumCount = -1;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + albumCount;
        result = prime * result + id;
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + songCount;
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
        final Artist other = (Artist) obj;
        if (albumCount != other.albumCount) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (!TextUtils.equals(name, other.name)) {
            return false;
        }
        return songCount == other.songCount;
    }

    @Override
    public String toString() {
        return name;
    }
}
