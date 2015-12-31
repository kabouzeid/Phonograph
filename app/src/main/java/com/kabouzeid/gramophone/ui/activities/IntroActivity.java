package com.kabouzeid.gramophone.ui.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class IntroActivity extends AppIntro {
    @Override
    public void init(Bundle savedInstanceState) {
        int color = ContextCompat.getColor(this, R.color.blue_grey_700);
        setStatusBarColor(ColorUtil.shiftColorDown(color));
        setTaskColor(color);
        setSkipText(getString(R.string.action_skip).toUpperCase());
        setDoneText(getString(R.string.action_done).toUpperCase());
        setGrantText(getString(R.string.action_grant).toUpperCase());

        addSlide(AppIntroFragment.newInstance(getString(R.string.app_name), getString(R.string.welcome_to_phonograph), R.drawable.icon_web, color));
        if (!hasExternalStoragePermission()) {
            addSlide(AppIntroFragment.newInstance(getString(R.string.label_storage), getString(R.string.storage_permission_explaination), R.drawable.ic_folder_web, color));
            askForPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        addSlide(AppIntroFragment.newInstance(getString(R.string.label_playing_queue), getString(R.string.open_playing_queue_instruction), R.drawable.tutorial_queue_swipe_up, color));
        addSlide(AppIntroFragment.newInstance(getString(R.string.label_playing_queue), getString(R.string.rearrange_playing_queue_instruction), R.drawable.tutorial_rearrange_queue, color));
    }

    private void setTaskColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(new ActivityManager.TaskDescription(
                    null,
                    null,
                    ColorUtil.getOpaqueColor(color)));
        }
    }

    private boolean hasExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            onSkipPressed();
            return;
        }

        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.press_back_again_to_exit_intro, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public void onSkipPressed() {
        super.onSkipPressed();
        PreferenceUtil.getInstance(this).setIntroShown();
    }

    @Override
    public void onDonePressed() {
        super.onDonePressed();
        PreferenceUtil.getInstance(this).setIntroShown();
    }
}
