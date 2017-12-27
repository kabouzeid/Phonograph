package com.poupa.vinylmusicplayer.misc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.widget.Toast;

import com.poupa.vinylmusicplayer.R;

import java.lang.ref.WeakReference;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class UpdateToastMediaScannerCompletionListener implements MediaScannerConnection.OnScanCompletedListener {
    int scanned = 0;
    int failed = 0;

    private final String[] toBeScanned;

    private final String scannedFiles;
    private final String couldNotScanFiles;

    private final WeakReference<Toast> toastWeakReference;
    private final WeakReference<Activity> activityWeakReference;

    @SuppressLint("ShowToast")
    public UpdateToastMediaScannerCompletionListener(Activity activity, String[] toBeScanned) {
        this.toBeScanned = toBeScanned;
        scannedFiles = activity.getString(R.string.scanned_files);
        couldNotScanFiles = activity.getString(R.string.could_not_scan_files);
        toastWeakReference = new WeakReference<>(Toast.makeText(activity, "", Toast.LENGTH_SHORT));
        activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    public void onScanCompleted(final String path, final Uri uri) {
        Activity activity = activityWeakReference.get();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                Toast toast = toastWeakReference.get();
                if (toast != null) {
                    if (uri == null) {
                        failed++;
                    } else {
                        scanned++;
                    }
                    String text = " " + String.format(scannedFiles, scanned, toBeScanned.length) + (failed > 0 ? " " + String.format(couldNotScanFiles, failed) : "");
                    toast.setText(text);
                    toast.show();
                }
            });
        }
    }
}
