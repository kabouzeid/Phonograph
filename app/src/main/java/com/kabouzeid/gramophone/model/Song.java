package com.kabouzeid.gramophone.model;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.Serializable;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Song implements Serializable {

    private static final long serialVersionUID = 3720703366054566981L;

    public final int id;
    public final int albumId;
    public final int artistId;
    public final String title;
    public final String artistName;
    public final String albumName;
    public final long duration;
    public final int trackNumber;
    public final String data;

    public Song(final int id, final int albumId, final int artistId, final String title, final String artistName,
                final String albumName, final long duration, final int trackNumber, final String data) {
        this.id = id;
        this.albumId = albumId;
        this.artistId = artistId;
        this.title = title;
        this.artistName = artistName;
        this.albumName = albumName;
        this.duration = duration;
        this.trackNumber = trackNumber;
        this.data = data;
    }

    public Song() {
        this.id = -1;
        this.albumId = -1;
        this.artistId = -1;
        this.title = "";
        this.artistName = "";
        this.albumName = "";
        this.duration = -1;
        this.trackNumber = -1;
        this.data = "";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (albumName == null ? 0 : albumName.hashCode());
        result = prime * result + (artistName == null ? 0 : artistName.hashCode());
        result = prime * result + (int) duration;
        result = prime * result + id;
        result = prime * result + (title == null ? 0 : title.hashCode());
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
        final Song other = (Song) obj;
        if (id != other.id) {
            return false;
        }
        if (!TextUtils.equals(albumName, other.albumName)) {
            return false;
        }
        if (!TextUtils.equals(artistName, other.artistName)) {
            return false;
        }
        if (duration != other.duration) {
            return false;
        }
        return TextUtils.equals(title, other.title);
    }

    @Override
    public String toString() {
        return title;
    }
}
