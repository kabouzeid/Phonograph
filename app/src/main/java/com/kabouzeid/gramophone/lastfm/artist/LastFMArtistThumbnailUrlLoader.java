package com.kabouzeid.gramophone.lastfm.artist;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.kabouzeid.gramophone.provider.ArtistJSONStore;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class LastFMArtistThumbnailUrlLoader {
    public static final String TAG = LastFMArtistThumbnailUrlLoader.class.getSimpleName();

    public static void loadArtistThumbnailUrl(final Context context, String queryArtist, boolean forceDownload, final ArtistThumbnailUrlLoaderCallback callback) {
        if (queryArtist != null && !queryArtist.trim().equals("<unknown>")) {
            String artistJSON = ArtistJSONStore.getInstance(context).getArtistJSON(queryArtist);
            if (artistJSON != null && !forceDownload) {
                try {
                    loadArtistThumbnailUrlFromJSON(new JSONObject(artistJSON), callback);
                } catch (JSONException e) {
                    Log.e(TAG, "Error while parsing string from cache to JSONObject", e);
                }
            } else {
                LastFMArtistInfoUtil.downloadArtistJSON(context, queryArtist, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadArtistThumbnailUrlFromJSON(response, callback);
                    }
                });
            }
        }
    }

    private static void loadArtistThumbnailUrlFromJSON(final JSONObject jsonObject, final ArtistThumbnailUrlLoaderCallback callback) {
        String url = LastFMArtistInfoUtil.getArtistThumbnailUrlFromJSON(jsonObject);
        if (!url.trim().equals("")) {
            callback.onArtistThumbnailUrlLoaded(url);
        }
    }

    public interface ArtistThumbnailUrlLoaderCallback {
        void onArtistThumbnailUrlLoaded(String url);
    }
}
