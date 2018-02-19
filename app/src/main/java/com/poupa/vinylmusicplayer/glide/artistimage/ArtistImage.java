package com.poupa.vinylmusicplayer.glide.artistimage;

import android.support.annotation.NonNull;

import com.bumptech.glide.load.Key;

import java.security.MessageDigest;

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
