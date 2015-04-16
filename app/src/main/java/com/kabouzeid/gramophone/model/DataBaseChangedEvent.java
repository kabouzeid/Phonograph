package com.kabouzeid.gramophone.model;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class DataBaseChangedEvent {
    public static final int PLAYLISTS_CHANGED = 0;
    public static final int ALBUMS_CHANGED = 1;
    public static final int ARTISTS_CHANGED = 2;
    public static final int SONGS_CHANGED = 3;
    public static final int DATABASE_CHANGED = 4;

    private final int action;

    public DataBaseChangedEvent(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }
}
