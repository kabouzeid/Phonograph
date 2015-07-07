package com.kabouzeid.gramophone.ui.activities.base;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.service.MusicService;

import java.lang.ref.WeakReference;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsPlaybackControlActivity extends AbsBaseActivity {
    private MusicPlayerRemote.ServiceToken serviceToken;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceToken = MusicPlayerRemote.bindToService(this, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                AbsPlaybackControlActivity.this.onServiceConnected();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        });
        playbackStatusReceiver = new PlaybackStatusReceiver(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(MusicService.PLAY_STATE_CHANGED);
        filter.addAction(MusicService.SHUFFLE_MODE_CHANGED);
        filter.addAction(MusicService.REPEAT_MODE_CHANGED);
        filter.addAction(MusicService.META_CHANGED);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MusicPlayerRemote.unbindFromService(serviceToken);
    }

    private static final class PlaybackStatusReceiver extends BroadcastReceiver {

        private final WeakReference<AbsPlaybackControlActivity> reference;

        public PlaybackStatusReceiver(final AbsPlaybackControlActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case MusicService.META_CHANGED:
                    reference.get().onPlayingMetaChanged();
                    break;
                case MusicService.PLAY_STATE_CHANGED:
                    reference.get().onPlayStateChanged();
                    break;
                case MusicService.REPEAT_MODE_CHANGED:
                    reference.get().onRepeatModeChanged();
                    break;
                case MusicService.SHUFFLE_MODE_CHANGED:
                    reference.get().onShuffleModeChanged();
                    break;
            }
        }
    }
}
