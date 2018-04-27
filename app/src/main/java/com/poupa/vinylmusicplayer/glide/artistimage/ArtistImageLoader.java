package com.poupa.vinylmusicplayer.glide.artistimage;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;
import com.poupa.vinylmusicplayer.lastfm.rest.LastFMRestClient;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public class ArtistImageLoader implements ModelLoader<ArtistImage, InputStream> {
    // we need these very low values to make sure our artist image loading calls doesn't block the image loading queue
    private static final int TIMEOUT = 500;

    private Context context;
    private LastFMRestClient lastFMClient;
    private OkHttpClient okhttp;

    public ArtistImageLoader(Context context, LastFMRestClient lastFMRestClient, OkHttpClient okhttp) {
        this.context = context;
        this.lastFMClient = lastFMRestClient;
        this.okhttp = okhttp;
    }

    @Override
    public LoadData<InputStream> buildLoadData(@NonNull ArtistImage model, int width, int height,
                                               @NonNull Options options) {
        return new LoadData<>(new ObjectKey(model.artistName), new ArtistImageFetcher(context, lastFMClient, okhttp, model, width, height));
    }

    @Override
    public boolean handles(@NonNull ArtistImage model) {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<ArtistImage, InputStream> {
        private LastFMRestClient lastFMClient;
        private Context context;
        private OkHttpClient okHttp;

        public Factory(Context context) {
            this.context = context;
            okHttp = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                    .build();
            lastFMClient = new LastFMRestClient(LastFMRestClient.createDefaultOkHttpClientBuilder(context)
                    .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                    .build());
        }

        @Override
        @NonNull
        public ModelLoader<ArtistImage, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new ArtistImageLoader(context, lastFMClient, okHttp);
        }

        @Override
        public void teardown() {
        }
    }
}

