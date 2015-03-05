package com.kabouzeid.gramophone.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class MediaButtonIntentReceiver extends BroadcastReceiver {
    public static final String TAG = MediaButtonIntentReceiver.class.getSimpleName();

    private static final int DOUBLE_CLICK = 500;
    private static long mLastClickTime = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            Log.i(TAG, intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT).toString());
            final KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event == null)
                return;
            final int keycode = event.getKeyCode();
            final int action = event.getAction();
            final long eventTime = event.getEventTime();

            String command = null;
            switch (keycode) {
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    command = MusicService.ACTION_STOP;
                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    command = MusicService.ACTION_TOGGLE_PLAYBACK;
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    command = MusicService.ACTION_SKIP;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    command = MusicService.ACTION_REWIND;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    command = MusicService.ACTION_PAUSE;
                    break;
            }
            if (command != null) {
                if (action == KeyEvent.ACTION_DOWN) {
                    if (event.getRepeatCount() == 0) {
                        /**
                         * If another app received the broadcast first, this if statement will skip.
                         */
                        //TODO triple click to rewind
                        final Intent i = new Intent(context, MusicService.class);
                        if (keycode == KeyEvent.KEYCODE_HEADSETHOOK
                                && eventTime - mLastClickTime < DOUBLE_CLICK) {
                            i.setAction(MusicService.ACTION_SKIP);
                            mLastClickTime = 0;
                        } else {
                            i.setAction(command);
                            mLastClickTime = eventTime;
                        }
                        context.startService(i);
                    }
                }
                if (isOrderedBroadcast())
                    abortBroadcast();
            }
        }
    }
}
