package com.kabouzeid.gramophone.appshortcuts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.kabouzeid.gramophone.appshortcuts.shortcuttype.LastAddedShortcutType;
import com.kabouzeid.gramophone.appshortcuts.shortcuttype.ShuffleAllShortcutType;
import com.kabouzeid.gramophone.appshortcuts.shortcuttype.TopTracksShortcutType;
import com.kabouzeid.gramophone.loader.LastAddedLoader;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.loader.TopAndRecentlyPlayedTracksLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.MainActivity;

import java.util.ArrayList;

/**
 * @author Adrian Campos
 */

public class AppShortcutLauncherActivity extends Activity {

    public static final String KEY_SHORTCUT_TYPE = "com.kabouzeid.gramophone.appshortcuts.ShortcutType";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ShortcutType shortcutType = ShortcutType.NONE;

        //Set shortcutType from the intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            try {
                shortcutType = ShortcutType.valueOf(extras.getString(KEY_SHORTCUT_TYPE));
            } catch (IllegalArgumentException e) { //In the event we're somehow passed an invalid enum string, don't crash.
                e.printStackTrace();
                shortcutType = ShortcutType.NONE;
            }
        }

        //Perform the action found in the extras
        switch (shortcutType) {
            case SHUFFLE_ALL:
                launchMainActivityWithSongs(PlayMode.SHUFFLE,
                        SongLoader.getAllSongs(getApplicationContext()));
                DynamicShortcutManager.reportShortcutUsed(this, ShuffleAllShortcutType.getId());
                break;
            case TOP_TRACKS:
                launchMainActivityWithSongs(PlayMode.NORMAL,
                        TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(getApplicationContext()));
                DynamicShortcutManager.reportShortcutUsed(this, TopTracksShortcutType.getId());
                break;
            case LAST_ADDED:
                launchMainActivityWithSongs(PlayMode.NORMAL,
                        LastAddedLoader.getLastAddedSongs(getApplicationContext()));
                DynamicShortcutManager.reportShortcutUsed(this, LastAddedShortcutType.getId());
                break;
        }

        finish();
    }

    private void launchMainActivityWithSongs(PlayMode playMode, ArrayList<Song> songs) {
        //Create a new intent to launch MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        switch (playMode) {
            case NORMAL:
                intent.setAction(MainActivity.INTENT_ACTION_MEDIA_PLAY);
                break;
            case SHUFFLE:
                intent.setAction(MainActivity.INTENT_ACTION_MEDIA_PLAY_SHUFFLED);
                break;
        }


        //Create a bundle to store the songs to shuffle through songs
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(MainActivity.INTENT_EXTRA_SONGS, songs);

        //Put the bundle in the intent
        intent.putExtras(bundle);

        //If MainActivity's already running, don't launch another instance. Instead, bring it to the top and deliver the intent to onNewIntent()
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        //Finally, start MainActivity with those extras
        startActivity(intent);
    }

    private enum PlayMode {NORMAL, SHUFFLE}

    public enum ShortcutType {
        SHUFFLE_ALL, TOP_TRACKS, LAST_ADDED, NONE
    }
}
