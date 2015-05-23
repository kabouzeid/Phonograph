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
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.appwidget.MusicPlayerWidget;
import com.kabouzeid.gramophone.helper.PlayingNotificationHelper;
import com.kabouzeid.gramophone.helper.ShuffleHelper;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.model.MusicRemoteEvent;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.InternalStorageUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {
    public static final String ACTION_TOGGLE_PLAYBACK = "com.kabouzeid.gramophone.action.TOGGLE_PLAYBACK";
    public static final String ACTION_PLAY = "com.kabouzeid.gramophone.action.PLAY";
    public static final String ACTION_RESUME = "com.kabouzeid.gramophone.action.RESUME";
    public static final String ACTION_PAUSE = "com.kabouzeid.gramophone.action.PAUSE";
    public static final String ACTION_STOP = "com.kabouzeid.gramophone.action.STOP";
    public static final String ACTION_SKIP = "com.kabouzeid.gramophone.action.SKIP";
    public static final String ACTION_REWIND = "com.kabouzeid.gramophone.action.REWIND";
    public static final String ACTION_QUIT = "com.kabouzeid.gramophone.action.QUIT";
    public static final String META_CHANGED = "com.android.music.metachanged";
    public static final String PLAYSTATE_CHANGED = "com.android.music.playstatechanged";
    public static final int SHUFFLE_MODE_NONE = 0;
    public static final int SHUFFLE_MODE_SHUFFLE = 1;
    public static final int REPEAT_MODE_NONE = 0;
    public static final int REPEAT_MODE_ALL = 1;
    public static final int REPEAT_MODE_THIS = 2;
    private static final String TAG = MusicService.class.getSimpleName();
    private final IBinder musicBind = new MusicBinder();
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().compareTo(AudioManager.ACTION_AUDIO_BECOMING_NOISY) == 0) {
                wasPlayingBeforeFocusLoss = false;
                pausePlaying();
            }
        }
    };
    private MediaPlayer player;
    private ArrayList<Song> playingQueue;
    private ArrayList<Song> originalPlayingQueue;
    private int currentSongId = -1;
    private int position = -1;
    private int shuffleMode;
    private int repeatMode;
    private boolean isPlayerPrepared;
    private boolean wasPlayingBeforeFocusLoss;
    private boolean thingsRegistered;
    private boolean saveQueuesAgain;
    private boolean isSavingQueues;
    private PlayingNotificationHelper playingNotificationHelper;
    private AudioManager audioManager;
    private RemoteControlClient remoteControlClient;
    private PowerManager.WakeLock wakeLock;
    private String currentAlbumArtUri;

    public MusicService() {
    }

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

        registerEverything();
    }

    private void registerEverything() {
        if (!thingsRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
            registerReceiver(receiver, intentFilter);
            getAudioManager().registerMediaButtonEventReceiver(new ComponentName(getApplicationContext(), MediaButtonIntentReceiver.class));
            initRemoteControlClient();
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
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(new ComponentName(getApplicationContext(), MediaButtonIntentReceiver.class));
        PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent, 0);
        remoteControlClient = new RemoteControlClient(mediaPendingIntent);
        remoteControlClient.setTransportControlFlags(
                RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                        RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                        RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                        RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS);
        getAudioManager().registerRemoteControlClient(remoteControlClient);
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
                            pausePlaying();
                        } else {
                            resumePlaying();
                        }
                        break;
                    case ACTION_PAUSE:
                        pausePlaying();
                        break;
                    case ACTION_PLAY:
                        playSong();
                        break;
                    case ACTION_RESUME:
                        resumePlaying();
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
            unregisterReceiver(receiver);
            getAudioManager().unregisterRemoteControlClient(remoteControlClient);
            getAudioManager().unregisterMediaButtonEventReceiver(new ComponentName(getApplicationContext(), MediaButtonIntentReceiver.class));
            getAudioManager().abandonAudioFocus(this);
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
        isPlayerPrepared = false;
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        playingNotificationHelper.updatePlayState(isPlaying());
        MusicPlayerWidget.updateWidgetsPlayState(this, isPlaying());
        remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
        notifyOnMusicRemoteEventListeners(MusicRemoteEvent.STOP);
        notifyChange(PLAYSTATE_CHANGED);
    }

    public boolean isPlaying() {
        return player != null && isPlayerPrepared && player.isPlaying();
    }

    private void notifyOnMusicRemoteEventListeners(int event) {
        MusicRemoteEvent musicRemoteEvent = new MusicRemoteEvent(event);
        App.bus.post(musicRemoteEvent);
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
        notifyOnMusicRemoteEventListeners(MusicRemoteEvent.SONG_COMPLETED);
        if (isLastTrack() && getRepeatMode() == REPEAT_MODE_NONE) {
            notifyOnMusicRemoteEventListeners(MusicRemoteEvent.QUEUE_COMPLETED);
            playingNotificationHelper.updatePlayState(isPlaying());
            MusicPlayerWidget.updateWidgetsPlayState(this, isPlaying());
            remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
            notifyOnMusicRemoteEventListeners(MusicRemoteEvent.STOP);
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
                notifyOnMusicRemoteEventListeners(MusicRemoteEvent.NEXT);
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

                    notifyOnMusicRemoteEventListeners(MusicRemoteEvent.STOP);
                    playingNotificationHelper.updatePlayState(false);
                    MusicPlayerWidget.updateWidgetsPlayState(this, false);
                    remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
                    notifyChange(PLAYSTATE_CHANGED);

                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.unplayable_file), Toast.LENGTH_SHORT).show();
                    return;
                }
                currentSongId = playingQueue.get(getPosition()).id;
                updateNotification();
                updateWidgets();
                updateRemoteControlClient();
                notifyChange(META_CHANGED);
            } else {
                playingNotificationHelper.updatePlayState(false);
                MusicPlayerWidget.updateWidgetsPlayState(this, false);
                notifyChange(PLAYSTATE_CHANGED);
            }
        }
        notifyOnMusicRemoteEventListeners(MusicRemoteEvent.TRACK_CHANGED);
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
        return (getAudioManager().requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

    private void updateRemoteControlClient() {
        final Song song = playingQueue.get(getPosition());
        remoteControlClient
                .editMetadata(false)
                .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, song.artistName)
                .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, song.title)
                .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, song.duration)
                .apply();
        currentAlbumArtUri = MusicUtil.getAlbumArtUri(song.albumId).toString();
        ImageLoader.getInstance().displayImage(currentAlbumArtUri, new NonViewAware(new ImageSize(-1, -1), ViewScaleType.CROP), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (currentAlbumArtUri.equals(imageUri))
                    // copy() prevents the original bitmap in the memory cache from being recycled by the remote control client
                    updateRemoteControlClientBitmap(loadedImage.copy(loadedImage.getConfig(), true));
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (currentAlbumArtUri.equals(imageUri))
                    updateRemoteControlClientBitmap(null);
            }
        });
    }

    private void updateRemoteControlClientBitmap(final Bitmap albumArt) {
        remoteControlClient
                .editMetadata(false)
                .putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, albumArt)
                .apply();
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
                notifyOnMusicRemoteEventListeners(MusicRemoteEvent.REPEAT_MODE_CHANGED);
                break;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        isPlayerPrepared = false;
        player.reset();
        player = null;
        notifyOnMusicRemoteEventListeners(MusicRemoteEvent.STOP);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        player.start();
        isPlayerPrepared = true;
        openAudioEffectSession();
        playingNotificationHelper.updatePlayState(isPlaying());
        MusicPlayerWidget.updateWidgetsPlayState(this, isPlaying());
        remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        notifyOnMusicRemoteEventListeners(MusicRemoteEvent.PLAY);
        notifyChange(PLAYSTATE_CHANGED);
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

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                registerEverything();
                setUpMediaPlayerIfNeeded();
                player.setVolume(1.0f, 1.0f);
                if (wasPlayingBeforeFocusLoss) {
                    resumePlaying();
                }
                updateRemoteControlClient();
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                //TODO maybe also release player (stopPlaying()) but then the current position in the song is 0 again
                wasPlayingBeforeFocusLoss = false;
                pausePlaying();
                unregisterEverything();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                wasPlayingBeforeFocusLoss = isPlaying();
                pausePlaying();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (!isPlayerPrepared()) {
                    setUpMediaPlayerIfNeeded();
                }
                player.setVolume(0.2f, 0.2f);
                break;
        }
    }

    public void playSongAt(final int position) {
        if (position < getPlayingQueue().size() && position >= 0) {
            setPosition(position);
            playSong();
        } else {
            Log.e(TAG, "No song in queue at given index!");
        }
    }

    public void pausePlaying() {
        if (isPlaying()) {
            player.pause();
            playingNotificationHelper.updatePlayState(isPlaying());
            MusicPlayerWidget.updateWidgetsPlayState(this, isPlaying());
            remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
            notifyOnMusicRemoteEventListeners(MusicRemoteEvent.PAUSE);
            notifyChange(PLAYSTATE_CHANGED);
        }
    }

    public void resumePlaying() {
        if (!isPlaying()) {
            if (requestFocus()) {
                if (isPlayerPrepared()) {
                    player.start();
                    playingNotificationHelper.updatePlayState(isPlaying());
                    MusicPlayerWidget.updateWidgetsPlayState(this, isPlaying());
                    remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
                    notifyOnMusicRemoteEventListeners(MusicRemoteEvent.RESUME);
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
            notifyOnMusicRemoteEventListeners(MusicRemoteEvent.PREV);
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
                notifyOnMusicRemoteEventListeners(MusicRemoteEvent.SHUFFLE_MODE_CHANGED);
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
                notifyOnMusicRemoteEventListeners(MusicRemoteEvent.SHUFFLE_MODE_CHANGED);
                break;
        }
    }

    private void notifyChange(final String what) {
        //to let other apps know whats playing. i.E. last.fm (scrobbling) or musixmatch
        final Intent intent = new Intent(what);
        final int position = getPosition();
        if (position >= 0 && !playingQueue.isEmpty()) {
            final Song currentSong = playingQueue.get(position);
            intent.putExtra("id", currentSong.id);
            intent.putExtra("artist", currentSong.artistName);
            intent.putExtra("album", currentSong.albumName);
            intent.putExtra("track", currentSong.title);
        }
        intent.putExtra("playing", isPlaying());
        sendStickyBroadcast(intent);
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
}
