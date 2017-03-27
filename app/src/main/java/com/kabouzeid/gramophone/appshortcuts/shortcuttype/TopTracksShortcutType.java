package com.kabouzeid.gramophone.appshortcuts.shortcuttype;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ShortcutInfo;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.appshortcuts.AppShortcutIconGenerator;
import com.kabouzeid.gramophone.appshortcuts.AppShortcutLauncherActivity;

/**
 * @author Adrian Campos
 */

@TargetApi(25)
public final class TopTracksShortcutType extends BaseShortcutType {
    public TopTracksShortcutType(Context context) {
        super(context);
    }

    public ShortcutInfo getShortcutInfo() {
        return new ShortcutInfo.Builder(context, getId())
                .setShortLabel(context.getString(R.string.app_shortcut_top_tracks_short))
                .setLongLabel(context.getString(R.string.app_shortcut_top_tracks_long))
                .setIcon(AppShortcutIconGenerator.generateThemedIcon(context, R.drawable.ic_app_shortcut_top_tracks))
                .setIntent(getPlaySongsIntent(AppShortcutLauncherActivity.SHORTCUT_TYPE_TOP_TRACKS))
                .build();
    }

    public static String getId() {
        return ID_PREFIX + "top_tracks";
    }
}
