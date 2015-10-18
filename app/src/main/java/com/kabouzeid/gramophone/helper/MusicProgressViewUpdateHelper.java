package com.kabouzeid.gramophone.helper;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class MusicProgressViewUpdateHelper extends Handler {
    private static final int CMD_REFRESH_PROGRESS_VIEWS = 1;

    private Callback callback;

    public void start() {
        queueNextRefresh(1);
    }

    public void stop() {
        removeMessages(CMD_REFRESH_PROGRESS_VIEWS);
    }

    public MusicProgressViewUpdateHelper(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        if (msg.what == CMD_REFRESH_PROGRESS_VIEWS) {
            queueNextRefresh(refreshProgressViews());
        }
    }

    private long refreshProgressViews() {
        final int progressMillis = MusicPlayerRemote.getSongProgressMillis();
        final int totalMillis = MusicPlayerRemote.getSongDurationMillis();

        callback.onUpdateProgressViews(progressMillis, totalMillis);

        if (!MusicPlayerRemote.isPlaying()) {
            return 500;
        }

        // calculate the number of milliseconds until the next full second,
        // so
        // the counter can be updated at just the right time
        final long remainingMillis = 1000 - progressMillis % 1000;
        if (remainingMillis < 20) {
            return 20;
        }

        return remainingMillis;
    }

    private void queueNextRefresh(final long delay) {
        final Message message = obtainMessage(CMD_REFRESH_PROGRESS_VIEWS);
        removeMessages(CMD_REFRESH_PROGRESS_VIEWS);
        sendMessageDelayed(message, delay);
    }

    public interface Callback {
        void onUpdateProgressViews(int progress, int total);
    }
}
