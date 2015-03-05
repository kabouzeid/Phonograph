package com.kabouzeid.gramophone.lastfm.artist;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.provider.ArtistJSONStore;
import com.kabouzeid.gramophone.util.ImageLoaderUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by karim on 01.01.15.
 */
public class LastFMArtistImageLoader {
    public static final String TAG = LastFMArtistImageLoader.class.getSimpleName();

    @Deprecated
    public static void loadArtistImage(Context context, String queryArtist, ArtistImageLoaderCallback callback) {
        loadArtistImage(context, queryArtist, false, callback);
    }

    public static void loadArtistImage(Context context, String queryArtist, boolean forceDownload, ArtistImageLoaderCallback callback) {
        if (queryArtist != null) {
            String artistJSON = ArtistJSONStore.getInstance(context).getArtistJSON(queryArtist);
            if (artistJSON != null && !forceDownload) {
                Log.i(TAG, queryArtist + " is in cache.");
                try {
                    loadArtistImageFromJSON(new JSONObject(artistJSON), callback);
                } catch (JSONException e) {
                    Log.e(TAG, "Error while parsing string from cache to JSONObject", e);
                }
            } else {
                if(forceDownload){
                    Log.i(TAG, queryArtist + " force re-download");
                } else {
                    Log.i(TAG, queryArtist + " is not in cache.");
                }
                downloadArtistJSONAndStartImageDownload(context, queryArtist, callback);
            }
        }
    }

    private static void loadArtistImageFromJSON(JSONObject jsonObject, final ArtistImageLoaderCallback callback) {
        Log.i(TAG, "Applying artist art...");
        String url = LastFMArtistInfoUtil.getArtistImageUrlFromJSON(jsonObject);
        if (!url.trim().equals("")) {
            ImageLoader.getInstance().loadImage(url, ImageLoaderUtil.getCacheOnDiskOptions(), new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    callback.onArtistImageLoaded(null);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    callback.onArtistImageLoaded(loadedImage);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    callback.onArtistImageLoaded(null);
                }
            });
        } else {
            callback.onArtistImageLoaded(null);
        }
    }

    private static void downloadArtistJSONAndStartImageDownload(final Context context, final String artist, final ArtistImageLoaderCallback callback) {
        Log.i(TAG, "Downloading details for " + artist);
        App app = (App) context.getApplicationContext();
        String artistUrl = LastFMArtistInfoUtil.getArtistUrl(artist);
        JsonObjectRequest artistInfoJSONRequest = new JsonObjectRequest(0, artistUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "Download was successful!");
                LastFMArtistInfoUtil.saveArtistJSONDataToCacheAndDisk(context, artist, response);
                loadArtistImageFromJSON(response, callback);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Download failed!", error);
                callback.onArtistImageLoaded(null);
            }
        });
        app.addToVolleyRequestQueue(artistInfoJSONRequest);
    }

    public static interface ArtistImageLoaderCallback {
        public void onArtistImageLoaded(Bitmap artistImage);
    }
}
