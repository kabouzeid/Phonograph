package com.kabouzeid.gramophone.appshortcuts.shortcuttype;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.os.Bundle;

import com.kabouzeid.gramophone.appshortcuts.AppShortcutLauncherActivity;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.MainActivity;

import java.util.ArrayList;

/**
 * @author Adrian Campos
 */

@TargetApi(25)
public abstract class BaseShortcutType {

    static final String ID_PREFIX = "com.kabouzeid.gramophone.appshortcuts.id.";

    Context mContext;

    public BaseShortcutType(Context context) {
        mContext = context;
    }


    abstract ShortcutInfo getShortcutInfo();



    /**
     * Creates an Intent that will launch MainActivtiy and immediately play {@param songs} in either shuffle or normal mode
     *
     * @param shortcutType Describes the type of shortcut to create (ShuffleAll, TopTracks, custom playlist, etc.)
     * @return
     */
    Intent getPlaySongsIntent(AppShortcutLauncherActivity.ShortcutType shortcutType) {
        //Create a new intent to launch MainActivity
        Intent intent = new Intent(mContext, AppShortcutLauncherActivity.class);
        intent.setAction(Intent.ACTION_VIEW);

        //Create a bundle to store instructions for AppShortcutLauncherActivity
        Bundle b = new Bundle();
        b.putString(AppShortcutLauncherActivity.KEY_SHORTCUT_TYPE, shortcutType.toString());

        //Put bundle in intent
        intent.putExtras(b);

        return intent;
    }
}
