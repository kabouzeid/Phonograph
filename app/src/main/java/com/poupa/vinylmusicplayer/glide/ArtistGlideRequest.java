package com.poupa.vinylmusicplayer.glide;

import android.support.annotation.NonNull;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.glide.artistimage.ArtistImage;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.util.CustomArtistImageUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistGlideRequest {

    private GlideRequest requestManager;
    private final Artist artist;
    private boolean forceDownload;

    public static ArtistGlideRequest from(@NonNull GlideRequest requestManager, Artist artist) {
        return new ArtistGlideRequest(requestManager, artist);
    }

    private ArtistGlideRequest(@NonNull GlideRequest requestManager, Artist artist) {
        this.requestManager = requestManager;
        this.artist = artist;
    }

    public ArtistGlideRequest forceDownload(boolean forceDownload) {
        this.forceDownload = forceDownload;
        return this;
    }

    public GlideRequest build() {
        boolean hasCustomImage = CustomArtistImageUtil.getInstance(App.getInstance()).hasCustomArtistImage(artist);
        if (!hasCustomImage) {
            requestManager = requestManager.load(new ArtistImage(artist.getName(), forceDownload));
        } else {
            requestManager = requestManager.load(CustomArtistImageUtil.getFile(artist));
        }

        //noinspection unchecked
        return requestManager.transition(VinylGlideExtension.getDefaultTransition())
                .artistOptions(artist);
    }
}
