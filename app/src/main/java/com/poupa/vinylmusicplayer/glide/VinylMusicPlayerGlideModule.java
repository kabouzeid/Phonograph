package com.poupa.vinylmusicplayer.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.module.GlideModule;
import com.poupa.vinylmusicplayer.glide.artistimage.ArtistImage;
import com.poupa.vinylmusicplayer.glide.artistimage.ArtistImageLoader;
import com.poupa.vinylmusicplayer.glide.audiocover.AudioFileCover;
import com.poupa.vinylmusicplayer.glide.audiocover.AudioFileCoverLoader;

import java.io.InputStream;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class VinylMusicPlayerGlideModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {

    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        glide.register(AudioFileCover.class, InputStream.class, new AudioFileCoverLoader.Factory());
        glide.register(ArtistImage.class, InputStream.class, new ArtistImageLoader.Factory(context));
    }
}
