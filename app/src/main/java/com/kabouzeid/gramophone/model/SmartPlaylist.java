package com.kabouzeid.gramophone.model;

import android.content.Context;
import android.support.annotation.DrawableRes;

import com.kabouzeid.gramophone.R;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class SmartPlaylist extends Playlist {
    private static final long serialVersionUID = 3013701295356403681L;

    @DrawableRes
    public final int iconRes;

    public SmartPlaylist(final String name, final int iconRes) {
        super(-1, name);
        this.iconRes = iconRes;
    }

    public SmartPlaylist() {
        super();
        this.iconRes = R.drawable.ic_queue_music_white_24dp;
    }

    public abstract ArrayList<Song> getSongs(Context context);

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + iconRes;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (super.equals(obj)) {
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SmartPlaylist other = (SmartPlaylist) obj;
            return iconRes == other.iconRes;
        }
        return false;
    }
}
