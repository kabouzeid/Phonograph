package com.kabouzeid.gramophone.appshortcuts.shortcuttype;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Icon;

import com.kabouzeid.gramophone.R;
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
                .setShortLabel(mContext.getString(R.string.appshortcut_shuffleall_short))
                .setLongLabel(mContext.getString(R.string.appshortcut_shuffleall_long))
                .setIcon(Icon.createWithResource(mContext, R.drawable.ic_shuffle_white_24dp))
                .setIntent(getPlaySongsIntent(AppShortcutLauncherActivity.ShortcutType.SHUFFLE_ALL))
                .build();
    }
}
