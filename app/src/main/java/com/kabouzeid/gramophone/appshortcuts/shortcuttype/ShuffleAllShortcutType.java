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
public final class ShuffleAllShortcutType extends BaseShortcutType {
    public ShuffleAllShortcutType(Context context) {
        super(context);
    }

    public ShortcutInfo getShortcutInfo() {
        return new ShortcutInfo.Builder(mContext, ID_PREFIX + "shuffle_all")
                .setShortLabel(mContext.getString(R.string.app_shortcut_shuffle_all_short))
                .setLongLabel(mContext.getString(R.string.app_shortcut_shuffle_all_long))
                .setIcon(AppShortcutIconGenerator.generateThemedIcon(mContext, R.drawable.ic_app_shortcut_shuffle_all))
                .setIntent(getPlaySongsIntent(AppShortcutLauncherActivity.ShortcutType.SHUFFLE_ALL))
                .build();
    }
}
