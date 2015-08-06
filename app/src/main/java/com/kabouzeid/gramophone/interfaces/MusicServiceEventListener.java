package com.kabouzeid.gramophone.interfaces;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public interface MusicServiceEventListener {
    void onPlayingMetaChanged();

    void onPlayStateChanged();

    void onRepeatModeChanged();

    void onShuffleModeChanged();

    void onMediaStoreChanged();
}
