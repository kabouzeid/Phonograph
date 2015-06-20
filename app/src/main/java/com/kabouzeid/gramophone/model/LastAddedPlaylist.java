package com.kabouzeid.gramophone.model;

import android.content.Context;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.loader.LastAddedLoader;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class LastAddedPlaylist extends SmartPlaylist {

    public LastAddedPlaylist(Context context) {
        super(context.getString(R.string.last_added), R.drawable.ic_queue_white_24dp);
    }

    @Override
    public ArrayList<Song> getSongs(Context context) {
        return LastAddedLoader.getLastAddedSongs(context);
    }
}
