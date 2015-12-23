package com.kabouzeid.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Song implements Parcelable {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        if (id != song.id) return false;
        if (albumId != song.albumId) return false;
        if (artistId != song.artistId) return false;
        if (duration != song.duration) return false;
        if (trackNumber != song.trackNumber) return false;
        if (title != null ? !title.equals(song.title) : song.title != null) return false;
        if (artistName != null ? !artistName.equals(song.artistName) : song.artistName != null)
            return false;
        if (albumName != null ? !albumName.equals(song.albumName) : song.albumName != null)
            return false;
        return data != null ? data.equals(song.data) : song.data == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + albumId;
        result = 31 * result + artistId;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (artistName != null ? artistName.hashCode() : 0);
        result = 31 * result + (albumName != null ? albumName.hashCode() : 0);
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        result = 31 * result + trackNumber;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", albumId=" + albumId +
                ", artistId=" + artistId +
                ", title='" + title + '\'' +
                ", artistName='" + artistName + '\'' +
                ", albumName='" + albumName + '\'' +
                ", duration=" + duration +
                ", trackNumber=" + trackNumber +
                ", data='" + data + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.albumId);
        dest.writeInt(this.artistId);
        dest.writeString(this.title);
        dest.writeString(this.artistName);
        dest.writeString(this.albumName);
        dest.writeLong(this.duration);
        dest.writeInt(this.trackNumber);
        dest.writeString(this.data);
    }

    protected Song(Parcel in) {
        this.id = in.readInt();
        this.albumId = in.readInt();
        this.artistId = in.readInt();
        this.title = in.readString();
        this.artistName = in.readString();
        this.albumName = in.readString();
        this.duration = in.readLong();
        this.trackNumber = in.readInt();
        this.data = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}
