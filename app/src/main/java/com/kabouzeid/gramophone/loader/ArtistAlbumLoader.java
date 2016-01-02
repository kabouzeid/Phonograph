package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.kabouzeid.gramophone.comparator.AlbumASCComparator;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistAlbumLoader {

    @NonNull
    public static ArrayList<Album> getAlbums(@NonNull final Context context, final int artistId) {
        ArrayList<Song> songs = SongLoader.getSongs(SongLoader.makeSongCursor(
                context,
                MediaStore.Audio.AudioColumns.ARTIST_ID + "=?",
                new String[]{String.valueOf(artistId)},
                PreferenceUtil.getInstance(context).getAlbumSongSortOrder()
        ));
        return AlbumLoader.splitIntoAlbums(songs, new AlbumASCComparator());
    }
}
