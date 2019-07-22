/*
 * Copyright (C) 2007 The Android Open Source Project Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

// Modified for Phonograph by Karim Abou Zeid (kabouzeid).

package com.kabouzeid.gramophone.service;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;

import androidx.core.content.ContextCompat;

import com.kabouzeid.gramophone.BuildConfig;

/**
 * Used to control headset playback.
 * Single press: pause/resume
 * Double press: next track
 * Triple press: previous track
 */
public class MediaButtonIntentReceiver extends BroadcastReceiver {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    public static final String TAG = MediaButtonIntentReceiver.class.getSimpleName();

    private static final int MSG_HEADSET_DOUBLE_CLICK_TIMEOUT = 2;

    private static final int DOUBLE_CLICK = 400;

    private static WakeLock mWakeLock = null;
    private static int mClickCounter = 0;
    private static long mLastClickTime = 0;

    @SuppressLint("HandlerLeak") // false alarm, handler is already static
    private static Handler mHandler = new Handler() {

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case MSG_HEADSET_DOUBLE_CLICK_TIMEOUT:
                    final int clickCount = msg.arg1;
                    final String command;

                    if (DEBUG) Log.v(TAG, "Handling headset click, count = " + clickCount);
                    switch (clickCount) {
                        case 1:
                            command = MusicService.ACTION_TOGGLE_PAUSE;
                            break;
                        case 2:
                            command = MusicService.ACTION_SKIP;
                            break;
                        case 3:
                            command = MusicService.ACTION_REWIND;
                            break;
                        default:
                            command = null;
                            break;
                    }

                    if (command != null) {
                        final Context context = (Context) msg.obj;
                        startService(context, command);
                    }
                    break;
            }
            releaseWakeLockIfHandlerIdle();
        }
    };

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (DEBUG) Log.v(TAG, "Received intent: " + intent);
        if (handleIntent(context, intent) && isOrderedBroadcast()) {
            abortBroadcast();
        }
    }

    public static boolean handleIntent(final Context context, final Intent intent) {
        final String intentAction = intent.getAction();
        if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            final KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event == null) {
                return false;
            }

            final int keycode = event.getKeyCode();
            final int action = event.getAction();
            final long eventTime = event.getEventTime() != 0 ?
                    event.getEventTime() : System.currentTimeMillis();
            // Fallback to system time if event time was not available.

            String command = null;
            switch (keycode) {
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    command = MusicService.ACTION_STOP;
                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    command = MusicService.ACTION_TOGGLE_PAUSE;
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
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    command = MusicService.ACTION_PLAY;
                    break;
            }
            if (command != null) {
                if (action == KeyEvent.ACTION_DOWN) {
                    if (event.getRepeatCount() == 0) {
                        // Only consider the first event in a sequence, not the repeat events,
                        // so that we don't trigger in cases where the first event went to
                        // a different app (e.g. when the user ends a phone call by
                        // long pressing the headset button)

                        // The service may or may not be running, but we need to send it
                        // a command.
                        if (keycode == KeyEvent.KEYCODE_HEADSETHOOK || keycode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                            if (eventTime - mLastClickTime >= DOUBLE_CLICK) {
                                mClickCounter = 0;
                            }

                            mClickCounter++;
                            if (DEBUG) Log.v(TAG, "Got headset click, count = " + mClickCounter);
                            mHandler.removeMessages(MSG_HEADSET_DOUBLE_CLICK_TIMEOUT);

                            Message msg = mHandler.obtainMessage(
                                    MSG_HEADSET_DOUBLE_CLICK_TIMEOUT, mClickCounter, 0, context);

                            long delay = mClickCounter < 3 ? DOUBLE_CLICK : 0;
                            if (mClickCounter >= 3) {
                                mClickCounter = 0;
                            }
                            mLastClickTime = eventTime;
                            acquireWakeLockAndSendMessage(context, msg, delay);
                        } else {
                            startService(context, command);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void startService(Context context, String command) {
        final Intent intent = new Intent(context, MusicService.class);
        intent.setAction(command);
        try {
            // IMPORTANT NOTE: (kind of a hack)
            // on Android O and above the following crashes when the app is not running
            // there is no good way to check whether the app is running so we catch the exception
            // we do not always want to use startForegroundService() because then one gets an ANR
            // if no notification is displayed via startForeground()
            // according to Play analytics this happens a lot, I suppose for example if command = PAUSE
            context.startService(intent);
        } catch (IllegalStateException ignored) {
            ContextCompat.startForegroundService(context, intent);
        }
    }

    private static void acquireWakeLockAndSendMessage(Context context, Message msg, long delay) {
        if (mWakeLock == null) {
            Context appContext = context.getApplicationContext();
            PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Phonograph headset button");
            mWakeLock.setReferenceCounted(false);
        }
        if (DEBUG) Log.v(TAG, "Acquiring wake lock and sending " + msg.what);
        // Make sure we don't indefinitely hold the wake lock under any circumstances
        mWakeLock.acquire(10000);

        mHandler.sendMessageDelayed(msg, delay);
    }

    private static void releaseWakeLockIfHandlerIdle() {
        if (mHandler.hasMessages(MSG_HEADSET_DOUBLE_CLICK_TIMEOUT)) {
            if (DEBUG) Log.v(TAG, "Handler still has messages pending, not releasing wake lock");
            return;
        }

        if (mWakeLock != null) {
            if (DEBUG) Log.v(TAG, "Releasing wake lock");
            mWakeLock.release();
            mWakeLock = null;
        }
    }
}
