package com.poupa.vinylmusicplayer.glide.artistimage;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.bumptech.glide.Priority;
import com.bumptech.glide.integration.okhttp3.OkHttpStreamFetcher;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;
import com.poupa.vinylmusicplayer.lastfm.rest.LastFMRestClient;
import com.poupa.vinylmusicplayer.lastfm.rest.model.LastFmArtist;
import com.poupa.vinylmusicplayer.util.LastFMUtil;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.Util;

import java.io.InputStream;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistImageFetcher implements DataFetcher<InputStream> {
    public static final String TAG = ArtistImageFetcher.class.getSimpleName();
    private Context context;
    private final LastFMRestClient lastFMRestClient;
    private final ArtistImage model;
    private volatile boolean isCancelled;
    private Call<LastFmArtist> call;
    private OkHttpClient okhttp;
    private OkHttpStreamFetcher streamFetcher;

    public ArtistImageFetcher(Context context, LastFMRestClient lastFMRestClient, OkHttpClient okhttp, ArtistImage model, int width, int height) {
        this.context = context;
        this.lastFMRestClient = lastFMRestClient;
        this.okhttp = okhttp;
        this.model = model;
    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.REMOTE;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        try {
            if (!MusicUtil.isArtistNameUnknown(model.artistName) && Util.isAllowedToDownloadMetadata(context)) {
                call = lastFMRestClient.getApiService().getArtistInfo(model.artistName, null, model.skipOkHttpCache ? "no-cache" : null);
                call.enqueue(new Callback<LastFmArtist>() {
                    @Override
                    public void onResponse(@NonNull Call<LastFmArtist> call, @NonNull Response<LastFmArtist> response) {
                        if (isCancelled) {
                            callback.onDataReady(null);
                            return;
                        }

                        LastFmArtist lastFmArtist = response.body();
                        if (lastFmArtist == null || lastFmArtist.getArtist() == null || lastFmArtist.getArtist().getImage() == null) {
                            callback.onLoadFailed(new Exception("No artist image url found"));
                            return;
                        }

                        String url = LastFMUtil.getLargestArtistImageUrl(lastFmArtist.getArtist().getImage());
                        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(url.trim())) {
                            callback.onLoadFailed(new Exception("No artist image url found"));
                            return;
                        }

                        streamFetcher = new OkHttpStreamFetcher(okhttp, new GlideUrl(url));
                        streamFetcher.loadData(priority, callback);
                    }

                    @Override
                    public void onFailure(@NonNull Call<LastFmArtist> call, @NonNull Throwable throwable) {
                        callback.onLoadFailed(new Exception(throwable));
                    }
                });


            }
        } catch (Exception e) {
            callback.onLoadFailed(e);
        }
    }

    @Override
    public void cleanup() {
        if (streamFetcher != null) {
            streamFetcher.cleanup();
        }
    }

    @Override
    public void cancel() {
        isCancelled = true;
        if (call != null) {
            call.cancel();
        }
        if (streamFetcher != null) {
            streamFetcher.cancel();
        }
    }
}
