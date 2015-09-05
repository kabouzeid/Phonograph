package com.kabouzeid.gramophone.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
public class AboutDialog extends LeakDetectDialogFragment {

    private static String getCurrentVersionName(@NonNull final Context context) {
        String versionName;
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Unknown";
        }
        return versionName;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity())
                .title(getActivity().getResources().getString(R.string.app_name) + " " + getCurrentVersionName(getActivity()))
                .iconRes(R.drawable.ic_launcher)
                .content(TextUtils.concat(getActivity().getResources().getText(R.string.credits_1),
                                " ",
                                getActivity().getResources().getText(R.string.karim_abou_zeid),
                                ".\n",
                                getActivity().getResources().getText(R.string.karim_abou_zeid_links),
                                "\n\n",
                                getActivity().getResources().getText(R.string.special_thanks_to),
                                " ",
                                getActivity().getResources().getText(R.string.aidan_follestad),
                                ".\n\n",
                                getActivity().getResources().getText(R.string.credits_3),
                                " ",
                                getActivity().getResources().getText(R.string.cookicons),
                                ".\n\n",
                                getActivity().getResources().getText(R.string.play_store_illustration_by),
                                " ",
                                getActivity().getResources().getText(R.string.maarten_corpel),
                                "."
                        )
                )
                .positiveText(android.R.string.ok)
                .neutralText(R.string.changelog)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        super.onNeutral(dialog);
                        ChangelogDialog.create().show(getActivity().getSupportFragmentManager(), "CHANGE_LOG_DIALOG");
                    }
                })
                .build();
    }
}
