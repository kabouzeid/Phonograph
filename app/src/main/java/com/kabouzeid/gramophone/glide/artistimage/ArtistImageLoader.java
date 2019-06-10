package com.kabouzeid.gramophone.glide.artistimage;

import android.content.Context;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.stream.StreamModelLoader;
import com.kabouzeid.gramophone.deezer.rest.DeezerRestClient;
import okhttp3.OkHttpClient;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public class ArtistImageLoader implements StreamModelLoader<ArtistImage> {
    // we need these very low values to make sure our artist image loading calls doesn't block the image loading queue
    private static final int TIMEOUT = 700;

    private Context context;
    private DeezerRestClient deezerRestClient;
    private ModelLoader<GlideUrl, InputStream> urlLoader;

    public ArtistImageLoader(Context context, DeezerRestClient deezerRestClient, ModelLoader<GlideUrl, InputStream> urlLoader) {
        this.context = context;
        this.deezerRestClient = deezerRestClient;
        this.urlLoader = urlLoader;
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(ArtistImage model, int width, int height) {
        return new ArtistImageFetcher(context, model, deezerRestClient, urlLoader, width, height);
    }

    public static class Factory implements ModelLoaderFactory<ArtistImage, InputStream> {
        private DeezerRestClient deezerRestClient;
        private OkHttpUrlLoader.Factory okHttpFactory;

        public Factory(Context context) {
            okHttpFactory = new OkHttpUrlLoader.Factory(new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                    .build());
            deezerRestClient = new DeezerRestClient(DeezerRestClient.createDefaultOkHttpClientBuilder(context)
                    .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                    .build());
        }

        @Override
        public ModelLoader<ArtistImage, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new ArtistImageLoader(context, deezerRestClient, okHttpFactory.build(context, factories));
        }

        @Override
        public void teardown() {
        }
    }
}

