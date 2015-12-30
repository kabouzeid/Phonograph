package com.kabouzeid.gramophone.ui.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.ColorUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class IntroActivity extends AppIntro {
    @Override
    public void init(Bundle savedInstanceState) {
        int color = ContextCompat.getColor(this, R.color.blue_grey_700);
        setStatusBarColor(ColorUtil.shiftColorDown(color));

        addSlide(AppIntroFragment.newInstance(getString(R.string.app_name), "Welcome to Phonograph, a beautiful and lightweight music player for Android. ", R.drawable.icon_web, color));
        if (!hasExternalStoragePermission()) {
            addSlide(AppIntroFragment.newInstance("Storage", "The storage permission is required for Phonograph to read your music library.", R.drawable.ic_folder_web, color));
            askForPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        addSlide(AppIntroFragment.newInstance(getString(R.string.label_current_playing_queue), "You can swipe the card in the now playing screen up to reveal to full playing queue.", R.drawable.tutorial_queue_swipe_up, color));
        addSlide(AppIntroFragment.newInstance(getString(R.string.label_current_playing_queue), "You can rearrange the playing queue by dragging a song from its track number.", R.drawable.tutorial_rearrange_queue, color));
    }

    private boolean hasExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
}
