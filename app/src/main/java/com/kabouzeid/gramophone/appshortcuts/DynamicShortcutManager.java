package com.kabouzeid.gramophone.appshortcuts;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;

import com.kabouzeid.gramophone.appshortcuts.shortcuttype.LastAddedShortcutType;
import com.kabouzeid.gramophone.appshortcuts.shortcuttype.ShuffleAllShortcutType;
import com.kabouzeid.gramophone.appshortcuts.shortcuttype.TopTracksShortcutType;

import java.util.Arrays;
import java.util.List;

/**
 * @author Adrian Campos
 */

@TargetApi(Build.VERSION_CODES.N_MR1)
public class DynamicShortcutManager {

    private Context context;
    private ShortcutManager shortcutManager;

    public DynamicShortcutManager(Context context) {
        this.context = context;
        shortcutManager = this.context.getSystemService(ShortcutManager.class);
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

    public void updateDynamicShortcuts() {
        shortcutManager.updateShortcuts(getDefaultShortcuts());
    }

    public List<ShortcutInfo> getDefaultShortcuts() {
        return (Arrays.asList(
                new ShuffleAllShortcutType(context).getShortcutInfo(),
                new TopTracksShortcutType(context).getShortcutInfo(),
                new LastAddedShortcutType(context).getShortcutInfo()
        ));
    }

    public static void reportShortcutUsed(Context context, String shortcutId){
        context.getSystemService(ShortcutManager.class).reportShortcutUsed(shortcutId);
    }
}
