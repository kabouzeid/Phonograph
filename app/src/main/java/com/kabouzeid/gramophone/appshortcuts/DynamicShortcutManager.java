package com.kabouzeid.gramophone.appshortcuts;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Build;

import com.kabouzeid.gramophone.appshortcuts.shortcuttype.LastAddedShortcutType;
import com.kabouzeid.gramophone.appshortcuts.shortcuttype.ShuffleAllShortcutType;
import com.kabouzeid.gramophone.appshortcuts.shortcuttype.TopTracksShortcutType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Adrian Campos
 */

@TargetApi(Build.VERSION_CODES.N_MR1)
public class DynamicShortcutManager {

    Context mContext;
    ShortcutManager shortcutManager;

    public DynamicShortcutManager(Context context) {
        mContext = context;
        shortcutManager = mContext.getSystemService(ShortcutManager.class);
    }

    public static ShortcutInfo createShortcut(Context context, String id, String shortLabel, String longLabel, Icon icon, Intent intent) {
        return new ShortcutInfo.Builder(context, id)
                .setShortLabel(shortLabel)
                .setLongLabel(longLabel)
                .setIcon(icon)
                .setIntent(intent)
                .build();
    }

    public void initDynamicShortcuts() {
        if (shortcutManager.getDynamicShortcuts().size() == 0) {
            shortcutManager.setDynamicShortcuts(getDefaultShortcuts());
        }
    }

    public List<ShortcutInfo> getDefaultShortcuts() {
        return (Arrays.asList(
                new ShuffleAllShortcutType(mContext).getShortcutInfo(),
                new TopTracksShortcutType(mContext).getShortcutInfo(),
                new LastAddedShortcutType(mContext).getShortcutInfo()
        ));
    }

    public void tintShortcutIcons(ArrayList<ShortcutInfo> shortcutInfos, Color color) {
        for (ShortcutInfo shortcutInfo : shortcutInfos) {
            tintShortcutIcon(shortcutInfo, color);
        }
    }

    public void tintShortcutIcon(ShortcutInfo shortcutInfo, Color color) {
        //TODO Tint icons here
    }

}
