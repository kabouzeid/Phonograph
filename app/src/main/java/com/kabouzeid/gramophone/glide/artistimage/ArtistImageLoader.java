package com.kabouzeid.gramophone.glide.artistimage;

import android.content.Context;

import com.bumptech.glide.integration.okhttp.OkHttpUrlLoader;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.stream.StreamModelLoader;
import com.kabouzeid.gramophone.lastfm.rest.LastFMRestClient;
import com.squareup.okhttp.OkHttpClient;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public class ArtistImageLoader implements StreamModelLoader<ArtistImage> {
    private Context context;
    private LastFMRestClient lastFMClient;
    private ModelLoader<GlideUrl, InputStream> urlLoader;

    public ArtistImageLoader(Context context, LastFMRestClient lastFMRestClient, ModelLoader<GlideUrl, InputStream> urlLoader) {
        this.context = context;
        this.lastFMClient = lastFMRestClient;
        this.urlLoader = urlLoader;
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(ArtistImage model, int width, int height) {
        return new ArtistImageFetcher(context, lastFMClient, model, urlLoader, width, height);
    }

    public static class Factory implements ModelLoaderFactory<ArtistImage, InputStream> {
        private LastFMRestClient lastFMClient;
        private OkHttpUrlLoader.Factory okHttpFactory;

        public Factory(Context context) {
            // we need these very low values to make sure our artist image loading calls doesn't block the image loading queue
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setConnectTimeout(500, TimeUnit.MILLISECONDS);
            okHttpClient.setReadTimeout(500, TimeUnit.MILLISECONDS);
            okHttpClient.setWriteTimeout(500, TimeUnit.MILLISECONDS);

            okHttpFactory = new OkHttpUrlLoader.Factory(okHttpClient);
            lastFMClient = new LastFMRestClient(context, okHttpClient);
        }

        @Override
        public ModelLoader<ArtistImage, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new ArtistImageLoader(context, lastFMClient, okHttpFactory.build(context, factories));
        }

        @Override
        public void teardown() {
            okHttpFactory.teardown();
        }
    }
}

