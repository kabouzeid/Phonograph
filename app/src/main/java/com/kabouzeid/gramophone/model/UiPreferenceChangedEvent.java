package com.kabouzeid.gramophone.model;

/**
 * Created by karim on 28.03.15.
 */
public class UiPreferenceChangedEvent {
    public static final int THEME_CHANGED = 0;
    public static final int ALBUM_OVERVIEW_PALETTE_CHANGED = 1;
    public static final int COLORED_NAVIGATION_BAR_ARTIST_CHANGED = 2;
    public static final int COLORED_NAVIGATION_BAR_ALBUM_CHANGED = 3;
    public static final int PLAYBACK_CONTROLLER_CARD_CHANGED = 4;
    public static final int TOOLBAR_TRANSPARENT_CHANGED = 5;

    private int action;
    private Object value;

    public UiPreferenceChangedEvent(int action, Object value) {
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
