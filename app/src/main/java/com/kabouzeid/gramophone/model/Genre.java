package com.kabouzeid.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Genre implements Parcelable {
    public final int id;
    public final String name;
<<<<<<< HEAD

    public Genre(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    // For unknown genre
    public Genre(final String name) {
        this.id = -1;
        this.name = name;
=======
    public final int songCount;

    public Genre(final int id, final String name, final int songCount) {
        this.id = id;
        this.name = name;
        this.songCount = songCount;
>>>>>>> kabouzeid/master
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Genre genre = (Genre) o;

        if (id != genre.id) return false;
<<<<<<< HEAD
        return name != null ? name.equals(genre.name) : genre.name == null;
=======
        if (!name.equals(genre.name)) return false;
        return songCount == genre.songCount;
>>>>>>> kabouzeid/master
    }

    @Override
    public int hashCode() {
        int result = id;
<<<<<<< HEAD
        result = 31 * result + (name != null ? name.hashCode() : 0);
=======
        result = 31 * result + name.hashCode();
        result = 31 * result + songCount;
>>>>>>> kabouzeid/master
        return result;
    }

    @Override
    public String toString() {
        return "Genre{" +
                "id=" + id +
                ", name='" + name + '\'' +
<<<<<<< HEAD
=======
                ", songCount=" + songCount + '\'' +
>>>>>>> kabouzeid/master
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
<<<<<<< HEAD
=======
        dest.writeInt(this.songCount);
>>>>>>> kabouzeid/master
    }

    protected Genre(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
<<<<<<< HEAD
=======
        this.songCount = in.readInt();
>>>>>>> kabouzeid/master
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
