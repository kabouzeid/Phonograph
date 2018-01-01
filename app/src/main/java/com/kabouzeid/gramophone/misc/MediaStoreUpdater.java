package com.kabouzeid.gramophone.misc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;


public class MediaStoreUpdater {
    public static void confirmUpdate(Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.scan_whole_file_system)
                .content(R.string.scan_warning)
                .positiveText(R.string.scan_action)
                .negativeText(android.R.string.cancel)
                .onPositive((dialog, action) -> update(context))
                .show();
    }

    public static void update(Context context) {
        class ScanReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(intent.getAction())) {
                    context.unregisterReceiver(this);
                    Toast.makeText(context, R.string.scan_index_updated, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, R.string.scan_updating_index, Toast.LENGTH_SHORT).show();
                }
            }
        }
        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("file");
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        ScanReceiver receiver = new ScanReceiver();
        context.registerReceiver(receiver, filter);

        Bundle args = new Bundle();
        args.putString("volume", "external");
        Intent scannerIntent = new Intent("android.media.IMediaScannerService")
                .setClassName("com.android.providers.media","com.android.providers.media.MediaScannerService")
                .putExtras(args);
        context.startService(scannerIntent);
    }
}
