package com.kabouzeid.gramophone.helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;

/**
 * Created by karim on 05.02.15.
 */
public class AboutDeveloperDialogHelper {
    public static final String TAG = AboutDeveloperDialogHelper.class.getSimpleName();

    public static MaterialDialog getDialog(final Context context) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(context.getResources().getString(R.string.app_name) + " " + getCurrentVersionName(context))
                .iconRes(R.drawable.ic_launcher)
                .content(context.getResources().getText(R.string.credits))
                .positiveText(context.getResources().getString(R.string.ok))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
                    }
                })
                .build();
        return dialog;
    }

    private static String getCurrentVersionName(final Context context) {
        String versionName = "";
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to get current app version number.", e);
        }
        return versionName;
    }
}
