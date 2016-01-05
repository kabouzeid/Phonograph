package com.kabouzeid.gramophone.lastfm.rest;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kabouzeid.gramophone.lastfm.rest.service.LastFMService;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class LastFMRestClient {
    public static final String BASE_URL = "http://ws.audioscrobbler.com/2.0/";

    private LastFMService apiService;

    public LastFMRestClient(@NonNull Context context) {
        this(context, new OkHttpClient());
    }

    public LastFMRestClient(@NonNull Context context, @NonNull OkHttpClient okHttpClient) {
        File cacheDir = new File(context.getCacheDir().getAbsolutePath(), "/okhttp-lastfm/");
        if (cacheDir.mkdirs() || cacheDir.isDirectory()) {
            okHttpClient.setCache(new Cache(cacheDir, 1024 * 1024 * 10));
        }

        okHttpClient.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request modifiedRequest = chain.request().newBuilder()
                        .addHeader("Cache-Control", String.format("max-age=%d, max-stale=%d", 31536000, 31536000))
                        .build();
                return chain.proceed(modifiedRequest);
            }
        });

        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = restAdapter.create(LastFMService.class);
    }

    public LastFMService getApiService() {
        return apiService;
    }
}
