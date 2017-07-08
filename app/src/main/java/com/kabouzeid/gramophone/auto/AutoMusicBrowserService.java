package com.kabouzeid.gramophone.auto;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.util.PackageValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beesham Sarendranauth (Beesham)
 */
public class AutoMusicBrowserService extends MediaBrowserServiceCompat implements ServiceConnection {

    private static final String TAG = AutoMusicBrowserService.class.getCanonicalName();

    private AutoMusicProvider mMusicProvider;
    private PackageValidator mPackageValidator;
    private MediaSessionCompat mMediaSession;
    private MusicService mMusicService;

    public AutoMusicBrowserService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMusicProvider = new AutoMusicProvider(this);
        mPackageValidator = new PackageValidator(this);

        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(this);
    }

    private void createMediaSession() {
        setSessionToken(mMediaSession.getSessionToken());
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        // Check origin to ensure we're not allowing any arbitrary app to browse app contents
        if (!mPackageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
            // Request from an untrusted package: return an empty browser root
            return new MediaBrowserServiceCompat.BrowserRoot(MediaIDHelper.MEDIA_ID_EMPTY_ROOT, null);
        }

        return new BrowserRoot(MediaIDHelper.MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentId, @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
        if (MediaIDHelper.MEDIA_ID_EMPTY_ROOT.equals(parentId)) {
            result.sendResult(new ArrayList<MediaBrowserCompat.MediaItem>());
        } else if (mMusicProvider.isInitialized()) {
            result.sendResult(mMusicProvider.getChildren(parentId, getResources()));
        } else {
            result.detach();
            mMusicProvider.retrieveMediaAsync(new AutoMusicProvider.Callback() {
                @Override
                public void onMusicCatalogReady(boolean success) {
                    result.sendResult(mMusicProvider.getChildren(parentId, getResources()));
                }
            });
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
        mMusicService = binder.getService();
        mMediaSession = mMusicService.getMediaSession();
        createMediaSession();
        mMusicProvider.setMusicService(mMusicService);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mMusicService = null;
    }
}
