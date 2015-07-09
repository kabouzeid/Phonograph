package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.util.PreferenceUtils;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistAlbumLoader {

    public static ArrayList<Album> getArtistAlbumList(final Context context, final int artistId) {
        return AlbumLoader.getAlbums(makeArtistAlbumCursor(context, artistId));
    }

    public static Cursor makeArtistAlbumCursor(final Context context, final int artistId) {
        return AlbumLoader.makeAlbumCursor(context,
                MediaStore.Audio.Artists.Albums.getContentUri("external", artistId),
                null,
                null,
                PreferenceUtils.getInstance(context).getArtistAlbumSortOrder()
        );
    }
}
