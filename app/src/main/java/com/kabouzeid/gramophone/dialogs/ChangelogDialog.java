package com.kabouzeid.gramophone.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ChangelogDialog extends DialogFragment {

    public static ChangelogDialog create() {
        return new ChangelogDialog();
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View customView;
        try {
            customView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_web_view, null);
        } catch (InflateException e) {
            e.printStackTrace();
            return new MaterialDialog.Builder(getActivity())
                    .title(android.R.string.dialog_alert_title)
                    .content("This device doesn't support web view, which is necessary to view the change log. It is missing a system component.")
                    .positiveText(android.R.string.ok)
                    .build();
        }
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.changelog)
                .customView(customView, false)
                .positiveText(android.R.string.ok)
                .showListener(dialog1 -> {
                    if (getActivity() != null)
                        setChangelogRead(getActivity());
                })
                .build();

        final WebView webView = customView.findViewById(R.id.web_view);
        try {
            // Load from phonograph-changelog.html in the assets folder
            StringBuilder buf = new StringBuilder();
            InputStream json = getActivity().getAssets().open("phonograph-changelog.html");
            BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null)
                buf.append(str);
            in.close();

            // Inject color values for WebView body background and links
            final String backgroundColor = colorToCSS(ATHUtil.resolveColor(getActivity(), R.attr.md_background_color, Color.parseColor(ThemeSingleton.get().darkTheme ? "#424242" : "#ffffff")));
            final String contentColor = colorToCSS(Color.parseColor(ThemeSingleton.get().darkTheme ? "#ffffff" : "#000000"));
            final String changeLog = buf.toString()
                    .replace("{style-placeholder}",
                            String.format("body { background-color: %s; color: %s; }", backgroundColor, contentColor))
                    .replace("{link-color}", colorToCSS(ThemeSingleton.get().positiveColor.getDefaultColor()))
                    .replace("{link-color-active}", colorToCSS(ColorUtil.lightenColor(ThemeSingleton.get().positiveColor.getDefaultColor())));
            webView.loadData(changeLog, "text/html", "UTF-8");
        } catch (Throwable e) {
            webView.loadData("<h1>Unable to load</h1><p>" + e.getLocalizedMessage() + "</p>", "text/html", "UTF-8");
        }
        return dialog;
    }

    public static void setChangelogRead(@NonNull Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int currentVersion = pInfo.versionCode;
            PreferenceUtil.getInstance(context).setLastChangeLogVersion(currentVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String colorToCSS(int color) {
        return String.format("rgb(%d, %d, %d)", Color.red(color), Color.green(color), Color.blue(color)); // on API 29, WebView doesn't load with hex colors
    }
}
