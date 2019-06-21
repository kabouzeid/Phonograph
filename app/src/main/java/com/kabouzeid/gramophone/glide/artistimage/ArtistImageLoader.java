package com.kabouzeid.gramophone.glide.artistimage;

import android.content.Context;

import java.io.InputStream;

import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.stream.StreamModelLoader;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public class ArtistImageLoader implements StreamModelLoader<ArtistImage> {

    @Override
    public DataFetcher<InputStream> getResourceFetcher(final ArtistImage model, int width, int height) {

        return new ArtistImageFetcher(model);
    }

    public static class Factory implements ModelLoaderFactory<ArtistImage, InputStream> {

        @Override
        public ModelLoader<ArtistImage, InputStream> build(Context context, GenericLoaderFactory factories) {

            return new ArtistImageLoader();
        }

        @Override
        public void teardown() {

        }
    }
}

