package com.kabouzeid.gramophone.glide.artistimage;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistImageRequest {
    public final String artistName;
    public final boolean skipOkHttpCache;

    public ArtistImageRequest(String artistName, boolean skipOkHttpCache) {
        this.artistName = artistName;
        this.skipOkHttpCache = skipOkHttpCache;
    }
}
