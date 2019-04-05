package com.kabouzeid.gramophone.model;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.MusicUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public abstract class AbsCustomPlaylist extends Playlist {
    public AbsCustomPlaylist(int id, String name) {
        super(id, name);
    }

    public AbsCustomPlaylist() {
    }

    public AbsCustomPlaylist(Parcel in) {
        super(in);
    }

    @NonNull
    public abstract ArrayList<Song> getSongs(Context context);

    @NonNull
    @Override
    public String getInfoString(@NonNull Context context) {
        int songCount = getSongs(context).size();
        String songCountString = MusicUtil.getSongCountString(context, songCount);

        return MusicUtil.buildInfoString(
            songCountString,
            super.getInfoString(context)
        );
    }
}
