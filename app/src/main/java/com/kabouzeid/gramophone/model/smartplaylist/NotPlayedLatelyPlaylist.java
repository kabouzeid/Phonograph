package com.kabouzeid.gramophone.model.smartplaylist;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.kabouzeid.gramophone.loader.TopAndRecentlyPlayedTracksLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.provider.HistoryStore;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.util.ArrayList;

/**
 * @author SC (soncaokim)
 */
public class NotPlayedLatelyPlaylist extends AbsSmartPlaylist {

    public NotPlayedLatelyPlaylist(@NonNull Context context) {
        super(context.getString(R.string.not_played_lately), R.drawable.ic_library_music_white_24dp);
    }

    @NonNull
    @Override
    public String getInfoString(@NonNull Context context) {
        String baseInfo = super.getInfoString(context);
    	String cutoff = PreferenceUtil.getInstance(context).getRecentlyPlayedCutoffText(context);

        if (baseInfo.isEmpty()) {return cutoff;}
        return cutoff + INFO_STRING_SEPARATOR + baseInfo;
    }

    @NonNull
    @Override
    public ArrayList<Song> getSongs(@NonNull Context context) {
        return TopAndRecentlyPlayedTracksLoader.getNotPlayedLatelyTracks(context);
    }

    @Override
    public void clear(@NonNull Context context) {
        HistoryStore.getInstance(context).clear();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    protected NotPlayedLatelyPlaylist(Parcel in) {
        super(in);
    }

    public static final Creator<NotPlayedLatelyPlaylist> CREATOR = new Creator<NotPlayedLatelyPlaylist>() {
        public NotPlayedLatelyPlaylist createFromParcel(Parcel source) {
            return new NotPlayedLatelyPlaylist(source);
        }

        public NotPlayedLatelyPlaylist[] newArray(int size) {
            return new NotPlayedLatelyPlaylist[size];
        }
    };
}
