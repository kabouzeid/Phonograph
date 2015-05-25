package com.kabouzeid.gramophone.model;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Playlist implements Serializable {

    private static final long serialVersionUID = 3013703495354856981L;

    public final int id;
    public final String name;

    public Playlist(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    public Playlist() {
        this.id = -1;
        this.name = "";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + (name == null ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Playlist other = (Playlist) obj;
        if (id != other.id) {
            return false;
        }
        return TextUtils.equals(name, other.name);
    }

    @Override
    public String toString() {
        return name;
    }
}
