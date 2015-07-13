package com.kabouzeid.gramophone.model.smartplaylist;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.loader.TopAndRecentlyPlayedTracksLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.provider.RecentlyPlayedStore;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class RecentlyPlayedPlaylist extends AbsSmartPlaylist {

    public RecentlyPlayedPlaylist(@NonNull Context context) {
        super(context.getString(R.string.recently_played), R.drawable.ic_access_time_white_24dp);
    }

    @NonNull
    @Override
    public ArrayList<Song> getSongs(@NonNull Context context) {
        return TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(context);
    }

    @Override
    public void clear(@NonNull Context context) {
        RecentlyPlayedStore.getInstance(context).clear();
    }
}
