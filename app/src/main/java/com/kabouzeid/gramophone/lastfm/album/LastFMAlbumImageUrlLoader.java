package com.kabouzeid.gramophone.lastfm.album;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.kabouzeid.gramophone.provider.AlbumJSONStore;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class LastFMAlbumImageUrlLoader {
    public static final String TAG = LastFMAlbumImageUrlLoader.class.getSimpleName();

    public static void loadAlbumImageUrl(Context context, String queryAlbum, String queryArtist, final AlbumImageUrlLoaderCallback callback) {
        if (queryAlbum != null) {
            String albumJSON = AlbumJSONStore.getInstance(context).getJSONData(queryAlbum + queryArtist);
            if (albumJSON != null) {
                try {
                    loadAlbumImageUrlFromJSON(new JSONObject(albumJSON), callback);
                } catch (JSONException e) {
                    Log.e(TAG, "Error while parsing string from cache to JSONObject", e);
                }
            } else {
                LastFMAlbumInfoUtil.downloadAlbumInfoJSON(context, queryAlbum, queryArtist, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadAlbumImageUrlFromJSON(response, callback);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError();
                    }
                });
            }
        }
    }

    private static void loadAlbumImageUrlFromJSON(final JSONObject jsonObject, final AlbumImageUrlLoaderCallback callback) {
        String url = LastFMAlbumInfoUtil.getAlbumImageUrlFromJSON(jsonObject);
        if (!url.trim().equals("")) {
            callback.onAlbumImageUrlLoaded(url);
        } else {
            callback.onError();
        }
    }

    public interface AlbumImageUrlLoaderCallback {
        void onAlbumImageUrlLoaded(String url);

        void onError();
    }
}
