package com.kabouzeid.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Album implements Parcelable {
    public final List<Song> songs;

    public Album(List<Song> songs) {
        this.songs = songs;
    }

    public Album() {
        this.songs = new ArrayList<>();
    }

    public long getId() {
        return safeGetFirstSong().albumId;
    }

    public String getTitle() {
        return safeGetFirstSong().albumName;
    }

    public long getArtistId() {
        return safeGetFirstSong().artistId;
    }

    public String getArtistName() {
        return safeGetFirstSong().artistName;
    }

    public int getYear() {
        return safeGetFirstSong().year;
    }

    public long getDateModified() {
        return safeGetFirstSong().dateModified;
    }

    public int getSongCount() {
        return songs.size();
    }

    @NonNull
    public Song safeGetFirstSong() {
        return songs.isEmpty() ? Song.EMPTY_SONG : songs.get(0);
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
