package com.kabouzeid.gramophone.glide.artistimage;

import android.content.Context;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.data.HttpUrlFetcher;
import com.bumptech.glide.load.model.GlideUrl;
import com.kabouzeid.gramophone.lastfm.rest.LastFMRestClient;
import com.kabouzeid.gramophone.lastfm.rest.model.LastFmArtist;
import com.kabouzeid.gramophone.util.LastFMUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.Util;

import java.io.InputStream;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistImageFetcher implements DataFetcher<InputStream> {
    private Context context;
    private final LastFMRestClient lastFMRestClient;
    private final ArtistImage model;
    private HttpUrlFetcher urlFetcher;
    private volatile boolean isCancelled;

    public ArtistImageFetcher(Context context, LastFMRestClient lastFMRestClient, ArtistImage model) {
        this.context = context;
        this.lastFMRestClient = lastFMRestClient;
        this.model = model;
    }

    @Override
    public String getId() {
        return model.artistName;
    }

    @Override
    public InputStream loadData(Priority priority) throws Exception {
        if (!MusicUtil.isArtistNameUnknown(model.artistName) && Util.isAllowedToAutoDownload(context)) {
            LastFmArtist lastFmArtist = lastFMRestClient.getApiService().getArtistInfo(model.artistName, model.skipOkHttpCache ? "no-cache" : null).execute().body();

            if (isCancelled) return null;

            urlFetcher = new HttpUrlFetcher(new GlideUrl(LastFMUtil.getLargestArtistImageUrl(lastFmArtist.getArtist().getImage())));
            return urlFetcher.loadData(priority);
        }
        return null;
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
