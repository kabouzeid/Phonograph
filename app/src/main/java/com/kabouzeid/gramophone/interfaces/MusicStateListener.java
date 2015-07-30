package com.kabouzeid.gramophone.interfaces;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public interface MusicStateListener {
    void onPlayingMetaChanged();

    void onPlayStateChanged();

    void onMediaStoreChanged();
}
