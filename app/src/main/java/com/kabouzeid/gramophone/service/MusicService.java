package com.kabouzeid.gramophone.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.appwidget.MusicPlayerWidget;
import com.kabouzeid.gramophone.helper.PlayingNotificationHelper;
import com.kabouzeid.gramophone.helper.ShuffleHelper;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.InternalStorageUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    public static final String PHONOGRAPH_PACKAGE_NAME = "com.kabouzeid.gramophone";
    public static final String MUSIC_PACKAGE_NAME = "com.android.music";

    public static final String ACTION_TOGGLE_PLAYBACK = "com.kabouzeid.gramophone.action.TOGGLE_PLAYBACK";
    public static final String ACTION_PLAY = "com.kabouzeid.gramophone.action.PLAY";
    public static final String ACTION_RESUME = "com.kabouzeid.gramophone.action.RESUME";
    public static final String ACTION_PAUSE = "com.kabouzeid.gramophone.action.PAUSE";
    public static final String ACTION_STOP = "com.kabouzeid.gramophone.action.STOP";
    public static final String ACTION_SKIP = "com.kabouzeid.gramophone.action.SKIP";
    public static final String ACTION_REWIND = "com.kabouzeid.gramophone.action.REWIND";
    public static final String ACTION_QUIT = "com.kabouzeid.gramophone.action.QUIT";

    public static final String META_CHANGED = "com.kabouzeid.gramophone.metachanged";
    public static final String PLAYSTATE_CHANGED = "com.kabouzeid.gramophone.playstatechanged";
    public static final String REPEATMODE_CHANGED = "com.kabouzeid.gramophone.repeatmodechanged";
    public static final String SHUFFLEMODE_CHANGED = "com.kabouzeid.gramophone.shufflemodechanged";
    public static final String POSITION_CHANGED = "com.kabouzeid.phonograph.positionchanged";

    private static final int FOCUSCHANGE = 5;
    private static final int DUCK = 6;
    private static final int UNDUCK = 7;
    private static final int FADEDOWNANDPAUSE = 8;
    private static final int FADEUPANDRESUME = 9;

    public static final int SHUFFLE_MODE_NONE = 0;
    public static final int SHUFFLE_MODE_SHUFFLE = 1;
    public static final int REPEAT_MODE_NONE = 0;
    public static final int REPEAT_MODE_ALL = 1;
    public static final int REPEAT_MODE_THIS = 2;
    private static final String TAG = MusicService.class.getSimpleName();
    private final IBinder musicBind = new MusicBinder();

    private MediaPlayer player;
    private ArrayList<Song> playingQueue;
    private ArrayList<Song> originalPlayingQueue;
    private int currentSongId = -1;
    private int position = -1;
    private int shuffleMode;
    private int repeatMode;
    private boolean isPlayerPrepared;
    private boolean pausedByTransientLossOfFocus;
    private boolean thingsRegistered;
    private boolean saveQueuesAgain;
    private boolean isSavingQueues;
    private PlayingNotificationHelper playingNotificationHelper;
    private AudioManager audioManager;
    private MediaSessionCompat mSession;
    private PowerManager.WakeLock wakeLock;
    private String currentAlbumArtUri;
    private MusicPlayerHandler playerHandler;
    private boolean fadingDown = false;
    private HandlerThread handlerThread;

    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().compareTo(AudioManager.ACTION_AUDIO_BECOMING_NOISY) == 0) {
                pausePlaying(true);
            }
        }
    };

    private final AudioManager.OnAudioFocusChangeListener audioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(final int focusChange) {
            playerHandler.obtainMessage(FOCUSCHANGE, focusChange, 0).sendToTarget();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        isPlayerPrepared = false;
        playingQueue = new ArrayList<>();
        originalPlayingQueue = new ArrayList<>();
        playingNotificationHelper = new PlayingNotificationHelper(this);

        shuffleMode = PreferenceManager.getDefaultSharedPreferences(this).getInt(AppKeys.SP_SHUFFLE_MODE, 0);
        repeatMode = PreferenceManager.getDefaultSharedPreferences(this).getInt(AppKeys.SP_REPEAT_MODE, 0);

        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wakeLock.setReferenceCounted(false);

        handlerThread = new HandlerThread("MusicPlayerHandler",
                Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        playerHandler = new MusicPlayerHandler(this, handlerThread.getLooper());

        registerEverything();

        setUpMediaSession();
    }

    private void setUpMediaSession() {
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(new ComponentName(getApplicationContext(), MediaButtonIntentReceiver.class));
        PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent, 0);

        mSession = new MediaSessionCompat(this, "Phonograph", new ComponentName(getApplicationContext(), MediaButtonIntentReceiver.class), mediaPendingIntent);
        mSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPause() {
                pausePlaying(false);
            }

            @Override
            public void onPlay() {
                resumePlaying(false);
            }

            @Override
            public void onSeekTo(long pos) {
                //TODO
                //seek(pos);
            }

            @Override
            public void onSkipToNext() {
                playNextSong(true);
            }

            @Override
            public void onSkipToPrevious() {
                playPreviousSong(true);
            }

            @Override
            public void onStop() {
                stopPlaying();
            }
        });
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
    }

    private void registerEverything() {
        if (!thingsRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
            registerReceiver(becomingNoisyReceiver, intentFilter);
            thingsRegistered = true;
        }
    }

    private AudioManager getAudioManager() {
        if (audioManager == null) {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        return audioManager;
    }

    private void initRemoteControlClient() {
//        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
//        mediaButtonIntent.setComponent(new ComponentName(getApplicationContext(), MediaButtonIntentReceiver.class));
//        PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent, 0);
//        remoteControlClient = new RemoteControlClient(mediaPendingIntent);
//        remoteControlClient.setTransportControlFlags(
//                RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
//                        RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
//                        RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
//                        RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS);
//        getAudioManager().registerRemoteControlClient(remoteControlClient);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setUpMediaPlayerIfNeeded();
        if (intent != null) {
            if (intent.getAction() != null) {
                String action = intent.getAction();
                switch (action) {
                    case ACTION_TOGGLE_PLAYBACK:
                        if (isPlaying()) {
                            pausePlaying(false);
                        } else {
                            resumePlaying(false);
                        }
                        break;
                    case ACTION_PAUSE:
                        pausePlaying(false);
                        break;
                    case ACTION_PLAY:
                        playSong();
                        break;
                    case ACTION_RESUME:
                        resumePlaying(false);
                        break;
                    case ACTION_REWIND:
                        back(true);
                        break;
                    case ACTION_SKIP:
                        playNextSong(true);
                        break;
                    case ACTION_STOP:
                        stopPlaying();
                        break;
                    case ACTION_QUIT:
                        killEverythingAndReleaseResources();
                        break;
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        closeAudioEffectSession();
        unregisterEverything();

        playerHandler.removeCallbacksAndMessages(null);
        handlerThread.quitSafely();

        killEverythingAndReleaseResources();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        unregisterEverything();
        killEverythingAndReleaseResources();
        return false;
    }

    private void unregisterEverything() {
        if (thingsRegistered) {
            unregisterReceiver(becomingNoisyReceiver);
            getAudioManager().abandonAudioFocus(audioFocusListener);
            thingsRegistered = false;
        }
    }

    private void killEverythingAndReleaseResources() {
        stopPlaying();
        playingNotificationHelper.killNotification();
        savePosition();
        saveQueues();
        stopSelf();
    }

    public void stopPlaying() {
        pausedByTransientLossOfFocus = false;
        isPlayerPrepared = false;
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        mSession.setActive(false);
        mSession.release();
        notifyChange(PLAYSTATE_CHANGED);
    }

    public boolean isPlaying() {
        return player != null && isPlayerPrepared && !fadingDown && player.isPlaying();
    }

    public void saveQueues() {
        try {
            InternalStorageUtil.writeObject(MusicService.this, AppKeys.IS_PLAYING_QUEUE, playingQueue);
            InternalStorageUtil.writeObject(MusicService.this, AppKeys.IS_ORIGINAL_PLAYING_QUEUE, originalPlayingQueue);
        } catch (IOException e) {
            Log.e(TAG, "error while saving music service queue state", e);
        }
    }

    public void savePosition() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InternalStorageUtil.writeObject(MusicService.this, AppKeys.IS_POSITION_IN_QUEUE, getPosition());
                } catch (IOException e) {
                    Log.e(TAG, "error while saving music service position state", e);
                }
            }
        }).start();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (isLastTrack() && getRepeatMode() == REPEAT_MODE_NONE) {
            notifyChange(PLAYSTATE_CHANGED);
        } else {
            acquireWakeLock(30000);
            playNextSong(false);
        }
    }

    public void playNextSong(boolean force) {
        if (position != -1) {
            if (isPlayerPrepared) {
                setPosition(getNextPosition(force));
                playSong();
            }
        }
    }

    public void playSong() {
        if (requestFocus()) {
            setUpMediaPlayerIfNeeded();
            registerEverything();
            isPlayerPrepared = false;
            player.reset();
            if (position != -1) {
                try {
                    Uri trackUri = getCurrentPositionTrackUri();
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.setDataSource(getApplicationContext(), trackUri);
                    player.prepareAsync();
                } catch (Exception e) {
                    player.reset();
                    player = null;
                    notifyChange(PLAYSTATE_CHANGED);

                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.unplayable_file), Toast.LENGTH_SHORT).show();
                    return;
                }
                currentSongId = playingQueue.get(getPosition()).id;
                notifyChange(META_CHANGED);
            } else {
                notifyChange(PLAYSTATE_CHANGED);
            }
        }
    }

    private void openAudioEffectSession() {
        if (player != null) {
            final Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
            intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
            intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
            intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
            sendBroadcast(intent);
        }
    }

    private void closeAudioEffectSession() {
        if (player != null) {
            final Intent audioEffectsIntent = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
            audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, player.getAudioSessionId());
            audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
            sendBroadcast(audioEffectsIntent);
        }
    }

    private boolean requestFocus() {
        return (getAudioManager().requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

    private void updateMediaSession(final String what) {
        final Song song = playingQueue.get(getPosition());

        int playState = isPlaying()
                ? PlaybackState.STATE_PLAYING
                : PlaybackState.STATE_PAUSED;

        if (what.equals(PLAYSTATE_CHANGED) || what.equals(POSITION_CHANGED)) {
            mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(playState, getSongProgressMillis(), 1.0f).build());
        } else if (what.equals(META_CHANGED)) {
            mSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, song.artistName)
                    .putString(MediaMetadata.METADATA_KEY_ALBUM, song.albumName)
                    .putString(MediaMetadata.METADATA_KEY_TITLE, song.title)
                    .putLong(MediaMetadata.METADATA_KEY_DURATION, song.duration)
                    .putLong(MediaMetadata.METADATA_KEY_TRACK_NUMBER, getPosition() + 1)
                    .putLong(MediaMetadata.METADATA_KEY_NUM_TRACKS, getPlayingQueue().size())
                    .build());

            mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(playState, getSongProgressMillis(), 1.0f).build());
        }

        currentAlbumArtUri = MusicUtil.getAlbumArtUri(song.albumId).toString();
        ImageLoader.getInstance().displayImage(currentAlbumArtUri, new NonViewAware(new ImageSize(-1, -1), ViewScaleType.CROP), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (currentAlbumArtUri.equals(imageUri)) {
                    Bitmap albumArt = loadedImage;
                    if (albumArt != null) {
                        // RemoteControlClient wants to recycle the bitmaps thrown at it, so we need
                        // to make sure not to hand out our cache copy
                        Bitmap.Config config = albumArt.getConfig();
                        if (config == null) {
                            config = Bitmap.Config.ARGB_8888;
                        }
                        albumArt = albumArt.copy(config, false);
                        updateMediaSessionBitmap(albumArt.copy(albumArt.getConfig(), true));
                    }
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (currentAlbumArtUri.equals(imageUri))
                    updateMediaSessionBitmap(null);
            }
        });
    }

    private void updateMediaSessionBitmap(final Bitmap albumArt) {
        MediaMetadataCompat current = mSession.getController().getMetadata();
        if (current == null) current = new MediaMetadataCompat.Builder().build();

        mSession.setMetadata(new MediaMetadataCompat.Builder(current)
                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumArt)
                .build());
    }

    private void setUpMediaPlayerIfNeeded() {
        if (player == null) {
            player = new MediaPlayer();

            player.setOnPreparedListener(this);
            player.setOnCompletionListener(this);
            player.setOnErrorListener(this);

            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        }
    }

    private void updateNotification() {
        playingNotificationHelper.buildNotification(playingQueue.get(position), isPlaying());
    }

    private void updateWidgets() {
        MusicPlayerWidget.updateWidgets(this, playingQueue.get(position), isPlaying());
    }

    private Uri getCurrentPositionTrackUri() {
        return ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, playingQueue.get(position).id);
    }

    public int getNextPosition(boolean force) {
        int position = 0;
        switch (repeatMode) {
            case REPEAT_MODE_NONE:
                position = getPosition() + 1;
                if (isLastTrack()) {
                    position -= 1;
                }
                break;
            case REPEAT_MODE_ALL:
                position = getPosition() + 1;
                if (isLastTrack()) {
                    position = 0;
                }
                break;
            case REPEAT_MODE_THIS:
                if (force) {
                    position = getPosition() + 1;
                    if (isLastTrack()) {
                        position = 0;
                    }
                } else {
                    position = getPosition();
                }
                break;
        }
        return position;
    }

    private boolean isLastTrack() {
        return getPosition() == getPlayingQueue().size() - 1;
    }

    public ArrayList<Song> getPlayingQueue() {
        return playingQueue;
    }

    public int getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(final int repeatMode) {
        switch (repeatMode) {
            case REPEAT_MODE_NONE:
            case REPEAT_MODE_ALL:
            case REPEAT_MODE_THIS:
                this.repeatMode = repeatMode;
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putInt(AppKeys.SP_REPEAT_MODE, repeatMode)
                        .apply();
                notifyChange(REPEATMODE_CHANGED);
                break;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        isPlayerPrepared = false;
        player.reset();
        player = null;
        notifyChange(PLAYSTATE_CHANGED);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPlayerPrepared = true;
        openAudioEffectSession();
        resumePlaying(false);
        savePosition();
        releaseWakeLock();
    }

    public void openQueue(final ArrayList<Song> playingQueue, final int startPosition, final boolean startPlaying) {
        if (playingQueue != null && !playingQueue.isEmpty() && startPosition >= 0 && startPosition < playingQueue.size()) {
            originalPlayingQueue = playingQueue;
            this.playingQueue = new ArrayList<>(originalPlayingQueue);
            setPosition(startPosition);
            if (shuffleMode == SHUFFLE_MODE_SHUFFLE) {
                ShuffleHelper.makeShuffleList(this.playingQueue, startPosition);
                setPosition(0);
            }
            if (startPlaying) {
                playSong();
            }
            saveState();
        }
    }

    public void saveState() {
        saveQueuesAsync();
        savePosition();
    }

    public void saveQueuesAsync() {
        if (isSavingQueues) {
            saveQueuesAgain = true;
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    isSavingQueues = true;
                    do {
                        saveQueuesAgain = false;
                        saveQueues();
                    } while (saveQueuesAgain);
                    isSavingQueues = false;
                }
            }).start();
        }
    }

    public void restorePreviousState(final ArrayList<Song> originalPlayingQueue, final ArrayList<Song> playingQueue, int position) {
        this.originalPlayingQueue = originalPlayingQueue;
        this.playingQueue = playingQueue;
        this.position = position;
        saveState();
    }

    public void addSong(int position, Song song) {
        playingQueue.add(position, song);
        originalPlayingQueue.add(position, song);
        saveState();
    }

    public void addSong(Song song) {
        playingQueue.add(song);
        originalPlayingQueue.add(song);
        saveState();
    }

    public void addSongs(int position, List<Song> songs) {
        playingQueue.addAll(position, songs);
        originalPlayingQueue.addAll(position, songs);
        saveState();
    }

    public void addSongs(List<Song> songs) {
        playingQueue.addAll(songs);
        originalPlayingQueue.addAll(songs);
        saveState();
    }

    public void removeSong(int position) {
        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            playingQueue.remove(position);
            originalPlayingQueue.remove(position);
        } else {
            originalPlayingQueue.remove(playingQueue.remove(position));
        }
        saveState();
    }

    public void removeSong(Song song) {
        for (int i = 0; i < playingQueue.size(); i++) {
            if (playingQueue.get(i).id == song.id) playingQueue.remove(i);
        }
        for (int i = 0; i < originalPlayingQueue.size(); i++) {
            if (originalPlayingQueue.get(i).id == song.id) originalPlayingQueue.remove(i);
        }
        if (song.id == currentSongId) {
            playSong();
        }
        saveState();
    }

    public void moveSong(int from, int to) {
        final int currentPosition = getPosition();
        Song songToMove = playingQueue.remove(from);
        playingQueue.add(to, songToMove);
        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            Song tmpSong = originalPlayingQueue.remove(from);
            originalPlayingQueue.add(to, tmpSong);
        }
        if (from > currentPosition && to <= currentPosition) {
            setPosition(getPosition() + 1);
        } else if (from < currentPosition && to >= currentPosition) {
            setPosition(getPosition() - 1);
        } else if (from == currentPosition) {
            setPosition(to);
        }
        saveState();
    }

    public long getCurrentSongId() {
        return currentSongId;
    }

    public void playSongAt(final int position) {
        if (position < getPlayingQueue().size() && position >= 0) {
            setPosition(position);
            playSong();
        } else {
            Log.e(TAG, "No song in queue at given index!");
        }
    }

    public void pausePlaying(boolean forceNoFading) {
        pausedByTransientLossOfFocus = false;
        if (!forceNoFading && PreferenceUtils.getInstance(this).fadePlayPauseAndInterruptions()) {
            playerHandler.removeMessages(FADEUPANDRESUME);
            playerHandler.sendEmptyMessage(FADEDOWNANDPAUSE);
        } else {
            pause();
        }
    }

    private void pause() {
        fadingDown = false;
        if (isPlaying()) {
            player.pause();
            notifyChange(PLAYSTATE_CHANGED);
        }
    }

    public void resumePlaying(boolean forceNoFading) {
        mSession.setActive(true);
        if (!forceNoFading && PreferenceUtils.getInstance(this).fadePlayPauseAndInterruptions()) {
            playerHandler.removeMessages(FADEDOWNANDPAUSE);
            playerHandler.sendEmptyMessage(FADEUPANDRESUME);
        } else {
            player.setVolume(1f, 1f);
            resume();
        }
    }

    private void resume() {
        fadingDown = false;
        if (!isPlaying()) {
            if (requestFocus()) {
                if (isPlayerPrepared()) {
                    player.start();
                    notifyChange(PLAYSTATE_CHANGED);
                } else {
                    playSong();
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.audio_focus_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void playPreviousSong(boolean force) {
        if (position != -1) {
            setPosition(getPreviousPosition(force));
            playSong();
        }
    }

    public void back(boolean force) {
        if (position != -1) {
            if (isPlayerPrepared() && getSongProgressMillis() > 2000) {
                seekTo(0);
            } else {
                playPreviousSong(force);
            }
        }
    }

    public int getPreviousPosition(boolean force) {
        int position = 0;
        switch (repeatMode) {
            case REPEAT_MODE_NONE:
                position = getPosition() - 1;
                if (position < 0) {
                    position = 0;
                }
                break;
            case REPEAT_MODE_ALL:
                position = getPosition() - 1;
                if (position < 0) {
                    position = getPlayingQueue().size() - 1;
                }
                break;
            case REPEAT_MODE_THIS:
                if (force) {
                    position = getPosition() - 1;
                    if (position < 0) {
                        position = getPlayingQueue().size() - 1;
                    }
                } else {
                    position = getPosition();
                }
                break;
        }
        return position;
    }

    public int getSongProgressMillis() {
        return player.getCurrentPosition();
    }

    public int getSongDurationMillis() {
        return player.getDuration();
    }

    public void seekTo(int millis) {
        player.seekTo(millis);
    }

    public boolean isPlayerPrepared() {
        return player != null && isPlayerPrepared;
    }

    public void cycleRepeatMode() {
        switch (getRepeatMode()) {
            case REPEAT_MODE_NONE:
                setRepeatMode(REPEAT_MODE_ALL);
                break;
            case REPEAT_MODE_ALL:
                setRepeatMode(REPEAT_MODE_THIS);
                break;
            default:
                setRepeatMode(REPEAT_MODE_NONE);
                break;
        }
    }

    public void toggleShuffle() {
        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            setShuffleMode(SHUFFLE_MODE_SHUFFLE);
        } else {
            setShuffleMode(SHUFFLE_MODE_NONE);
        }
    }

    public int getShuffleMode() {
        return shuffleMode;
    }

    public void setShuffleMode(final int shuffleMode) {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putInt(AppKeys.SP_SHUFFLE_MODE, shuffleMode)
                .apply();
        switch (shuffleMode) {
            case SHUFFLE_MODE_SHUFFLE:
                this.shuffleMode = shuffleMode;
                ShuffleHelper.makeShuffleList(this.playingQueue, getPosition());
                setPosition(0);
                break;
            case SHUFFLE_MODE_NONE:
                this.shuffleMode = shuffleMode;
                playingQueue = new ArrayList<>(originalPlayingQueue);
                int newPosition = 0;
                for (Song song : playingQueue) {
                    if (song.id == currentSongId) {
                        newPosition = playingQueue.indexOf(song);
                    }
                }
                setPosition(newPosition);
                break;
        }
        notifyChange(SHUFFLEMODE_CHANGED);
    }

    private void notifyChange(final String what) {
        updateMediaSession(what);
        if (what.equals(POSITION_CHANGED)) {
            return;
        }

        final Intent internalIntent = new Intent(what);
        final int position = getPosition();
        if (position >= 0 && !playingQueue.isEmpty()) {
            final Song currentSong = playingQueue.get(position);
            internalIntent.putExtra("id", currentSong.id);
            internalIntent.putExtra("artist", currentSong.artistName);
            internalIntent.putExtra("album", currentSong.albumName);
            internalIntent.putExtra("track", currentSong.title);
        }
        internalIntent.putExtra("playing", isPlaying());
        sendStickyBroadcast(internalIntent);

        //to let other apps know whats playing. i.E. last.fm (scrobbling) or musixmatch
        final Intent publicMusicIntent = new Intent(internalIntent);
        publicMusicIntent.setAction(what.replace(PHONOGRAPH_PACKAGE_NAME, MUSIC_PACKAGE_NAME));
        sendStickyBroadcast(publicMusicIntent);

        if (what.equals(PLAYSTATE_CHANGED)) {
            final boolean isPlaying = isPlaying();
            playingNotificationHelper.updatePlayState(isPlaying);
            MusicPlayerWidget.updateWidgetsPlayState(this, isPlaying);
        } else if (what.equals(META_CHANGED)) {
            updateNotification();
            updateWidgets();
        }
    }

    public int getAudioSessionId() {
        if (player == null)
            return AudioEffect.ERROR_BAD_VALUE;
        return player.getAudioSessionId();
    }

    private void releaseWakeLock() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private void acquireWakeLock(long milli) {
        wakeLock.acquire(milli);
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    private static final class MusicPlayerHandler extends Handler {
        private final WeakReference<MusicService> mService;
        private float currentDuckVolume = 1.0f;
        private float currentPlayPauseFadeVolume = 1.0f;

        public MusicPlayerHandler(final MusicService service, final Looper looper) {
            super(looper);
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(final Message msg) {
            final MusicService service = mService.get();
            if (service == null) {
                return;
            }

            switch (msg.what) {
                case DUCK:
                    currentDuckVolume -= .05f;
                    if (currentDuckVolume > .2f) {
                        sendEmptyMessageDelayed(DUCK, 10);
                    } else {
                        currentDuckVolume = .2f;
                    }
                    service.player.setVolume(currentDuckVolume, currentDuckVolume);
                    break;

                case UNDUCK:
                    currentDuckVolume += .05f;
                    if (currentDuckVolume < 1.0f) {
                        sendEmptyMessageDelayed(UNDUCK, 10);
                    } else {
                        currentDuckVolume = 1.0f;
                    }
                    service.player.setVolume(currentDuckVolume, currentDuckVolume);
                    break;

                case FADEDOWNANDPAUSE:
                    if (!service.fadingDown) {
                        service.fadingDown = true;
                        service.notifyChange(PLAYSTATE_CHANGED);
                    }
                    service.fadingDown = true;
                    currentPlayPauseFadeVolume -= .1f;
                    if (currentPlayPauseFadeVolume > 0f) {
                        sendEmptyMessageDelayed(FADEDOWNANDPAUSE, 10);
                    } else {
                        currentPlayPauseFadeVolume = 0f;
                        service.fadingDown = false;
                        service.pausePlaying(true);
                    }
                    service.player.setVolume(currentPlayPauseFadeVolume, currentPlayPauseFadeVolume);
                    break;

                case FADEUPANDRESUME:
                    service.resume();
                    currentPlayPauseFadeVolume += .1f;
                    if (currentPlayPauseFadeVolume < 1.0f) {
                        sendEmptyMessageDelayed(FADEUPANDRESUME, 10);
                    } else {
                        currentPlayPauseFadeVolume = 1.0f;
                    }
                    service.player.setVolume(currentPlayPauseFadeVolume, currentPlayPauseFadeVolume);
                    break;

                case FOCUSCHANGE:
                    switch (msg.arg1) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                            service.registerEverything();
                            if (!service.isPlaying() && service.pausedByTransientLossOfFocus) {
                                service.resumePlaying(false);
                            }
                            removeMessages(DUCK);
                            sendEmptyMessage(UNDUCK);
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS:
                            // Lost focus for an unbounded amount of time: stop playback and release media player
                            service.pausePlaying(true);
                            service.unregisterEverything();
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            // Lost focus for a short time, but we have to stop
                            // playback. We don't release the media player because playback
                            // is likely to resume
                            service.pausePlaying(false);
                            service.pausedByTransientLossOfFocus = service.isPlaying();
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            // Lost focus for a short time, but it's ok to keep playing
                            // at an attenuated level
                            if (!service.isPlayerPrepared()) {
                                service.setUpMediaPlayerIfNeeded();
                            }
                            removeMessages(UNDUCK);
                            sendEmptyMessage(DUCK);
                            break;
                    }
                    break;
            }
        }
    }
}
