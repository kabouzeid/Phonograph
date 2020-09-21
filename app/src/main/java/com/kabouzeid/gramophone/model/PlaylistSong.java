package com.kabouzeid.gramophone.model;

import android.os.Parcel;

public class PlaylistSong extends Song {
    public static final PlaylistSong EMPTY_PLAYLIST_SONG = new PlaylistSong(-1, "", -1, -1, -1, "", -1, -1, "", -1, "", -1, -1);

    public final long playlistId;
    public final long idInPlayList;

    public PlaylistSong(long id, String title, int trackNumber, int year, long duration, String data, int dateModified, long albumId, String albumName, long artistId, String artistName, final long playlistId, final long idInPlayList) {
        super(id, title, trackNumber, year, duration, data, dateModified, albumId, albumName, artistId, artistName);
        this.playlistId = playlistId;
        this.idInPlayList = idInPlayList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PlaylistSong that = (PlaylistSong) o;

        if (playlistId != that.playlistId) return false;
        return idInPlayList == that.idInPlayList;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int)playlistId;
        result = 31 * result + (int)idInPlayList;
        return result;
    }

    @Override
    public String toString() {
        return super.toString() +
                "PlaylistSong{" +
                "playlistId=" + playlistId +
                ", idInPlayList=" + idInPlayList +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(this.playlistId);
        dest.writeLong(this.idInPlayList);
    }

    protected PlaylistSong(Parcel in) {
        super(in);
        this.playlistId = in.readLong();
        this.idInPlayList = in.readLong();
    }

    public static final Creator<PlaylistSong> CREATOR = new Creator<PlaylistSong>() {
        public PlaylistSong createFromParcel(Parcel source) {
            return new PlaylistSong(source);
        }

        public PlaylistSong[] newArray(int size) {
            return new PlaylistSong[size];
        }
    };
}
