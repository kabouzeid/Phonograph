package com.kabouzeid.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Album implements Parcelable {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Album album = (Album) o;

        if (id != album.id) return false;
        if (artistId != album.artistId) return false;
        if (songCount != album.songCount) return false;
        if (year != album.year) return false;
        if (title != null ? !title.equals(album.title) : album.title != null) return false;
        return artistName != null ? artistName.equals(album.artistName) : album.artistName == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + artistId;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (artistName != null ? artistName.hashCode() : 0);
        result = 31 * result + songCount;
        result = 31 * result + year;
        return result;
    }

    @Override
    public String toString() {
        return "Album{" +
                "id=" + id +
                ", artistId=" + artistId +
                ", title='" + title + '\'' +
                ", artistName='" + artistName + '\'' +
                ", songCount=" + songCount +
                ", year=" + year +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.artistId);
        dest.writeString(this.title);
        dest.writeString(this.artistName);
        dest.writeInt(this.songCount);
        dest.writeInt(this.year);
    }

    protected Album(Parcel in) {
        this.id = in.readInt();
        this.artistId = in.readInt();
        this.title = in.readString();
        this.artistName = in.readString();
        this.songCount = in.readInt();
        this.year = in.readInt();
    }

    public static final Parcelable.Creator<Album> CREATOR = new Parcelable.Creator<Album>() {
        public Album createFromParcel(Parcel source) {
            return new Album(source);
        }

        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
}
