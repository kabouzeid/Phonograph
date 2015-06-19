package com.kabouzeid.gramophone.service;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.PreferenceUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;

public final class MultiPlayer implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {
    public static final String TAG = MultiPlayer.class.getSimpleName();

    private final WeakReference<MusicService> mService;

    private MediaPlayer mCurrentMediaPlayer = new MediaPlayer();

    private MediaPlayer mNextMediaPlayer;

    private Handler mHandler;

    private boolean mIsInitialized = false;

    /**
     * Constructor of <code>MultiPlayer</code>
     */
    public MultiPlayer(final MusicService service) {
        mService = new WeakReference<>(service);
        mCurrentMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
    }

    /**
     * @param path The path of the file, or the http/rtsp URL of the stream
     *             you want to play
     */
    public void setDataSource(final String path) {
        mIsInitialized = false;
        mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path);
        if (mIsInitialized) {
            setNextDataSource(null);
        }
    }

    /**
     * @param player The {@link MediaPlayer} to use
     * @param path   The path of the file, or the http/rtsp URL of the stream
     *               you want to play
     * @return True if the <code>player</code> has been prepared and is
     * ready to play, false otherwise
     */
    private boolean setDataSourceImpl(final MediaPlayer player, final String path) {
        try {
            player.reset();
            player.setOnPreparedListener(null);
            if (path.startsWith("content://")) {
                player.setDataSource(mService.get(), Uri.parse(path));
            } else {
                player.setDataSource(path);
            }
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.prepare();
        } catch (final IOException e) {
            return false;
        } catch (final IllegalArgumentException e) {
            return false;
        }
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        final Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, mService.get().getPackageName());
        intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
        mService.get().sendBroadcast(intent);
        return true;
    }

    /**
     * Set the MediaPlayer to start when this MediaPlayer finishes playback.
     *
     * @param path The path of the file, or the http/rtsp URL of the stream
     *             you want to play
     */
    public void setNextDataSource(final String path) {
        try {
            mCurrentMediaPlayer.setNextMediaPlayer(null);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "Next media player is current one, continuing");
        } catch (IllegalStateException e) {
            Log.e(TAG, "Media player not initialized!");
            return;
        }
        if (mNextMediaPlayer != null) {
            mNextMediaPlayer.release();
            mNextMediaPlayer = null;
        }
        if (path == null) {
            return;
        }
        if (PreferenceUtils.getInstance(mService.get()).gaplessPlayback()) {
            mNextMediaPlayer = new MediaPlayer();
            mNextMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
            mNextMediaPlayer.setAudioSessionId(getAudioSessionId());
            if (setDataSourceImpl(mNextMediaPlayer, path)) {
                mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer);
            } else {
                if (mNextMediaPlayer != null) {
                    mNextMediaPlayer.release();
                    mNextMediaPlayer = null;
                }
            }
        }
    }

    /**
     * Sets the handler
     *
     * @param handler The handler to use
     */
    public void setHandler(final Handler handler) {
        mHandler = handler;
    }

    /**
     * @return True if the player is ready to go, false otherwise
     */
    public boolean isInitialized() {
        return mIsInitialized;
    }

    /**
     * Starts or resumes playback.
     */
    public void start() {
        mCurrentMediaPlayer.start();
    }

    /**
     * Resets the MediaPlayer to its uninitialized state.
     */
    public void stop() {
        mCurrentMediaPlayer.reset();
        mIsInitialized = false;
    }

    /**
     * Releases resources associated with this MediaPlayer object.
     */
    public void release() {
        stop();
        mCurrentMediaPlayer.release();
    }

    /**
     * Pauses playback. Call start() to resume.
     */
    public void pause() {
        mCurrentMediaPlayer.pause();
    }

    /**
     * Checks whether the MultiPlayer is playing.
     */
    public boolean isPlaying() {
        return mIsInitialized && mCurrentMediaPlayer.isPlaying();
    }

    /**
     * Gets the duration of the file.
     *
     * @return The duration in milliseconds
     */
    public int duration() {
        return mCurrentMediaPlayer.getDuration();
    }

    /**
     * Gets the current playback position.
     *
     * @return The current position in milliseconds
     */
    public int position() {
        return mCurrentMediaPlayer.getCurrentPosition();
    }

    /**
     * Gets the current playback position.
     *
     * @param whereto The offset in milliseconds from the start to seek to
     * @return The offset in milliseconds from the start to seek to
     */
    public long seek(final long whereto) {
        mCurrentMediaPlayer.seekTo((int) whereto);
        return whereto;
    }

    /**
     * Sets the volume on this player.
     *
     * @param vol Left and right volume scalar
     */
    public void setVolume(final float vol) {
        mCurrentMediaPlayer.setVolume(vol, vol);
    }

    /**
     * Sets the audio session ID.
     *
     * @param sessionId The audio session ID
     */
    public void setAudioSessionId(final int sessionId) {
        mCurrentMediaPlayer.setAudioSessionId(sessionId);
    }

    /**
     * Returns the audio session ID.
     *
     * @return The current audio session ID.
     */
    public int getAudioSessionId() {
        return mCurrentMediaPlayer.getAudioSessionId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onError(final MediaPlayer mp, final int what, final int extra) {
        Toast.makeText(mService.get().getApplicationContext(), mService.get().getResources().getString(R.string.unplayable_file), Toast.LENGTH_SHORT).show();
        mService.get().playNextSong(true);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCompletion(final MediaPlayer mp) {
        if (mp == mCurrentMediaPlayer && mNextMediaPlayer != null) {
            mIsInitialized = false;
            mCurrentMediaPlayer.release();
            mCurrentMediaPlayer = mNextMediaPlayer;
            mIsInitialized = true;
            mNextMediaPlayer = null;
            mHandler.sendEmptyMessage(MusicService.TRACK_WENT_TO_NEXT);
        } else {
            mService.get().acquireWakeLock(30000);
            mHandler.sendEmptyMessage(MusicService.TRACK_ENDED);
            mHandler.sendEmptyMessage(MusicService.RELEASE_WAKELOCK);
        }
    }
}