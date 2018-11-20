package com.kabouzeid.gramophone.model;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.kabouzeid.gramophone.R;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public abstract class AbsCustomPlaylist extends Playlist {
    public static final String INFO_STRING_SEPARATOR = "  •  ";

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
        String baseInfo = super.getInfoString(context);

        int songCount = getSongs(context).size(); // TODO Performance pernalty?
        String songCountText = (songCount == 0) ?
            context.getString(R.string.no_songs) :
            String.valueOf(songCount) + " " + context.getString(R.string.songs);

        if (baseInfo.isEmpty()) {return songCountText;}
        return songCountText + INFO_STRING_SEPARATOR + baseInfo;
    }
}
