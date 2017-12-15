package com.kabouzeid.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Genre implements Parcelable {
    public final int id;
    public final String name;
    public final int songs;

    public Genre(final int id, final String name, final int songs) {
        this.id = id;
        this.name = name;
        this.songs = songs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Genre genre = (Genre) o;

        if (id != genre.id) return false;
        if (!name.equals(genre.name)) return false;
        return songs == genre.songs;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        result = 31 * result + songs;
        return result;
    }

    @Override
    public String toString() {
        return "Genre{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", songs=" + songs +
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
        dest.writeInt(this.songs);
    }

    protected Genre(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.songs = in.readInt();
    }

    public static final Creator<Genre> CREATOR = new Creator<Genre>() {
        public Genre createFromParcel(Parcel source) {
            return new Genre(source);
        }

        public Genre[] newArray(int size) {
            return new Genre[size];
        }
    };
}
