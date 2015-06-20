package com.kabouzeid.gramophone.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.audiofx.AudioEffect;
import android.os.IBinder;
import android.widget.Toast;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.service.MusicService;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class MusicPlayerRemote {

    public static final String TAG = MusicPlayerRemote.class.getSimpleName();

    public static final String SERVICE_BOUND = "com.kabouzeid.gramophone.SERVICE_BOUND";

    private static MusicService musicService;

    private static final ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.sendBroadcast(new Intent(SERVICE_BOUND));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
        }
    };

    public static void startAndBindService(final Context context) {
        Intent musicServiceIntent = new Intent(context, MusicService.class);
        context.bindService(musicServiceIntent, musicConnection, Context.BIND_AUTO_CREATE);
        context.startService(musicServiceIntent);
    }

    public static boolean isServiceConnected() {
        return musicService != null;
    }

    public static void playSongAt(final int position) {
        if (musicService != null) {
            musicService.playSongAt(position);
        }
    }

    public static void pauseSong() {
        if (musicService != null) {
            musicService.pause(false);
        }
    }

    public static void playNextSong() {
        if (musicService != null) {
            musicService.playNextSong(true);
        }
    }

    public static void playPreviousSong() {
        if (musicService != null) {
            musicService.back(true);
        }
    }

    public static void back() {
        if (musicService != null) {
            musicService.back(true);
        }
    }

    public static boolean isPlaying() {
        return musicService != null && musicService.isPlayingAndNotFadingDown();
    }

    public static void resumePlaying() {
        if (musicService != null) {
            musicService.play(false);
        }
    }

    public static void openQueue(final ArrayList<Song> playingQueue, final int startPosition, final boolean startPlaying) {
        if (musicService != null) {
            musicService.openAndPlayQueue(playingQueue, startPosition, startPlaying);
        }
    }

    public static Song getCurrentSong() {
        if (musicService != null) {
            return musicService.getCurrentSong();
        }
        return new Song();
    }

    public static int getPosition() {
        if (musicService != null) {
            return musicService.getPosition();
        }
        return -1;
    }

    public static ArrayList<Song> getPlayingQueue() {
        if (musicService != null) {
            return musicService.getPlayingQueue();
        }
        return new ArrayList<>();
    }

    public static int getSongProgressMillis() {
        if (musicService != null) {
            return musicService.getSongProgressMillis();
        }
        return -1;
    }

    public static int getSongDurationMillis() {
        if (musicService != null) {
            return musicService.getSongDurationMillis();
        }
        return -1;
    }

    public static void seekTo(int millis) {
        if (musicService != null) {
            musicService.seek(millis);
        }
    }

    public static int getRepeatMode() {
        if (musicService != null) {
            return musicService.getRepeatMode();
        }
        return MusicService.REPEAT_MODE_NONE;
    }

    public static int getShuffleMode() {
        if (musicService != null) {
            return musicService.getShuffleMode();
        }
        return MusicService.SHUFFLE_MODE_NONE;
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
        }
        return false;
    }

    public static boolean shuffleAllSongs(final Context context) {
        if (musicService != null) {
            ArrayList<Song> songs = SongLoader.getAllSongs(context);
            if (!songs.isEmpty()) {
                MusicPlayerRemote.openQueue(songs, new Random().nextInt(songs.size()), true);
                setShuffleMode(MusicService.SHUFFLE_MODE_SHUFFLE);
            }
            return true;
        }
        return false;
    }

    public static boolean playNext(Song song) {
        if (musicService != null) {
            musicService.addSong(getPosition() + 1, song);
            Toast.makeText(musicService, musicService.getResources().getString(R.string.added_title_to_playing_queue), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public static boolean enqueue(Song song) {
        if (musicService != null) {
            musicService.addSong(song);
            Toast.makeText(musicService, musicService.getResources().getString(R.string.added_title_to_playing_queue), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public static boolean enqueue(ArrayList<Song> songs) {
        if (musicService != null) {
            musicService.addSongs(songs);
            final String toast = songs.size() == 1 ? musicService.getResources().getString(R.string.added_title_to_playing_queue) : musicService.getResources().getString(R.string.added_x_titles_to_playing_queue, songs.size());
            Toast.makeText(musicService, toast, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public static boolean removeFromQueue(Song song) {
        if (musicService != null) {
            musicService.removeSong(song);
            return true;
        }
        return false;
    }

    public static boolean removeFromQueue(int position) {
        if (musicService != null) {
            musicService.removeSong(position);
            return true;
        }
        return false;
    }

    public static boolean moveSong(int from, int to) {
        if (musicService != null) {
            musicService.moveSong(from, to);
            return true;
        }
        return false;
    }

    public static int getAudioSessionId() {
        if (musicService != null) {
            return musicService.getAudioSessionId();
        }
        return AudioEffect.ERROR_NO_INIT;
    }

    public static void playFile(String path) {
        //TODO
    }
}
