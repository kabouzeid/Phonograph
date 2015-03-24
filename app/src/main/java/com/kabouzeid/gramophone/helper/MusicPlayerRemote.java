package com.kabouzeid.gramophone.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.model.MusicRemoteEvent;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.util.InternalStorageUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by karim on 29.11.14.
 */
public class MusicPlayerRemote {
    private static final String TAG = MusicPlayerRemote.class.getSimpleName();

    private static int position = -1;

    private static List<Song> playingQueue;
    private static List<Song> restoredOriginalQueue;

    private static Context context;
    private static MusicService musicService;
    private static Intent musicServiceIntent;

    private static ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.restorePreviousState(restoredOriginalQueue, playingQueue, position);
            postToBus(MusicRemoteEvent.SERVICE_CONNECTED);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            postToBus(MusicRemoteEvent.SERVICE_DISCONNECTED);
        }
    };

    public static void init(final Context context) {
        MusicPlayerRemote.context = context;
        playingQueue = new ArrayList<>();
        restoredOriginalQueue = new ArrayList<>();
        startAndBindService();
        restorePreviousState();
    }

    private static void startAndBindService() {
        if (musicServiceIntent == null) {
            musicServiceIntent = new Intent(context, MusicService.class);
            context.bindService(musicServiceIntent, musicConnection, Context.BIND_AUTO_CREATE);
            context.startService(musicServiceIntent);
        }
    }

    public static boolean playSongAt(final int position) {
        if (musicService != null) {
            musicService.playSongAt(position);
        }
        return false;
    }

    public static void pauseSong() {
        if (musicService != null) {
            musicService.pausePlaying();
        }
    }

    public static void playNextSong() {
        if (musicService != null) {
            musicService.playNextSong();
        }
    }

    public static void playPreviousSong() {
        if (musicService != null) {
            musicService.back();
        }
    }

    public static void back() {
        if (musicService != null) {
            musicService.back();
        }
    }

    public static boolean isPlaying() {
        if (musicService != null) {
            return musicService.isPlaying();
        }
        return false;
    }

    public static void resumePlaying() {
        if (musicService != null) {
            musicService.resumePlaying();
        }
    }

    public static long getCurrentSongId() {
        if (musicService != null) {
            return musicService.getCurrentSongId();
        }
        try {
            return playingQueue.get(position).id;
        } catch (Exception e) {
            return -1;
        }
    }

    public static void openQueue(final List<Song> playingQueue, final int startPosition, final boolean startPlaying) {
        MusicPlayerRemote.playingQueue = playingQueue;
        if (musicService != null) {
            musicService.openQueue(MusicPlayerRemote.playingQueue, startPosition, startPlaying);
        }
    }

    public static Song getCurrentSong() {
        final int position = getPosition();
        if (position != -1) {
            return getPlayingQueue().get(position);
        }
        return new Song();
    }

    public static int getPosition() {
        if (musicService != null) {
            position = musicService.getPosition();
        }
        return position;
    }

    private static void setPosition(int position) {
        MusicPlayerRemote.position = position;
        if (musicService != null) {
            musicService.setPosition(position);
        }
    }

    public static List<Song> getPlayingQueue() {
        if (musicService != null) {
            playingQueue = musicService.getPlayingQueue();
        }
        return playingQueue;
    }

    public static int getSongProgressMillis() {
        if (isPlayerPrepared()) {
            return musicService.getSongProgressMillis();
        }
        return -1;
    }

    public static boolean isPlayerPrepared() {
        if (musicService != null) {
            return musicService.isPlayerPrepared();
        }
        return false;
    }

    public static int getSongDurationMillis() {
        if (isPlayerPrepared()) {
            return musicService.getSongDurationMillis();
        }
        return -1;
    }

    public static void seekTo(int millis) {
        if (musicService != null) {
            musicService.seekTo(millis);
        }
    }

    public static int getRepeatMode() {
        if (musicService != null) {
            return musicService.getRepeatMode();
        }
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(AppKeys.SP_REPEAT_MODE, 0);
    }

    public static int getShuffleMode() {
        if (musicService != null) {
            return musicService.getShuffleMode();
        }
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(AppKeys.SP_SHUFFLE_MODE, 0);
    }

    public static boolean cycleRepeatMode() {
        if (musicService != null) {
            musicService.cycleRepeatMode();
            return true;
        }
        return false;
    }

    public static boolean toggleShuffleMode() {
        if (musicService != null) {
            musicService.toggleShuffle();
            return true;
        }
        return false;
    }

    public static boolean setShuffleMode(final int shuffleMode) {
        if (musicService != null) {
            musicService.setShuffleMode(shuffleMode);
            return true;
        } return false;
    }

    public static void forceSetShuffleMode(final Context context, final int shuffleMode){
        if (musicService != null) {
            musicService.setShuffleMode(shuffleMode);
        } else {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putInt(AppKeys.SP_SHUFFLE_MODE, shuffleMode)
                    .apply();
        }
    }

    public static void playNext(Song song) {
        if (musicService != null) {
            musicService.addSong(getPosition() + 1, song);
            Toast.makeText(musicService, musicService.getResources().getString(R.string.added_title_to_playing_queue), Toast.LENGTH_SHORT).show();
        }
    }

    public static void enqueue(Song song) {
        if (musicService != null) {
            musicService.addSong(song);
            Toast.makeText(musicService, musicService.getResources().getString(R.string.added_title_to_playing_queue), Toast.LENGTH_SHORT).show();
        }
    }

    public static void removeFromQueue(Song song) {
        if (musicService != null) {
            musicService.removeSong(song);
        }
    }

    public static void removeFromQueue(int position) {
        if (musicService != null) {
            musicService.removeSong(position);
        }
    }

    public static void moveSong(int from, int to) {
        if (musicService != null) {
            musicService.moveSong(from, to);
        }
    }

    private static void postToBus(int event) {
        MusicRemoteEvent musicRemoteEvent = new MusicRemoteEvent(event);
        App.bus.post(musicRemoteEvent);
    }

    @SuppressWarnings("unchecked")
    public static void restorePreviousState() {
        try {
            List restoredQueue = (ArrayList<Song>) InternalStorageUtil.readObject(context, AppKeys.IS_PLAYING_QUEUE);
            List restoredOriginalQueue = (ArrayList<Song>) InternalStorageUtil.readObject(context, AppKeys.IS_ORIGINAL_PLAYING_QUEUE);
            int restoredPosition = (int) InternalStorageUtil.readObject(context, AppKeys.IS_POSITION_IN_QUEUE);

            if (musicService != null) {
                musicService.restorePreviousState(restoredOriginalQueue, restoredQueue, restoredPosition);
            }

            playingQueue = restoredQueue;
            MusicPlayerRemote.restoredOriginalQueue = restoredOriginalQueue;
            position = restoredPosition;

            postToBus(MusicRemoteEvent.STATE_RESTORED);
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            Log.e(TAG, "error while restoring music service state", e);
            playingQueue = new ArrayList<>();
            position = -1;
        }
    }
}
