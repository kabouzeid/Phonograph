package com.poupa.vinylmusicplayer.glide.artistimage;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.poupa.vinylmusicplayer.lastfm.rest.LastFMRestClient;
import com.poupa.vinylmusicplayer.lastfm.rest.model.LastFmArtist;
import com.poupa.vinylmusicplayer.util.LastFMUtil;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import retrofit2.Response;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistImageFetcher implements DataFetcher<InputStream> {
    public static final String TAG = ArtistImageFetcher.class.getSimpleName();
    private Context context;
    private final LastFMRestClient lastFMRestClient;
    private final ArtistImage model;
    private ModelLoader<GlideUrl, InputStream> urlLoader;
    private final int width;
    private final int height;
    private volatile boolean isCancelled;
    private DataFetcher<InputStream> urlFetcher;

    public ArtistImageFetcher(Context context, LastFMRestClient lastFMRestClient, ArtistImage model, ModelLoader<GlideUrl, InputStream> urlLoader, int width, int height) {
        this.context = context;
        this.lastFMRestClient = lastFMRestClient;
        this.model = model;
        this.urlLoader = urlLoader;
        this.width = width;
        this.height = height;
    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        if (!MusicUtil.isArtistNameUnknown(model.artistName) && Util.isAllowedToDownloadMetadata(context)) {
            try {
                Response<LastFmArtist> response = lastFMRestClient.getApiService().getArtistInfo(model.artistName, null, model.skipOkHttpCache ? "no-cache" : null).execute();

                if (!response.isSuccessful()) {
                    throw new IOException("Request failed with code: " + response.code());
                }

                LastFmArtist lastFmArtist = response.body();

                if (isCancelled) return;

                if (lastFmArtist != null) {
                    URL urlH = new URL(LastFMUtil.getLargestArtistImageUrl(lastFmArtist.getArtist().getImage()));
                    InputStream is = urlH.openStream();

                    callback.onDataReady(is);

                    /*GlideUrl url = new GlideUrl(LastFMUtil.getLargestArtistImageUrl(lastFmArtist.getArtist().getImage()));

                    //urlFetcher = urlLoader.getResourceFetcher(url, width, height);
                    ModelLoader.LoadData<InputStream> modelLoader = urlLoader.buildLoadData(url, width, height, new Options());
                    if (modelLoader != null) {
                        urlFetcher = modelLoader.fetcher;

                        // Here we want to get the InputStream urlFetcher.loadData(priority, callback) is returning
                        urlFetcher.loadData(priority, callback);

                        //callback.onDataReady(urlFetcher.loadData(priority, callback));
                    }*/
                }
            } catch (IOException e) {
                callback.onLoadFailed(e);
            }
        }
    }

    @Override
    public void cleanup() {
        if (urlFetcher != null) {
            urlFetcher.cleanup();
        }
    }

    @Override
    public void cancel() {
        isCancelled = true;
        if (urlFetcher != null) {
            urlFetcher.cancel();
        }
    }
}
