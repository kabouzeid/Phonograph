package com.kabouzeid.gramophone.ui.activities.base;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;

import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.MusicStateListener;
import com.kabouzeid.gramophone.service.MusicService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsMusicStateActivity extends AbsBaseActivity implements ServiceConnection, MusicStateListener {
    public static final String TAG = AbsMusicStateActivity.class.getSimpleName();

    private final ArrayList<MusicStateListener> mMusicStateListener = new ArrayList<>();

    private MusicPlayerRemote.ServiceToken serviceToken;
    private MusicStateReceiver musicStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceToken = MusicPlayerRemote.bindToService(this, this);
        musicStateReceiver = new MusicStateReceiver(this);

        final IntentFilter filter = new IntentFilter();
        filter.addAction(MusicService.PLAY_STATE_CHANGED);
        filter.addAction(MusicService.SHUFFLE_MODE_CHANGED);
        filter.addAction(MusicService.REPEAT_MODE_CHANGED);
        filter.addAction(MusicService.META_CHANGED);
        filter.addAction(MusicService.MEDIA_STORE_CHANGED);

        registerReceiver(musicStateReceiver, filter);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        onPlayStateChanged();
        onPlayingMetaChanged();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MusicPlayerRemote.unbindFromService(serviceToken);
        unregisterReceiver(musicStateReceiver);
    }

    public void addMusicStateListenerListener(final MusicStateListener listener) {
        if (listener != null) {
            mMusicStateListener.add(listener);
        }
    }

    public void removeMusicStateListenerListener(final MusicStateListener listener) {
        if (listener != null) {
            mMusicStateListener.remove(listener);
        }
    }

    @Override
    public void onPlayingMetaChanged() {
        for (MusicStateListener listener : mMusicStateListener) {
            if (listener != null) {
                listener.onPlayingMetaChanged();
            }
        }
    }

    @Override
    public void onPlayStateChanged() {
        for (MusicStateListener listener : mMusicStateListener) {
            if (listener != null) {
                listener.onPlayStateChanged();
            }
        }
    }

    @Override
    public void onMediaStoreChanged() {
        for (MusicStateListener listener : mMusicStateListener) {
            if (listener != null) {
                listener.onMediaStoreChanged();
            }
        }
    }

    public void onRepeatModeChanged() {

    }

    public void onShuffleModeChanged() {

    }

    private static final class MusicStateReceiver extends BroadcastReceiver {

        private final WeakReference<AbsMusicStateActivity> reference;

        public MusicStateReceiver(final AbsMusicStateActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(final Context context, @NonNull final Intent intent) {
            final String action = intent.getAction();
            AbsMusicStateActivity activity = reference.get();
            if (activity != null) {
                switch (action) {
                    case MusicService.META_CHANGED:
                        activity.onPlayingMetaChanged();
                        break;
                    case MusicService.PLAY_STATE_CHANGED:
                        activity.onPlayStateChanged();
                        break;
                    case MusicService.REPEAT_MODE_CHANGED:
                        activity.onRepeatModeChanged();
                        break;
                    case MusicService.SHUFFLE_MODE_CHANGED:
                        activity.onShuffleModeChanged();
                        break;
                    case MusicService.MEDIA_STORE_CHANGED:
                        activity.onMediaStoreChanged();
                        break;
                }
            }
        }
    }
}
