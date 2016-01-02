package com.kabouzeid.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Album implements Parcelable {
    public final ArrayList<Song> songs;

    public Album(ArrayList<Song> songs) {
        this.songs = songs;
    }

    public Album() {
        this.songs = new ArrayList<>();
    }

    public int getId() {
        return songs.get(0).albumId;
    }

    public String getTitle() {
        return songs.get(0).albumName;
    }

    public int getArtistId() {
        return songs.get(0).artistId;
    }

    public String getArtistName() {
        return songs.get(0).artistName;
    }

    public int getYear() {
        return songs.get(0).year;
    }

    public int getSongCount() {
        return songs.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Album that = (Album) o;

        return songs != null ? songs.equals(that.songs) : that.songs == null;

    }

    @Override
    public int hashCode() {
        return songs != null ? songs.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Album{" +
                "songs=" + songs +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(songs);
    }

    protected Album(Parcel in) {
        this.songs = in.createTypedArrayList(Song.CREATOR);
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        public Album createFromParcel(Parcel source) {
            return new Album(source);
        }

        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
}
