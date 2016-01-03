package com.kabouzeid.gramophone.glide.artistimage;

import android.content.Context;

import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.stream.StreamModelLoader;
import com.kabouzeid.gramophone.lastfm.rest.LastFMRestClient;

import java.io.InputStream;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public class ArtistImageLoader implements StreamModelLoader<ArtistImageRequest> {
    private Context context;
    private LastFMRestClient lastFMRestClient;

    public ArtistImageLoader(Context context, LastFMRestClient lastFMRestClient) {
        this.context = context;
        this.lastFMRestClient = lastFMRestClient;
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(ArtistImageRequest model, int width, int height) {
        return new ArtistImageFetcher(context, lastFMRestClient, model);
    }

    public static class Factory implements ModelLoaderFactory<ArtistImageRequest, InputStream> {
        private static volatile LastFMRestClient internalClient;
        private LastFMRestClient client;

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
        }

        @Override
        public ModelLoader<ArtistImageRequest, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new ArtistImageLoader(context, client);
        }

        @Override
        public void teardown() {
        }
    }
}

