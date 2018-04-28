package com.kabouzeid.gramophone.misc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;


public class MediaStoreUpdater {
    public static void confirmUpdate(Context context) {
        if (isMediaScannerRunning(context)) {
            Toast.makeText(context, R.string.scan_already_updating, Toast.LENGTH_SHORT).show();
        } else {
            new MaterialDialog.Builder(context)
                    .title(R.string.scan_whole_file_system)
                    .content(R.string.scan_warning)
                    .positiveText(R.string.scan_action)
                    .negativeText(android.R.string.cancel)
                    .onPositive((dialog, action) -> update(context))
                    .show();
        }
    }

    public static void update(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("file");
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        context.registerReceiver(new ScanReceiver(), filter);

        Bundle args = new Bundle();
        args.putString("volume", "external");
        Intent scannerIntent = new Intent("android.media.IMediaScannerService")
                .setClassName("com.android.providers.media", "com.android.providers.media.MediaScannerService")
                .putExtras(args);
        context.startService(scannerIntent);
    }

    private static boolean isMediaScannerRunning(Context context) {
        try (Cursor query = context.getContentResolver().query(MediaStore.getMediaScannerUri(), new String[]{MediaStore.MEDIA_SCANNER_VOLUME}, null, null, null)) {
            if (query != null && query.moveToFirst()) {
                return query.getString(query.getColumnIndex(MediaStore.MEDIA_SCANNER_VOLUME)) != null;
            }
        }
        return false;
    }

    static class ScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(intent.getAction())) {
                context.unregisterReceiver(this);
                Toast.makeText(context, R.string.scan_index_updated, Toast.LENGTH_SHORT).show();
            } else if(Intent.ACTION_MEDIA_SCANNER_STARTED.equals(intent.getAction())) {
                Toast.makeText(context, R.string.scan_updating_index, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
