package com.kabouzeid.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Song implements Parcelable {
    public static final Song EMPTY_SONG = new Song(-1, "", -1, -1, -1, "", -1, -1, "", "", -1, "");

    public final int id;
    public final String title;
    public final int trackNumber;
    public final int year;
    public final long duration;
    public final String data;
    public final long dateModified;
    public final int albumId;
    public final String albumName;
    public final String albumArtist;
    public final int artistId;
    public final String artistName;

    public Song(int id, String title, int trackNumber, int year, long duration, String data, long dateModified, int albumId, String albumName, String albumArtist, int artistId, String artistName) {
        this.id = id;
        this.title = title;
        this.trackNumber = trackNumber;
        this.year = year;
        this.duration = duration;
        this.data = data;
        this.dateModified = dateModified;
        this.albumId = albumId;
        this.albumName = albumName;
        this.albumArtist = albumArtist;
        this.artistId = artistId;
        this.artistName = artistName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        if (id != song.id) return false;
        if (trackNumber != song.trackNumber) return false;
        if (year != song.year) return false;
        if (duration != song.duration) return false;
        if (dateModified != song.dateModified) return false;
        if (albumId != song.albumId) return false;
        if (artistId != song.artistId) return false;
        if (title != null ? !title.equals(song.title) : song.title != null) return false;
        if (data != null ? !data.equals(song.data) : song.data != null) return false;
        if (albumName != null ? !albumName.equals(song.albumName) : song.albumName != null)
            return false;
        if (albumArtist != null ? !albumArtist.equals(song.albumArtist) : song.albumArtist != null)
            return false;
        return artistName != null ? artistName.equals(song.artistName) : song.artistName == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + trackNumber;
        result = 31 * result + year;
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (int) (dateModified ^ (dateModified >>> 32));
        result = 31 * result + albumId;
        result = 31 * result + (albumName != null ? albumName.hashCode() : 0);
        result = 31 * result + (albumArtist != null ? albumArtist.hashCode() : 0);
        result = 31 * result + artistId;
        result = 31 * result + (artistName != null ? artistName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", trackNumber=" + trackNumber +
                ", year=" + year +
                ", duration=" + duration +
                ", data='" + data + '\'' +
                ", dateModified=" + dateModified +
                ", albumId=" + albumId +
                ", albumName='" + albumName + '\'' +
                ", albumArtist='" + albumArtist + '\'' +
                ", artistId=" + artistId +
                ", artistName='" + artistName + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.title);
        dest.writeInt(this.trackNumber);
        dest.writeInt(this.year);
        dest.writeLong(this.duration);
        dest.writeString(this.data);
        dest.writeLong(this.dateModified);
        dest.writeInt(this.albumId);
        dest.writeString(this.albumName);
        dest.writeString(this.albumArtist);
        dest.writeInt(this.artistId);
        dest.writeString(this.artistName);
    }

    protected Song(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.trackNumber = in.readInt();
        this.year = in.readInt();
        this.duration = in.readLong();
        this.data = in.readString();
        this.dateModified = in.readLong();
        this.albumId = in.readInt();
        this.albumName = in.readString();
        this.albumArtist = in.readString();
        this.artistId = in.readInt();
        this.artistName = in.readString();
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
