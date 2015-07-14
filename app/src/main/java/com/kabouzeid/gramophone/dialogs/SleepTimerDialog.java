package com.kabouzeid.gramophone.dialogs;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.ThemeSingleton;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.triggertrap.seekarc.SeekArc;

import java.lang.reflect.Field;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SleepTimerDialog extends DialogFragment {
    @InjectView(R.id.seek_arc)
    SeekArc seekArc;
    @InjectView(R.id.timer_display)
    TextView timerDisplay;

    private int seekArcProgress;
    private MaterialDialog materialDialog;
    private TimerUpdater timerUpdater;

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        timerUpdater.cancel();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        timerUpdater = new TimerUpdater();
        materialDialog = new MaterialDialog.Builder(getActivity())
                .title(getActivity().getResources().getString(R.string.action_sleep_timer))
                .positiveText(R.string.action_set)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (getActivity() == null) {
                            return;
                        }
                        final int min = seekArcProgress;
                        PreferenceUtil.getInstance(getActivity()).setLastSleepTimerValue(min);

                        PendingIntent pi = makeTimerPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT);

                        final long nextSleepTimerElapsedTime = SystemClock.elapsedRealtime() + min * 60 * 1000;
                        PreferenceUtil.getInstance(getActivity()).setNextSleepTimerElapsedRealtime(nextSleepTimerElapsedTime);
                        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextSleepTimerElapsedTime, pi);

                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.sleep_timer_set, min), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        if (getActivity() == null) {
                            return;
                        }
                        final PendingIntent previous = makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE);
                        if (previous != null) {
                            AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                            am.cancel(previous);
                            previous.cancel();
                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.sleep_timer_canceled), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        if (makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE) != null) {
                            timerUpdater.start();
                        }
                    }
                })
                .customView(R.layout.dialog_sleep_timer, false)
                .build();

        if (getActivity() == null || materialDialog.getCustomView() == null) {
            return materialDialog;
        }

        ButterKnife.inject(this, materialDialog.getCustomView());

        seekArc.post(new Runnable() {
            @Override
            public void run() {
                int width = seekArc.getWidth();
                int height = seekArc.getHeight();
                int small = Math.min(width, height);

                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(seekArc.getLayoutParams());
                layoutParams.height = small;
                seekArc.setLayoutParams(layoutParams);

                try {
                    Field f1 = SeekArc.class.getDeclaredField("mThumb");
                    f1.setAccessible(true);
                    Drawable thumb = (Drawable) f1.get(seekArc);
                    thumb.setColorFilter(ThemeSingleton.get().positiveColor, PorterDuff.Mode.SRC_IN);

                    Field f2 = SeekArc.class.getDeclaredField("mProgressPaint");
                    f2.setAccessible(true);
                    Paint progressPaint = (Paint) f2.get(seekArc);
                    progressPaint.setColor(ThemeSingleton.get().positiveColor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        seekArcProgress = PreferenceUtil.getInstance(getActivity()).getLastSleepTimerValue();
        updateTimeDisplayTime();
        seekArc.setProgress(seekArcProgress);

        seekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(@NonNull SeekArc seekArc, int i, boolean b) {
                if (i < 1) {
                    seekArc.setProgress(1);
                    return;
                }
                seekArcProgress = i;
                updateTimeDisplayTime();
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {

            }
        });

        return materialDialog;
    }

    private void updateTimeDisplayTime() {
        timerDisplay.setText(seekArcProgress + " min");
    }

    private PendingIntent makeTimerPendingIntent(int flag) {
        return PendingIntent.getService(getActivity(), 0, makeTimerIntent(), flag);
    }

    private Intent makeTimerIntent() {
        return new Intent(getActivity(), MusicService.class)
                .setAction(MusicService.ACTION_QUIT);
    }

    private class TimerUpdater extends CountDownTimer {
        public TimerUpdater() {
            super(PreferenceUtil.getInstance(getActivity()).getNextSleepTimerElapsedRealTime() - SystemClock.elapsedRealtime(), 1000);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            materialDialog.setActionButton(DialogAction.NEUTRAL, materialDialog.getContext().getString(R.string.cancel_current_timer) + " (" + MusicUtil.getReadableDurationString(millisUntilFinished) + ")");
        }

        @Override
        public void onFinish() {
            materialDialog.setActionButton(DialogAction.NEUTRAL, null);
        }
    }
}