package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistAlbumLoader {

    @NonNull
    public static ArrayList<Album> getArtistAlbumList(@NonNull final Context context, final int artistId) {
        return AlbumLoader.getAlbums(makeArtistAlbumCursor(context, artistId));
    }

    public static Cursor makeArtistAlbumCursor(@NonNull final Context context, final int artistId) {
        try {
            return AlbumLoader.makeAlbumCursor(context,
                    MediaStore.Audio.Artists.Albums.getContentUri("external", artistId),
                    null,
                    null,
                    PreferenceUtil.getInstance(context).getArtistAlbumSortOrder()
            );
        } catch (SecurityException e) {
            return null;
        }
    }
}
