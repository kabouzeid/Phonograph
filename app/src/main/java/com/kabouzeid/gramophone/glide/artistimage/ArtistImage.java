package com.kabouzeid.gramophone.glide.artistimage;

import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistImage {
    public final String artistName;
    public final boolean skipOkHttpCache;
    // filePath to get the image of the artist
    public final List<AlbumCover> albumCovers;

    public ArtistImage(final String artistName, final boolean skipOkHttpCache, final List<AlbumCover> albumCovers) {
        this.artistName = artistName;
        this.albumCovers = albumCovers;
        this.skipOkHttpCache = skipOkHttpCache;
    }
}
