package com.kabouzeid.gramophone.lastfm.album;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.provider.AlbumJSONStore;
import com.kabouzeid.gramophone.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by karim on 01.01.15.
 */
public class LastFMAlbumImageLoader {
    public static final String TAG = LastFMAlbumImageLoader.class.getSimpleName();

    public static void loadAlbumImage(Context context, String queryAlbum, String queryArtist, AlbumImageLoaderCallback callback) {
        if (queryAlbum != null) {
            String albumJSON = AlbumJSONStore.getInstance(context).getAlbumJSON(queryAlbum + queryArtist);
            if (albumJSON != null) {
                try {
                    loadAlbumImageFromJSON(new JSONObject(albumJSON), callback);
                } catch (JSONException e) {
                    Log.e(TAG, "Error while parsing string from cache to JSONObject", e);
                }
            } else {
                downloadAlbumImage(context, queryAlbum, queryArtist, callback);
            }
        }
    }

    private static void loadAlbumImageFromJSON(JSONObject jsonObject, final AlbumImageLoaderCallback callback) {
        String url = LastFMAlbumInfoUtil.getAlbumImageUrlFromJSON(jsonObject);
        if (!url.trim().equals("")) {
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(false)
                    .postProcessor(new BitmapProcessor() {
                        @Override
                        public Bitmap process(Bitmap bmp) {
                            return Util.getAlbumArtScaledBitmap(bmp, true);
                        }
                    })
                    .build();
            ImageLoader.getInstance().loadImage(url, options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    callback.onAlbumImageLoaded(null, null);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    callback.onAlbumImageLoaded(loadedImage, imageUri);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    callback.onAlbumImageLoaded(null, null);
                }
            });
        } else {
            callback.onAlbumImageLoaded(null, null);
        }
    }

    private static void downloadAlbumImage(final Context context, final String album, final String artist, final AlbumImageLoaderCallback callback) {
        App app = (App) context.getApplicationContext();
        String albumUrl = LastFMAlbumInfoUtil.getAlbumUrl(album, artist);
        JsonObjectRequest albumInfoJSONRequest = new JsonObjectRequest(0, albumUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                LastFMAlbumInfoUtil.saveAlbumJSONDataToCacheAndDisk(context, album, artist, response);
                loadAlbumImageFromJSON(response, callback);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Download failed!", error);
                callback.onAlbumImageLoaded(null, null);
            }
        });
        app.addToVolleyRequestQueue(albumInfoJSONRequest);
    }

    public static interface AlbumImageLoaderCallback {
        public void onAlbumImageLoaded(Bitmap albumImage, String uri);
    }
}
