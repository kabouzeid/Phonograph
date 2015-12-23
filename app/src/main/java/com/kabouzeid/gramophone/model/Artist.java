package com.kabouzeid.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Artist implements Parcelable {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Artist artist = (Artist) o;

        if (id != artist.id) return false;
        if (albumCount != artist.albumCount) return false;
        if (songCount != artist.songCount) return false;
        return name != null ? name.equals(artist.name) : artist.name == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + albumCount;
        result = 31 * result + songCount;
        return result;
    }

    @Override
    public String toString() {
        return "Artist{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", albumCount=" + albumCount +
                ", songCount=" + songCount +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.albumCount);
        dest.writeInt(this.songCount);
    }

    protected Artist(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.albumCount = in.readInt();
        this.songCount = in.readInt();
    }

    public static final Parcelable.Creator<Artist> CREATOR = new Parcelable.Creator<Artist>() {
        public Artist createFromParcel(Parcel source) {
            return new Artist(source);
        }

        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };
}
