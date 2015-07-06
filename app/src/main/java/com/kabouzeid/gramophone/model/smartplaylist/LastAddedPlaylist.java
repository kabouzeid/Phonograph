package com.kabouzeid.gramophone.model.smartplaylist;

import android.content.Context;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.loader.LastAddedLoader;
import com.kabouzeid.gramophone.model.DataBaseChangedEvent;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PreferenceUtils;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class LastAddedPlaylist extends AbsSmartPlaylist {

    public LastAddedPlaylist(Context context) {
        super(context.getString(R.string.last_added), R.drawable.ic_queue_white_24dp);
    }

    @Override
    public ArrayList<Song> getSongs(Context context) {
        return LastAddedLoader.getLastAddedSongs(context);
    }

    @Override
    public void clear(Context context) {
        PreferenceUtils.getInstance(context).setLastAddedCutoffTimestamp(System.currentTimeMillis());
        App.bus.post(new DataBaseChangedEvent(DataBaseChangedEvent.PLAYLISTS_CHANGED));
    }
}
