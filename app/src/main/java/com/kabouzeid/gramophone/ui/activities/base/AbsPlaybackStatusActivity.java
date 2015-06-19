package com.kabouzeid.gramophone.ui.activities.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.service.MusicService;

import java.lang.ref.WeakReference;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsPlaybackStatusActivity extends AbsBaseActivity {
    private PlaybackStatusReceiver playbackStatusReceiver;

    public void onPlayingMetaChanged() {

    }

    public void onPlayStateChanged() {

    }

    public void onRepeatModeChanged() {

    }

    public void onShuffleModeChanged() {

    }

    public void onServiceConnected() {

    }

    @Override
    protected void onStart() {
        super.onStart();

        playbackStatusReceiver = new PlaybackStatusReceiver(this);

        // ensures that onServiceConnected() is called even if the service is already connected and wont sent the Intent again.
        if (MusicPlayerRemote.isServiceConnected()) {
            onServiceConnected();
        }

        final IntentFilter filter = new IntentFilter();
        filter.addAction(MusicService.PLAYSTATE_CHANGED);
        filter.addAction(MusicService.SHUFFLEMODE_CHANGED);
        filter.addAction(MusicService.REPEATMODE_CHANGED);
        filter.addAction(MusicService.META_CHANGED);
        filter.addAction(MusicPlayerRemote.SERVICE_BOUND);

        registerReceiver(playbackStatusReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(playbackStatusReceiver);
        } catch (Throwable ignored) {
        }
    }

    private static final class PlaybackStatusReceiver extends BroadcastReceiver {

        private final WeakReference<AbsPlaybackStatusActivity> reference;

        public PlaybackStatusReceiver(final AbsPlaybackStatusActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case MusicService.META_CHANGED:
                    reference.get().onPlayingMetaChanged();
                    break;
                case MusicService.PLAYSTATE_CHANGED:
                    reference.get().onPlayStateChanged();
                    break;
                case MusicService.REPEATMODE_CHANGED:
                    reference.get().onRepeatModeChanged();
                    break;
                case MusicService.SHUFFLEMODE_CHANGED:
                    reference.get().onShuffleModeChanged();
                    break;
                case MusicPlayerRemote.SERVICE_BOUND:
                    reference.get().onServiceConnected();
                    break;
            }
        }
    }
}
