package com.kabouzeid.gramophone.lastfm.artist;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.kabouzeid.gramophone.provider.ArtistJSONStore;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by karim on 01.01.15.
 */
public class LastFMArtistImageUrlLoader {
    public static final String TAG = LastFMArtistImageUrlLoader.class.getSimpleName();

    public static void loadArtistImageUrl(final Context context, String queryArtist, boolean forceDownload, final ArtistImageUrlLoaderCallback callback) {
        if (queryArtist != null && !queryArtist.trim().equals("<unknown>")) {
            String artistJSON = ArtistJSONStore.getInstance(context).getArtistJSON(queryArtist);
            if (artistJSON != null && !forceDownload) {
                try {
                    loadArtistImageUrlFromJSON(new JSONObject(artistJSON), callback);
                } catch (JSONException e) {
                    Log.e(TAG, "Error while parsing string from cache to JSONObject", e);
                }
            } else {
                LastFMArtistInfoUtil.downloadArtistJSON(context, queryArtist, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadArtistImageUrlFromJSON(response, callback);
                    }
                });
            }
        }
    }

    private static void loadArtistImageUrlFromJSON(JSONObject jsonObject, final ArtistImageUrlLoaderCallback callback) {
        String url = LastFMArtistInfoUtil.getArtistImageUrlFromJSON(jsonObject);
        if (!url.trim().equals("")) {
            callback.onArtistImageUrlLoaded(url);
        }
    }

    public static interface ArtistImageUrlLoaderCallback {
        public void onArtistImageUrlLoaded(String url);
    }
}
