package com.kabouzeid.materialmusic.model;

/**
 * Created by karim on 19.12.14.
 */
public class MusicRemoteEvent {
    public static final int PLAY = 0;
    public static final int PAUSE = 1;
    public static final int RESUME = 2;
    public static final int STOP = 3;
    public static final int NEXT = 4;
    public static final int PREV = 5;

    public static final int SONG_COMPLETED = 6;
    public static final int QUEUE_COMPLETED = 7;

    public static final int SERVICE_CONNECTED = 8;
    public static final int SERVICE_DISCONNECTED = 9;

    public static final int STATE_SAVED = 10;
    public static final int STATE_RESTORED = 11;

    public static final int SHUFFLE_MODE_CHANGED = 12;
    public static final int REPEAT_MODE_CHANGED = 13;

    private int action;

    public MusicRemoteEvent(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }
}
