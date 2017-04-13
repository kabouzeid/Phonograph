package com.kabouzeid.gramophone.service;

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


import com.kabouzeid.gramophone.modelAndroidAuto.AutoMusicProvider;
import com.kabouzeid.gramophone.util.PackageValidator;

import java.util.ArrayList;
import java.util.List;

import static com.kabouzeid.gramophone.modelAndroidAuto.MediaIDHelper.MEDIA_ID_EMPTY_ROOT;
import static com.kabouzeid.gramophone.modelAndroidAuto.MediaIDHelper.MEDIA_ID_ROOT;

public class AutoMusicBrowserService extends MediaBrowserServiceCompat implements ServiceConnection {

    private final static String TAG = AutoMusicBrowserService.class.getCanonicalName();

    private AutoMusicProvider mMusicProvider;
    private PackageValidator mPackageValidator;
    private MediaSessionCompat mMediaSession;

    private boolean mBound;

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

    private void createMediaSession(){
        setSessionToken(mMediaSession.getSessionToken());
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        // To ensure you are not allowing any arbitrary app to browse your app's contents, you
        // need to check the origin:
        if (!mPackageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
            // If the request comes from an untrusted package, return an empty browser root.
            // If you return null, then the media browser will not be able to connect and
            // no further calls will be made to other media browsing methods.

            return new MediaBrowserServiceCompat.BrowserRoot(MEDIA_ID_EMPTY_ROOT, null);
        }

        return new BrowserRoot(MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentId, @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
        if (MEDIA_ID_EMPTY_ROOT.equals(parentId)) {
            result.sendResult(new ArrayList<MediaBrowserCompat.MediaItem>());
        }else if (mMusicProvider.isInitialized()) {
            // if music library is ready, return immediately
            result.sendResult(mMusicProvider.getChildren(parentId, getResources()));
        } else {
            // otherwise, only return results when the music library is retrieved
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
        MusicService musicService = binder.getService();
        mMediaSession = musicService.getMediaSession();
        createMediaSession();
        mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mBound = false;
    }
}
