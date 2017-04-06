package com.kabouzeid.gramophone.service;


import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.kabouzeid.gramophone.service.automusicmodel.AutoMusicProvider;
import com.kabouzeid.gramophone.service.automusicmodel.MusicProviderSource;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.PackageValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.kabouzeid.gramophone.service.automusicmodel.MediaIDHelper.MEDIA_ID_EMPTY_ROOT;
import static com.kabouzeid.gramophone.service.automusicmodel.MediaIDHelper.MEDIA_ID_ROOT;

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
        //TODO: return the top node of the content hierarchy

        // To ensure you are not allowing any arbitrary app to browse your app's contents, you
        // need to check the origin:
        if (!mPackageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
            // If the request comes from an untrusted package, return an empty browser root.
            // If you return null, then the media browser will not be able to connect and
            // no further calls will be made to other media browsing methods.

            return new MediaBrowserServiceCompat.BrowserRoot(MEDIA_ID_EMPTY_ROOT, null);
        }
        //noinspection StatementWithEmptyBody
        //if (CarHelper.isValidCarPackage(clientPackageName)) {
            // Optional: if your app needs to adapt the music library to show a different subset
            // when connected to the car, this is where you should handle it.
            // If you want to adapt other runtime behaviors, like tweak ads or change some behavior
            // that should be different on cars, you should instead use the boolean flag
            // set by the BroadcastReceiver mCarConnectionReceiver (mIsConnectedToCar).
        //}
        return new BrowserRoot(MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentId, @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
        //TODO: get the children of root node, these children are used as a menu to the user
/*

        List<MediaBrowserCompat.MediaItem> list = mMusicProvider.getChildren(parentId, getResources());
        for(MediaMetadataCompat m : mMusicList){
            list.add(new MediaBrowserCompat.MediaItem(m.getDescription(),
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }
*/

        Log.d(TAG, "OnLoadChildren: parentMediaId=" + parentId);
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

    //TODO: implement playback controls with the appropriate methods
/*    private class MediaSessionCallback extends MediaSessionCompat.Callback{
        @Override
        public void onPlay() {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            mMediaPlayer.start();
            mMediaSession.setPlaybackState(buildState(PlaybackStateCompat.STATE_PLAYING));
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Log.v(TAG, "MediaID: " + mediaId);

            Uri mediaIDUri = Uri.parse(mediaId);
            String[] tokens = mediaIDUri.getPathSegments().get(1).split("\\|");
            int mediaID = Integer.parseInt(tokens[1]);

            Uri songUri = MusicUtil.getSongFileUri(mediaID);


            Uri mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String[] projection = new String[] { MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.ALBUM_ID};
            String selection = MediaStore.Audio.Media._ID + "=?";
            String[] selectionArgs = new String[] {"" + mediaID}; //This is the id you are looking for

            Cursor c = getContentResolver().query(mediaContentUri, projection, selection, selectionArgs, null);
            MediaMetadataCompat musicMetadata = null;
            if(c.moveToFirst()) {

                String _ID = c.getString(c.getColumnIndex(MediaStore.Audio.Media._ID));
                String title = c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album =c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String genre = "RANDOM";//c.getString(c.getColumnIndex(android.provider.MediaStore.Audio.Media.));
                String source = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
                //String iconUrl = c.getString(c.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE));
                //int trackNumber = c.getInt(c.getColumnIndex(MediaStore.Audio.Media.TRACK));
                //int totalTrackCount = c.getInt(c.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE));
                int duration = c.getInt(c.getColumnIndex(MediaStore.Audio.Media.DURATION)) * 1000; // ms

                musicMetadata = new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, _ID)
                        .putString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE, source)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                        .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                        //.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, iconUrl)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                        //.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
                        //.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                        .build();
            }
            handlePlay(songUri, musicMetadata);
        }

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
            super.onPlayFromSearch(query, extras);
        }

        @Override
        public void onPause() {
            mMediaPlayer.pause();
            mMediaSession.setPlaybackState(buildState(PlaybackStateCompat.STATE_PAUSED));
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
        }

        @Override
        public void onStop() {
            super.onStop();
        }
    }

    private PlaybackStateCompat buildState(int state){
        return new PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .setState(state, mMediaPlayer.getCurrentPosition(), 1, SystemClock.elapsedRealtime())
                .build();
    }

    private void handlePlay(Uri songUri, MediaMetadataCompat mediaMetadataCompat){
        mMediaSession.setPlaybackState(buildState(PlaybackStateCompat.STATE_PLAYING));
        mMediaSession.setMetadata(mediaMetadataCompat);

        try{
            Log.v(TAG, "media Uri: " + mediaMetadataCompat.getDescription().getMediaUri());
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(this, songUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
        mMediaPlayer.prepareAsync();

        //TODO: complete handle playback implementation
    }*/
}
