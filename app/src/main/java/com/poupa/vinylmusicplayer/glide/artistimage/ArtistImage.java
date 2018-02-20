package com.poupa.vinylmusicplayer.glide.artistimage;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistImage {
    public final String artistName;
    public final boolean skipOkHttpCache;

    public ArtistImage(String artistName, boolean skipOkHttpCache) {
        this.artistName = artistName;
        this.skipOkHttpCache = skipOkHttpCache;
    }
}
