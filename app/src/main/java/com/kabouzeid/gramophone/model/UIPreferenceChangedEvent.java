package com.kabouzeid.gramophone.model;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class UIPreferenceChangedEvent {
    public static final int THEME_CHANGED = 0;
    public static final int ALBUM_OVERVIEW_PALETTE_CHANGED = 1;
    public static final int COLORED_NAVIGATION_BAR_ARTIST_CHANGED = 2;
    public static final int COLORED_NAVIGATION_BAR_ALBUM_CHANGED = 3;
    public static final int COLORED_NAVIGATION_BAR_PLAYLIST_CHANGED = 4;
    public static final int COLORED_NAVIGATION_BAR_TAG_EDITOR_CHANGED = 5;
    public static final int COLORED_NAVIGATION_BAR_CURRENT_PLAYING_CHANGED = 6;
    public static final int COLORED_NAVIGATION_BAR_CHANGED = 10;
    public static final int COLORED_NAVIGATION_BAR_OTHER_SCREENS_CHANGED = 7;
    public static final int TOOLBAR_TRANSPARENT_CHANGED = 8;

    private final int action;
    private final Object value;

    public UIPreferenceChangedEvent(int action, Object value) {
        this.action = action;
        this.value = value;
    }

    public int getAction() {
        return action;
    }

    public Object getValue() {
        return value;
    }
}
