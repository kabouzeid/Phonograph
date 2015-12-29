package com.kabouzeid.gramophone.ui.activities.base;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.KeyEvent;
import android.view.View;

import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsBaseActivity extends AbsThemeActivity implements KabViewsDisableAble {
    public static final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 0;

    private boolean areViewsEnabled;

    private boolean hasExternalStoragePermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        hasExternalStoragePermission = hasExternalStoragePermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableViews();

        if (!hasExternalStoragePermission()) {
            requestPermissions();
        } else if (didPermissionsChanged()) {
            // the handler is necessary to avoid "java.lang.RuntimeException: Performing pause of activity that is not resumed"
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    recreate();
                }
            }, 200);
        }
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_UP) {
            showOverflowMenu();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    protected void showOverflowMenu() {

    }

    /**
     * Should be overwritten and re enable all {@link android.view.View} to ensure they are accessible again
     * <p/>
     * This is necessary because of a bug with the shared element transition
     */
    @Override
    public void enableViews() {
        areViewsEnabled = true;
    }

    /**
     * Should be overwritten and disable all views that start a new activity on click to prevent opening an activity multiple times
     * <p/>
     * This is necessary because of a bug with the shared element transition
     */
    @Override
    public void disableViews() {
        areViewsEnabled = false;
    }

    @Override
    public boolean areViewsEnabled() {
        return areViewsEnabled;
    }

    private boolean didPermissionsChanged() {
        return hasExternalStoragePermission != hasExternalStoragePermission();
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE_PERMISSION);
        }
    }

    @SuppressLint("NewApi")
    private boolean hasExternalStoragePermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE_PERMISSION) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    recreate();
                    return;
                }
            }
            Snackbar.make(getWindow().getDecorView(), R.string.permission_to_access_external_storage_denied, Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.action_settings), onGoToPermissionSettingsClickListener)
                    .setActionTextColor(ThemeSingleton.get().positiveColor)
                    .show();
        }
    }

    private View.OnClickListener onGoToPermissionSettingsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //TODO
        }
    };
}
