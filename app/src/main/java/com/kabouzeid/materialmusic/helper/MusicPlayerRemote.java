package com.kabouzeid.materialmusic.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.kabouzeid.materialmusic.App;
import com.kabouzeid.materialmusic.interfaces.OnMusicRemoteEventListener;
import com.kabouzeid.materialmusic.misc.AppKeys;
import com.kabouzeid.materialmusic.model.MusicRemoteEvent;
import com.kabouzeid.materialmusic.model.Song;
import com.kabouzeid.materialmusic.service.MusicService;
import com.kabouzeid.materialmusic.util.InternalStorageUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by karim on 29.11.14.
 */
public class MusicPlayerRemote implements OnMusicRemoteEventListener {
    private static final String TAG = MusicPlayerRemote.class.getSimpleName();

    private App app;

    private int position = -1;

    private List<Song> playingQueue;
    private List<OnMusicRemoteEventListener> onMusicRemoteEventListeners;

    private MusicService musicService;
    private Intent musicServiceIntent;
    private boolean musicBound = false;

    public MusicPlayerRemote(Context context) {
        app = (App) context.getApplicationContext();
        playingQueue = new ArrayList<>();
        onMusicRemoteEventListeners = new ArrayList<>();
        startAndBindService();
    }

    private void startAndBindService() {
        if (musicServiceIntent == null) {
            musicServiceIntent = new Intent(app, MusicService.class);
            app.bindService(musicServiceIntent, musicConnection, Context.BIND_AUTO_CREATE);
            app.startService(musicServiceIntent);
        }
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.setPosition(position);
            musicBound = true;
            musicService.addOnMusicRemoteEventListener(MusicPlayerRemote.this);
            setPlayingQueue(playingQueue);
            notifyOnMusicRemoteEventListeners(MusicRemoteEvent.SERVICE_CONNECTED);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
            notifyOnMusicRemoteEventListeners(MusicRemoteEvent.SERVICE_DISCONNECTED);
        }
    };

    public boolean playSongAt(int position) {
        if (musicBound) {
            if (position < getPlayingQueue().size() && position >= 0) {
                this.position = position;
                musicService.setPosition(position);
                musicService.playSong();
                return true;
            } else {
                Log.e(TAG, "No song in queue at given index!");
            }
        }
        return false;
    }

    public void pauseSong() {
        if (musicBound) {
            musicService.pausePlaying();
        }
    }

    public void playNextSong() {
        if (musicBound) {
            musicService.playNextSong();
        }
    }

    public void playPreviousSong() {
        if (musicBound) {
            musicService.back();
        }
    }

    public void back() {
        if (musicBound) {
            musicService.back();
        }
    }

    public boolean isPlaying() {
        if (musicBound) {
            return musicService.isPlaying();
        }
        return false;
    }

    public void resumePlaying() {
        if (musicBound) {
            musicService.resumePlaying();
        }
    }

    public long getCurrentSongId() {
        if (musicBound) {
            return musicService.getCurrentSongId();
        }
        try {
            return playingQueue.get(position).id;
        } catch (Exception e) {
            return -1;
        }
    }

    public int getPosition() {
        if (musicBound) {
            position = musicService.getPosition();
        }
        return position;
    }

    public void setPlayingQueue(List<Song> songs) {
        playingQueue = songs;
        if (musicBound) {
            musicService.setPlayingQueue(playingQueue);
        }
    }

    public List<Song> getPlayingQueue() {
        if (musicBound) {
            playingQueue = musicService.getPlayingQueue();
        }
        return playingQueue;
    }

    public Song getCurrentSong() {
        final int position = getPosition();
        if (position != -1) {
            return getPlayingQueue().get(position);
        }
        return new Song();
    }

    public int getSongProgressMillis() {
        if (isPlayerPrepared()) {
            return musicService.getSongProgressMillis();
        }
        return -1;
    }

    public int getSongDurationMillis() {
        if (isPlayerPrepared()) {
            return musicService.getSongDurationMillis();
        }
        return -1;
    }

    public boolean isMusicBound() {
        return musicBound;
    }

    public void seekTo(int millis) {
        if (musicBound) {
            musicService.seekTo(millis);
        }
    }

    public boolean isPlayerPrepared() {
        if (musicBound) {
            return musicService.isPlayerPrepared();
        }
        return false;
    }

    public void notifyPlayingQueueChanged() {
        final long currentSongId = getCurrentSongId();
    }

    public int getRepeatMode() {
        if (musicBound) {
            return musicService.getRepeatMode();
        }
        return app.getDefaultSharedPreferences().getInt(AppKeys.SP_REPEAT_MODE, 0);
    }

    public int getShuffleMode() {
        if (musicBound) {
            return musicService.getShuffleMode();
        }
        return app.getDefaultSharedPreferences().getInt(AppKeys.SP_SHUFFLE_MODE, 0);
    }

    public boolean cycleRepeatMode() {
        if (musicBound) {
            musicService.cycleRepeatMode();
            return true;
        }
        return false;
    }

    public boolean toggleShuffleMode() {
        if (musicBound) {
            musicService.toggleShuffle();
            return true;
        }
        return false;
    }

    @Override
    public void onMusicRemoteEvent(MusicRemoteEvent event) {
        notifyOnMusicRemoteEventListeners(event.getAction());
    }

    public void addOnMusicRemoteEventListener(OnMusicRemoteEventListener onMusicRemoteEventListener) {
        onMusicRemoteEventListeners.add(onMusicRemoteEventListener);
    }

    public void removeOnMusicRemoteEventListener(OnMusicRemoteEventListener onMusicRemoteEventListener) {
        onMusicRemoteEventListeners.remove(onMusicRemoteEventListener);
    }

    public void removeAllOnMusicRemoteEventListeners() {
        onMusicRemoteEventListeners.clear();
    }

    private void notifyOnMusicRemoteEventListeners(int event) {
        MusicRemoteEvent musicRemoteEvent = new MusicRemoteEvent(event);
        for (OnMusicRemoteEventListener listener : onMusicRemoteEventListeners) {
            listener.onMusicRemoteEvent(musicRemoteEvent);
        }
    }

    @SuppressWarnings("unchecked")
    public void restorePreviousState() {
        try {
            List restoredQueue = (ArrayList<Song>) InternalStorageUtil.readObject(app, AppKeys.IS_PLAYING_QUEUE);
            int restoredPosition = (int) InternalStorageUtil.readObject(app, AppKeys.IS_POSITION_IN_QUEUE);
            setPlayingQueue(restoredQueue);
            position = restoredPosition;
            if (musicBound) {
                musicService.setPosition(restoredPosition);
            }
            notifyOnMusicRemoteEventListeners(MusicRemoteEvent.STATE_RESTORED);
            Log.i(TAG, "restored last state");
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            Log.e(TAG, "error while restoring music service state", e);
            playingQueue = new ArrayList<>();
            position = -1;
        }
    }
}
