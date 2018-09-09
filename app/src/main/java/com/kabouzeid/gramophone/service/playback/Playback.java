package com.kabouzeid.gramophone.service.playback;

import android.support.annotation.Nullable;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public interface Playback {

    boolean setDataSource(String path);

    void setNextDataSource(@Nullable String path);

    void setCallbacks(PlaybackCallbacks callbacks);

    boolean isInitialized();

    boolean start();

    void stop();

    void release();

    boolean pause();

    boolean isPlaying();

    int duration();

    int position();

    int seek(int whereto);

    boolean setAudioSessionId(int sessionId);

    int getAudioSessionId();

    void setReplaygain(float replaygain);

    void setDuckingFactor(float duckingFactor);

    interface PlaybackCallbacks {
        void onTrackWentToNext();

        void onTrackEnded();
    }
}
