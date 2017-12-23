package com.kabouzeid.gramophone.model;

import com.kabouzeid.gramophone.R;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("id")
    public Id id;

    @SerializedName("index")
    public int index;

    @SerializedName("checkBox")
    public boolean visible;

    public Category(Category category) {
        this.id = category.id;
        this.visible = category.visible;
        this.index = index;
    }

    public Category(Id id, boolean visible, int index) {
        this.id = id;
        this.visible = visible;
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        return id == category.id;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + id.hashCode();
        return result;
    }

    public String toString()
    {
        return "{id:"+id + ", pos:"+ index + ", vis=" + visible + "}";
    }

    public static enum Id
    {
        SONGS(R.string.songs),
        ALBUMS(R.string.albums),
        ARTISTS(R.string.artists),
        GENRES(R.string.genres),
        PLAYLISTS(R.string.playlists);

        public final int key;

        private Id(int key) {
            this.key = key;
        }
    }
}
