package com.kabouzeid.gramophone.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.audiofx.AudioEffect;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.appwidget.WidgetMedium;
import com.kabouzeid.gramophone.helper.PlayingNotificationHelper;
import com.kabouzeid.gramophone.helper.ShuffleHelper;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.provider.MusicPlaybackQueueStore;
import com.kabouzeid.gramophone.provider.RecentlyPlayedStore;
import com.kabouzeid.gramophone.provider.SongPlayCountStore;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid), Andrew Neal
 */
public class MusicService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String TAG = MusicService.class.getSimpleName();

    public static final String PHONOGRAPH_PACKAGE_NAME = "com.kabouzeid.gramophone";
    public static final String MUSIC_PACKAGE_NAME = "com.android.music";

    public static final String ACTION_TOGGLE_PAUSE = "com.kabouzeid.gramophone.togglepause";
    public static final String ACTION_PLAY = "com.kabouzeid.gramophone.play";
    public static final String ACTION_PAUSE = "com.kabouzeid.gramophone.pause";
    public static final String ACTION_STOP = "com.kabouzeid.gramophone.stop";
    public static final String ACTION_SKIP = "com.kabouzeid.gramophone.skip";
    public static final String ACTION_REWIND = "com.kabouzeid.gramophone.rewind";
    public static final String ACTION_QUIT = "com.kabouzeid.gramophone.quitservice";

    // do not change this strings as it will break support with other apps (e.g. last.fm scrobbling)
    public static final String META_CHANGED = "com.kabouzeid.gramophone.metachanged";
    public static final String QUEUE_CHANGED = "com.kabouzeid.gramophone.queuechanged";
    public static final String PLAY_STATE_CHANGED = "com.kabouzeid.gramophone.playstatechanged";
    public static final String REPEAT_MODE_CHANGED = "com.kabouzeid.gramophone.repeatmodechanged";
    public static final String SHUFFLE_MODE_CHANGED = "com.kabouzeid.gramophone.shufflemodechanged";
    public static final String MEDIA_STORE_CHANGED = "com.kabouzeid.gramophone.mediastorechanged";

    public static final String SAVED_POSITION = "POSITION";
    public static final String SAVED_POSITION_IN_TRACK = "POSITION_IN_TRACK";
    public static final String SAVED_SHUFFLE_MODE = "SHUFFLE_MODE";
    public static final String SAVED_REPEAT_MODE = "REPEAT_MODE";
    public static final int RELEASE_WAKELOCK = 10;
    public static final int TRACK_ENDED = 11;
    public static final int TRACK_WENT_TO_NEXT = 12;
    public static final int PLAY_SONG = 13;
    public static final int SAVE_QUEUES = 14;
    public static final int PREPARE_NEXT = 15;
    public static final int SHUFFLE_MODE_NONE = 0;
    public static final int SHUFFLE_MODE_SHUFFLE = 1;
    public static final int REPEAT_MODE_NONE = 0;
    public static final int REPEAT_MODE_ALL = 1;
    public static final int REPEAT_MODE_THIS = 2;
    private static final int FOCUS_CHANGE = 5;
    private static final int DUCK = 6;
    private static final int UNDUCK = 7;
    private final IBinder musicBind = new MusicBinder();

    private MultiPlayer player;
    private ArrayList<Song> playingQueue;
    private ArrayList<Song> originalPlayingQueue;
    private int position = -1;
    private int nextPosition = -1;
    private int shuffleMode;
    private int repeatMode;
    private boolean pausedByTransientLossOfFocus;
    private boolean receiversAndRemoteControlClientRegistered;
    private PlayingNotificationHelper playingNotificationHelper;
    private AudioManager audioManager;
    @SuppressWarnings("deprecation")
    private RemoteControlClient remoteControlClient;
    private PowerManager.WakeLock wakeLock;
    private MusicPlayerHandler playerHandler;
    private final AudioManager.OnAudioFocusChangeListener audioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(final int focusChange) {
            playerHandler.obtainMessage(FOCUS_CHANGE, focusChange, 0).sendToTarget();
        }
    };
    private QueueSaveHandler queueSaveHandler;
    private HandlerThread musicPlayerHandlerThread;
    private HandlerThread queueSaveHandlerThread;
    private RecentlyPlayedStore recentlyPlayedStore;
    private SongPlayCountStore songPlayCountStore;
    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                pause();
            }
        }
    };
    private ContentObserver mediaStoreObserver;
    private boolean notHandledMetaChangedForCurrentTrack;
    private boolean isServiceInUse;

    private static String getTrackUri(@NonNull Song song) {
        return MusicUtil.getSongUri(song.id).toString();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        playingQueue = new ArrayList<>();
        originalPlayingQueue = new ArrayList<>();

        playingNotificationHelper = new PlayingNotificationHelper(this);

        recentlyPlayedStore = RecentlyPlayedStore.getInstance(this);
        songPlayCountStore = SongPlayCountStore.getInstance(this);

        shuffleMode = PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_SHUFFLE_MODE, 0);
        repeatMode = PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_REPEAT_MODE, 0);

        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wakeLock.setReferenceCounted(false);

        musicPlayerHandlerThread = new HandlerThread("MusicPlayerHandler",
                Process.THREAD_PRIORITY_BACKGROUND);
        musicPlayerHandlerThread.start();
        playerHandler = new MusicPlayerHandler(this, musicPlayerHandlerThread.getLooper());

        player = new MultiPlayer(this);
        player.setHandler(playerHandler);

        // queue saving needs to run on a separate thread so that it doesn't block the player handler events
        queueSaveHandlerThread = new HandlerThread("QueueSaveHandler",
                Process.THREAD_PRIORITY_BACKGROUND);
        queueSaveHandlerThread.start();
        queueSaveHandler = new QueueSaveHandler(this, queueSaveHandlerThread.getLooper());

        registerReceiversAndRemoteControlClient();

        restoreQueueAndPosition();

        mediaStoreObserver = new MediaStoreObserver(playerHandler);
        getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI, true, mediaStoreObserver);
        getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, mediaStoreObserver);

        PreferenceUtil.getInstance(this).registerOnSharedPreferenceChangedListener(this);

        notifyChange(SHUFFLE_MODE_CHANGED);
        notifyChange(REPEAT_MODE_CHANGED);
    }

    private void registerReceiversAndRemoteControlClient() {
        if (!receiversAndRemoteControlClientRegistered) {
            registerReceiver(becomingNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
            //noinspection deprecation
            getAudioManager().registerMediaButtonEventReceiver(new ComponentName(getApplicationContext(), MediaButtonIntentReceiver.class));
            initRemoteControlClient();
            receiversAndRemoteControlClientRegistered = true;
        }
    }

    private AudioManager getAudioManager() {
        if (audioManager == null) {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        return audioManager;
    }

    @SuppressWarnings("deprecation")
    private void initRemoteControlClient() {
        remoteControlClient = new RemoteControlClient(getMediaButtonIntent());
        remoteControlClient.setTransportControlFlags(
                RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                        RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                        RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                        RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS);
        getAudioManager().registerRemoteControlClient(remoteControlClient);
    }

    private PendingIntent getMediaButtonIntent() {
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(new ComponentName(getApplicationContext(), MediaButtonIntentReceiver.class));
        return PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent, 0);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getAction() != null) {
                String action = intent.getAction();
                switch (action) {
                    case ACTION_TOGGLE_PAUSE:
                        if (isPlaying()) {
                            pause();
                        } else {
                            play();
                        }
                        break;
                    case ACTION_PAUSE:
                        pause();
                        break;
                    case ACTION_PLAY:
                        play();
                        break;
                    case ACTION_REWIND:
                        back(true);
                        break;
                    case ACTION_SKIP:
                        playNextSong(true);
                        break;
                    case ACTION_STOP:
                        stop();
                        break;
                    case ACTION_QUIT:
                        return quit();
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        quit();
        releaseResources();
        getContentResolver().unregisterContentObserver(mediaStoreObserver);
        PreferenceUtil.getInstance(this).unregisterOnSharedPreferenceChangedListener(this);
        wakeLock.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        isServiceInUse = true;
        return musicBind;
    }

    @Override
    public void onRebind(Intent intent) {
        isServiceInUse = true;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isServiceInUse = false;
        if (!isPlaying()) {
            stopSelf();
        }
        return true;
    }

    private void unregisterReceiversAndRemoteControlClient() {
        if (receiversAndRemoteControlClientRegistered) {
            unregisterReceiver(becomingNoisyReceiver);
            //noinspection deprecation
            getAudioManager().unregisterRemoteControlClient(remoteControlClient);
            //noinspection deprecation
            getAudioManager().unregisterMediaButtonEventReceiver(new ComponentName(getApplicationContext(), MediaButtonIntentReceiver.class));
            receiversAndRemoteControlClientRegistered = false;
        }
    }

    private int quit() {
        unregisterReceiversAndRemoteControlClient();
        pause();
        playingNotificationHelper.killNotification();

        if (isServiceInUse) {
            return START_STICKY;
        } else {
            closeAudioEffectSession();
            stopSelf();
            return START_NOT_STICKY;
        }
    }

    private void releaseResources() {
        playerHandler.removeCallbacksAndMessages(null);
        if (Build.VERSION.SDK_INT >= 18) {
            musicPlayerHandlerThread.quitSafely();
        } else {
            musicPlayerHandlerThread.quit();
        }
        queueSaveHandler.removeCallbacksAndMessages(null);
        if (Build.VERSION.SDK_INT >= 18) {
            queueSaveHandlerThread.quitSafely();
        } else {
            queueSaveHandlerThread.quit();
        }
        player.release();
        player = null;
    }

    public void stop() {
        pausedByTransientLossOfFocus = false;
        savePositionInTrack();
        player.stop();
        notifyChange(PLAY_STATE_CHANGED);
        getAudioManager().abandonAudioFocus(audioFocusListener);
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void saveState() {
        saveQueues();
        savePosition();
        savePositionInTrack();
    }

    private void saveQueues() {
        queueSaveHandler.removeMessages(SAVE_QUEUES);
        queueSaveHandler.sendEmptyMessage(SAVE_QUEUES);
    }

    private void saveQueuesImpl() {
        MusicPlaybackQueueStore.getInstance(this).saveQueues(playingQueue, originalPlayingQueue);
    }

    private void savePosition() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(SAVED_POSITION, getPosition()).apply();
    }

    private void savePositionInTrack() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(SAVED_POSITION_IN_TRACK, getSongProgressMillis()).apply();
    }

    public int getPosition() {
        return position;
    }

    private void setPosition(int position) {
        this.position = position;
    }

    public void playNextSong(boolean force) {
        playSongAt(getNextPosition(force));
    }

    private boolean openTrackAndPrepareNextAt(int position) {
        synchronized (this) {
            setPosition(position);
            boolean prepared = openCurrent();
            if (prepared) prepareNextImpl();
            notifyChange(META_CHANGED);
            notHandledMetaChangedForCurrentTrack = false;
            return prepared;
        }
    }

    private boolean openCurrent() {
        synchronized (this) {
            try {
                return player.setDataSource(getTrackUri(getCurrentSong()));
            } catch (Exception e) {
                return false;
            }
        }
    }

    private void prepareNext() {
        playerHandler.removeMessages(PREPARE_NEXT);
        playerHandler.obtainMessage(PREPARE_NEXT).sendToTarget();
    }

    private boolean prepareNextImpl() {
        synchronized (this) {
            try {
                int nextPosition = getNextPosition(false);
                player.setNextDataSource(getTrackUri(getSongAt(nextPosition)));
                this.nextPosition = nextPosition;
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    private void closeAudioEffectSession() {
        final Intent audioEffectsIntent = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, player.getAudioSessionId());
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(audioEffectsIntent);
    }

    private boolean requestFocus() {
        return (getAudioManager().requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

    private void updateRemoteControlClient() {
        updateRemoteControlClientImpl(PreferenceUtil.getInstance(this).albumArtOnLockscreen());
    }

    private void updateRemoteControlClientImpl(boolean showAlbumArt) {
        final Song song = getCurrentSong();
        remoteControlClient
                .editMetadata(!showAlbumArt)
                .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, song.artistName)
                .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, song.title)
                .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, song.duration)
                .apply();
        if (showAlbumArt) {
            final String currentAlbumArtUri = MusicUtil.getSongImageLoaderString(song);
            Point screenSize = Util.getScreenSize(this);
            ImageLoader.getInstance().displayImage(
                    currentAlbumArtUri,
                    new NonViewAware(new ImageSize(screenSize.x, screenSize.y), ViewScaleType.CROP),
                    new DisplayImageOptions.Builder()
                            .postProcessor(new BitmapProcessor() {
                                @Override
                                public Bitmap process(Bitmap bitmap) {
                                    // RemoteControlClient wants to recycle the bitmaps thrown at it, so we need
                                    // to make sure not to hand out our cache copy
                                    Bitmap.Config config = bitmap.getConfig();
                                    if (config == null) {
                                        config = Bitmap.Config.ARGB_8888;
                                    }
                                    bitmap = bitmap.copy(config, false);
                                    return bitmap.copy(bitmap.getConfig(), true);
                                }
                            }).build(),
                    new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, @Nullable Bitmap loadedImage) {
                            if (currentAlbumArtUri.equals(imageUri)) {
                                updateRemoteControlClientBitmap(loadedImage);
                            }
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            if (currentAlbumArtUri.equals(imageUri)) {
                                updateRemoteControlClientBitmap(null);
                            }
                        }
                    });
        } else {
            updateRemoteControlClientBitmap(null);
        }
    }

    private void updateRemoteControlClientBitmap(final Bitmap albumArt) {
        //noinspection deprecation
        remoteControlClient
                .editMetadata(false)
                .putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, albumArt)
                .apply();
    }

    public Song getCurrentSong() {
        return getSongAt(getPosition());
    }

    public Song getSongAt(int position) {
        if (position >= 0 && position < getPlayingQueue().size()) {
            return getPlayingQueue().get(position);
        } else {
            return new Song();
        }
    }

    private void updateNotification() {
        playingNotificationHelper.updateNotification();
    }

    private void updateWidgets() {
        WidgetMedium.updateWidgets(this, getCurrentSong(), isPlaying());
    }

    public int getNextPosition(boolean force) {
        int position = getPosition() + 1;
        switch (getRepeatMode()) {
            case REPEAT_MODE_ALL:
                if (isLastTrack()) {
                    position = 0;
                }
                break;
            case REPEAT_MODE_THIS:
                if (force) {
                    if (isLastTrack()) {
                        position = 0;
                    }
                } else {
                    position -= 1;
                }
                break;
            default:
            case REPEAT_MODE_NONE:
                if (isLastTrack()) {
                    position -= 1;
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
                        .putInt(SAVED_REPEAT_MODE, repeatMode)
                        .apply();
                prepareNext();
                notifyChange(REPEAT_MODE_CHANGED);
                break;
        }
    }

    public void openAndPlayQueue(@Nullable final ArrayList<Song> playingQueue, final int startPosition, final boolean startPlaying) {
        if (playingQueue != null && !playingQueue.isEmpty() && startPosition >= 0 && startPosition < playingQueue.size()) {
            originalPlayingQueue = playingQueue;
            this.playingQueue = new ArrayList<>(originalPlayingQueue);

            int position = startPosition;
            if (shuffleMode == SHUFFLE_MODE_SHUFFLE) {
                ShuffleHelper.makeShuffleList(this.playingQueue, startPosition);
                position = 0;
            }
            if (startPlaying) {
                playSongAt(position);
            }
            notifyChange(QUEUE_CHANGED);
        }
    }

    private void restoreQueueAndPosition() {
        ArrayList<Song> restoredQueue = MusicPlaybackQueueStore.getInstance(this).getSavedPlayingQueue();
        ArrayList<Song> restoredOriginalQueue = MusicPlaybackQueueStore.getInstance(this).getSavedOriginalPlayingQueue();
        int restoredPosition = PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_POSITION, -1);
        int restoredPositionInTrack = PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_POSITION_IN_TRACK, -1);

        if (restoredQueue.size() > 0 && restoredQueue.size() == restoredOriginalQueue.size() && restoredPosition != -1) {
            this.originalPlayingQueue = restoredOriginalQueue;
            this.playingQueue = restoredQueue;

            setPosition(restoredPosition);
            openCurrent();
            prepareNext();

            if (restoredPositionInTrack > 0) seek(restoredPositionInTrack);

            notHandledMetaChangedForCurrentTrack = true;
            sendChangeIntent(META_CHANGED);
            sendChangeIntent(QUEUE_CHANGED);
            updateWidgets();
        }
    }

    public void addSong(int position, Song song) {
        playingQueue.add(position, song);
        originalPlayingQueue.add(position, song);
        notifyChange(QUEUE_CHANGED);
    }

    public void addSong(Song song) {
        playingQueue.add(song);
        originalPlayingQueue.add(song);
        notifyChange(QUEUE_CHANGED);
    }

    public void addSongs(List<Song> songs) {
        playingQueue.addAll(songs);
        originalPlayingQueue.addAll(songs);
        notifyChange(QUEUE_CHANGED);
    }

    public void removeSong(int position) {
        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            playingQueue.remove(position);
            originalPlayingQueue.remove(position);
        } else {
            originalPlayingQueue.remove(playingQueue.remove(position));
        }
        notifyChange(QUEUE_CHANGED);
    }

    public void removeSong(@NonNull Song song) {
        for (int i = 0; i < playingQueue.size(); i++) {
            if (playingQueue.get(i).id == song.id) playingQueue.remove(i);
        }
        for (int i = 0; i < originalPlayingQueue.size(); i++) {
            if (originalPlayingQueue.get(i).id == song.id) originalPlayingQueue.remove(i);
        }
        notifyChange(QUEUE_CHANGED);
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
        notifyChange(QUEUE_CHANGED);
    }

    public void playSongAt(final int position) {
        // handle this on the handlers thread to avoid blocking the ui thread
        playerHandler.removeMessages(PLAY_SONG);
        playerHandler.obtainMessage(PLAY_SONG, position, 0).sendToTarget();
    }

    private void playSongAtImpl(int position) {
        if (openTrackAndPrepareNextAt(position)) {
            play();
        } else {
            Toast.makeText(this, getResources().getString(R.string.unplayable_file), Toast.LENGTH_SHORT).show();
        }
    }

    public void pause() {
        pausedByTransientLossOfFocus = false;
        if (player.isPlaying()) {
            player.pause();
            notifyChange(PLAY_STATE_CHANGED);
        }
    }

    public void play() {
        synchronized (this) {
            if (requestFocus()) {
                if (!player.isPlaying()) {
                    if (!player.isInitialized()) {
                        playSongAt(getPosition());
                    } else {
                        registerReceiversAndRemoteControlClient();
                        player.start();
                        if (notHandledMetaChangedForCurrentTrack) {
                            handleChange(META_CHANGED);
                            notHandledMetaChangedForCurrentTrack = false;
                        }
                        notifyChange(PLAY_STATE_CHANGED);
                    }
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.audio_focus_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void playPreviousSong(boolean force) {
        playSongAt(getPreviousPosition(force));
    }

    public void back(boolean force) {
        if (getSongProgressMillis() > 2000) {
            seek(0);
        } else {
            playPreviousSong(force);
        }
    }

    public int getPreviousPosition(boolean force) {
        int newPosition = getPosition() - 1;
        switch (repeatMode) {
            case REPEAT_MODE_ALL:
                if (newPosition < 0) {
                    newPosition = getPlayingQueue().size() - 1;
                }
                break;
            case REPEAT_MODE_THIS:
                if (force) {
                    if (newPosition < 0) {
                        newPosition = getPlayingQueue().size() - 1;
                    }
                } else {
                    newPosition = getPosition();
                }
                break;
            default:
            case REPEAT_MODE_NONE:
                if (newPosition < 0) {
                    newPosition = 0;
                }
                break;
        }
        return newPosition;
    }

    public int getSongProgressMillis() {
        return player.position();
    }

    public int getSongDurationMillis() {
        return player.duration();
    }

    public int seek(int millis) {
        int newPosition = player.seek(millis);
        savePositionInTrack();
        return newPosition;
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
                .putInt(SAVED_SHUFFLE_MODE, shuffleMode)
                .apply();
        switch (shuffleMode) {
            case SHUFFLE_MODE_SHUFFLE:
                this.shuffleMode = shuffleMode;
                ShuffleHelper.makeShuffleList(this.getPlayingQueue(), getPosition());
                setPosition(0);
                break;
            case SHUFFLE_MODE_NONE:
                this.shuffleMode = shuffleMode;
                int currentSongId = getCurrentSong().id;
                playingQueue = new ArrayList<>(originalPlayingQueue);
                int newPosition = 0;
                for (Song song : getPlayingQueue()) {
                    if (song.id == currentSongId) {
                        newPosition = getPlayingQueue().indexOf(song);
                    }
                }
                setPosition(newPosition);
                break;
        }
        notifyChange(SHUFFLE_MODE_CHANGED);
        notifyChange(QUEUE_CHANGED);
    }

    private void notifyChange(@NonNull final String what) {
        sendChangeIntent(what);
        handleChange(what);
    }

    private void sendChangeIntent(@NonNull final String what) {
        final Intent internalIntent = new Intent(what);

        final Song currentSong = getCurrentSong();
        if (currentSong.id != -1) {
            internalIntent.putExtra("id", currentSong.id);
            internalIntent.putExtra("artist", currentSong.artistName);
            internalIntent.putExtra("album", currentSong.albumName);
            internalIntent.putExtra("track", currentSong.title);
            internalIntent.putExtra("duration", currentSong.duration);
        }
        internalIntent.putExtra("playing", isPlaying());
        sendStickyBroadcast(internalIntent);

        // to let other apps know whats playing. i.E. last.fm (scrobbling) or musixmatch
        final Intent publicMusicIntent = new Intent(internalIntent);
        publicMusicIntent.setAction(what.replace(PHONOGRAPH_PACKAGE_NAME, MUSIC_PACKAGE_NAME));
        sendStickyBroadcast(publicMusicIntent);
    }

    private void handleChange(@NonNull final String what) {
        switch (what) {
            case PLAY_STATE_CHANGED:
                final boolean isPlaying = isPlaying();
                playingNotificationHelper.updatePlayState(isPlaying);
                WidgetMedium.updateWidgetsPlayState(this, isPlaying);
                //noinspection deprecation
                remoteControlClient.setPlaybackState(isPlaying ? RemoteControlClient.PLAYSTATE_PLAYING : RemoteControlClient.PLAYSTATE_PAUSED);
                if (!isPlaying && getSongProgressMillis() > 0) {
                    savePositionInTrack();
                }
                break;
            case META_CHANGED:
                updateNotification();
                updateWidgets();
                updateRemoteControlClient();
                savePosition();
                savePositionInTrack();
                final Song currentSong = getCurrentSong();
                recentlyPlayedStore.addSongId(currentSong.id);
                songPlayCountStore.bumpSongCount(currentSong.id);
                break;
            case QUEUE_CHANGED:
                saveState();
                prepareNext();
                break;
        }
    }

    public int getAudioSessionId() {
        return player.getAudioSessionId();
    }

    public void releaseWakeLock() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    public void acquireWakeLock(long milli) {
        wakeLock.acquire(milli);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PreferenceUtil.GAPLESS_PLAYBACK:
                if (sharedPreferences.getBoolean(key, false)) {
                    prepareNext();
                } else {
                    player.setNextDataSource(null);
                }
                break;
            case PreferenceUtil.ALBUM_ART_ON_LOCKSCREEN:
                updateRemoteControlClientImpl(sharedPreferences.getBoolean(key, true));
                break;
            case PreferenceUtil.COLORED_NOTIFICATION:
                playingNotificationHelper.updateNotification(sharedPreferences.getBoolean(key, false));
                break;
        }
    }

    private static final class QueueSaveHandler extends Handler {
        @NonNull
        private final WeakReference<MusicService> mService;

        public QueueSaveHandler(final MusicService service, @NonNull final Looper looper) {
            super(looper);
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            final MusicService service = mService.get();
            switch (msg.what) {
                case SAVE_QUEUES:
                    service.saveQueuesImpl();
                    break;
            }
        }
    }

    private static final class MusicPlayerHandler extends Handler {
        @NonNull
        private final WeakReference<MusicService> mService;
        private float currentDuckVolume = 1.0f;

        public MusicPlayerHandler(final MusicService service, @NonNull final Looper looper) {
            super(looper);
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(@NonNull final Message msg) {
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
                    service.player.setVolume(currentDuckVolume);
                    break;

                case UNDUCK:
                    currentDuckVolume += .03f;
                    if (currentDuckVolume < 1.0f) {
                        sendEmptyMessageDelayed(UNDUCK, 10);
                    } else {
                        currentDuckVolume = 1.0f;
                    }
                    service.player.setVolume(currentDuckVolume);
                    break;

                case TRACK_WENT_TO_NEXT:
                    if (service.getRepeatMode() == REPEAT_MODE_NONE && service.isLastTrack()) {
                        service.pause();
                        service.seek(0);
                    } else {
                        service.setPosition(service.nextPosition);
                        service.prepareNextImpl();
                        service.notifyChange(META_CHANGED);
                    }
                    break;

                case TRACK_ENDED:
                    if (service.getRepeatMode() == REPEAT_MODE_NONE && service.isLastTrack()) {
                        service.notifyChange(PLAY_STATE_CHANGED);
                        service.seek(0);
                    } else {
                        service.playNextSong(false);
                    }
                    break;

                case RELEASE_WAKELOCK:
                    service.releaseWakeLock();
                    break;

                case PLAY_SONG:
                    service.playSongAtImpl(msg.arg1);
                    break;

                case PREPARE_NEXT:
                    service.prepareNextImpl();
                    break;

                case FOCUS_CHANGE:
                    switch (msg.arg1) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                            service.registerReceiversAndRemoteControlClient();
                            if (!service.isPlaying() && service.pausedByTransientLossOfFocus) {
                                service.play();
                                service.pausedByTransientLossOfFocus = false;
                            }
                            removeMessages(DUCK);
                            sendEmptyMessage(UNDUCK);
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS:
                            // Lost focus for an unbounded amount of time: stop playback and release media player
                            service.pause();
                            service.unregisterReceiversAndRemoteControlClient();
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            // Lost focus for a short time, but we have to stop
                            // playback. We don't release the media player because playback
                            // is likely to resume
                            boolean wasPlaying = service.isPlaying();
                            service.pause();
                            service.pausedByTransientLossOfFocus = wasPlaying;
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            // Lost focus for a short time, but it's ok to keep playing
                            // at an attenuated level
                            removeMessages(UNDUCK);
                            sendEmptyMessage(DUCK);
                            break;
                    }
                    break;
            }
        }
    }

    public class MusicBinder extends Binder {
        @NonNull
        public MusicService getService() {
            return MusicService.this;
        }
    }

    private class MediaStoreObserver extends ContentObserver implements Runnable {
        // milliseconds to delay before calling refresh to aggregate events
        private static final long REFRESH_DELAY = 500;
        private Handler mHandler;

        public MediaStoreObserver(Handler handler) {
            super(handler);
            mHandler = handler;
        }

        @Override
        public void onChange(boolean selfChange) {
            // if a change is detected, remove any scheduled callback
            // then post a new one. This is intended to prevent closely
            // spaced events from generating multiple refresh calls
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, REFRESH_DELAY);
        }

        @Override
        public void run() {
            // actually call refresh when the delayed callback fires
            // do not send a sticky broadcast here
            sendBroadcast(new Intent(MEDIA_STORE_CHANGED));
        }
    }
}
