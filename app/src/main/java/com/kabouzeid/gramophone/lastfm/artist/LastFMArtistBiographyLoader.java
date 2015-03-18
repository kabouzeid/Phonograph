package com.kabouzeid.gramophone.lastfm.artist;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.provider.ArtistJSONStore;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by karim on 01.01.15.
 */
public class LastFMArtistBiographyLoader {
    public static final String TAG = LastFMArtistBiographyLoader.class.getSimpleName();

    public static void loadArtistBio(Context context, String queryArtist, ArtistBioLoaderCallback callback) {
        if (queryArtist != null) {
            String artistJSON = ArtistJSONStore.getInstance(context).getArtistJSON(queryArtist);
            if (artistJSON != null) {
                try {
                    JSONObject json = new JSONObject(artistJSON);
                    String bio = LastFMArtistInfoUtil.getArtistBiographyFromJSON(json);
                    callback.onArtistBioLoaded(bio);
                } catch (JSONException e) {
                    Log.e(TAG, "Error while parsing bio from cache to JSONObject", e);
                }
            } else {
                downloadArtistBio(context, queryArtist, callback);
            }
        }
    }

    private static void downloadArtistBio(final Context context, final String artist, final ArtistBioLoaderCallback callback) {
        App app = (App) context.getApplicationContext();
        String artistUrl = LastFMArtistInfoUtil.getArtistUrl(artist);
        JsonObjectRequest artistInfoJSONRequest = new JsonObjectRequest(0, artistUrl, (JSONObject)null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                LastFMArtistInfoUtil.saveArtistJSONDataToCacheAndDisk(context, artist, response);
                String bio = LastFMArtistInfoUtil.getArtistBiographyFromJSON(response);
                callback.onArtistBioLoaded(bio);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Download failed!", error);
                callback.onArtistBioLoaded("");
            }
        });
        app.addToVolleyRequestQueue(artistInfoJSONRequest);
    }

    public static interface ArtistBioLoaderCallback {
        public void onArtistBioLoaded(String bio);
    }
}
