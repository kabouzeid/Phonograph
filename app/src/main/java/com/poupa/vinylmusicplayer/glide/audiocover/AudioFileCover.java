package com.poupa.vinylmusicplayer.glide.audiocover;

import android.support.annotation.NonNull;

import com.bumptech.glide.load.Key;

import java.security.MessageDigest;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AudioFileCover implements Key {
    public final String filePath;

    public AudioFileCover(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

    }
}
