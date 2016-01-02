package com.kabouzeid.gramophone.glide.artistimage;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistImageRequest {
    public final String artistName;
    public final boolean forceDownload;

    public ArtistImageRequest(String artistName, boolean forceDownload) {
        this.artistName = artistName;
        this.forceDownload = forceDownload;
    }
}
