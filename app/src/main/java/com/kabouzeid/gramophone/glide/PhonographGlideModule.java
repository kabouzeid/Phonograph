package com.kabouzeid.gramophone.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.module.GlideModule;
import com.kabouzeid.gramophone.glide.artistimage.ArtistImageLoader;
import com.kabouzeid.gramophone.glide.artistimage.ArtistImageRequest;
import com.kabouzeid.gramophone.glide.audiocover.SongCoverLoader;
import com.kabouzeid.gramophone.model.Song;

import java.io.InputStream;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PhonographGlideModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {

    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        glide.register(Song.class, InputStream.class, new SongCoverLoader.Factory());
        glide.register(ArtistImageRequest.class, InputStream.class, new ArtistImageLoader.Factory(context));
    }
}
