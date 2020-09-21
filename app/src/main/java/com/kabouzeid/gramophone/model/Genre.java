package com.kabouzeid.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Genre implements Parcelable {
    public final long id;
    public final String name;
    public final int songCount;

    public Genre(final long id, final String name, final int songCount) {
        this.id = id;
        this.name = name;
        this.songCount = songCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Genre genre = (Genre) o;

        if (id != genre.id) return false;
        if (!name.equals(genre.name)) return false;
        return songCount == genre.songCount;
    }

    @Override
    public int hashCode() {
        long result = id;
        result = 31 * result + name.hashCode();
        result = 31 * result + songCount;
        return (int)result;
    }

    @Override
    public String toString() {
        return "Genre{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", songCount=" + songCount + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.songCount);
    }

    protected Genre(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.songCount = in.readInt();
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
