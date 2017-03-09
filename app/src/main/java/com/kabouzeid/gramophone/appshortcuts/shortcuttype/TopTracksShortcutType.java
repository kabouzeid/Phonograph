package com.kabouzeid.gramophone.appshortcuts.shortcuttype;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Icon;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.appshortcuts.AppShortcutLauncherActivity;
import com.kabouzeid.gramophone.loader.TopAndRecentlyPlayedTracksLoader;

/**
 * @author Adrian Campos
 */

@TargetApi(25)
public final class TopTracksShortcutType extends BaseShortcutType {
    public TopTracksShortcutType(Context context) {
        super(context);
    }
    
    public ShortcutInfo getShortcutInfo() {
        return new ShortcutInfo.Builder(mContext, ID_PREFIX + "top_tracks")
                .setShortLabel(mContext.getString(R.string.appshortcut_toptracks_short))
                .setLongLabel(mContext.getString(R.string.appshortcut_toptracks_long))
                .setIcon(Icon.createWithResource(mContext, R.drawable.ic_trending_up_white_24dp))
                .setIntent(getPlaySongsIntent(AppShortcutLauncherActivity.ShortcutType.TOP_TRACKS))
                .build();
    }
}
