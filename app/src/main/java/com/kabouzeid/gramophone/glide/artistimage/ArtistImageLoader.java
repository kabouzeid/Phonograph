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

import java.io.InputStream;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public class ArtistImageLoader implements StreamModelLoader<ArtistImage> {
    private Context context;
    private LastFMRestClient lastFMRestClient;
    private ModelLoader<GlideUrl, InputStream> urlLoader;

    public ArtistImageLoader(Context context, LastFMRestClient lastFMRestClient, ModelLoader<GlideUrl, InputStream> urlLoader) {
        this.context = context;
        this.lastFMRestClient = lastFMRestClient;
        this.urlLoader = urlLoader;
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(ArtistImage model, int width, int height) {
        return new ArtistImageFetcher(context, lastFMRestClient, model, urlLoader, width, height);
    }

    public static class Factory implements ModelLoaderFactory<ArtistImage, InputStream> {
        private static volatile LastFMRestClient internalClient;
        private LastFMRestClient client;
        private OkHttpUrlLoader.Factory okHttpFactory;


        private static LastFMRestClient getInternalClient(Context context) {
            if (internalClient == null) {
                synchronized (Factory.class) {
                    if (internalClient == null) {
                        internalClient = new LastFMRestClient(context);
                    }
                }
            }
            return internalClient;
        }

        /**
         * Constructor for a new Factory that runs requests using a static singleton client.
         */
        public Factory(Context context) {
            client = getInternalClient(context);
            okHttpFactory = new OkHttpUrlLoader.Factory();
        }

        @Override
        public ModelLoader<ArtistImage, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new ArtistImageLoader(context, client, okHttpFactory.build(context, factories));
        }

        @Override
        public void teardown() {
            okHttpFactory.teardown();
        }
    }
}

