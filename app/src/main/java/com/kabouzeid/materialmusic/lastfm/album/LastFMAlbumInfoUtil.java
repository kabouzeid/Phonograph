package com.kabouzeid.materialmusic.lastfm.album;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.kabouzeid.materialmusic.lastfm.LastFMUtil;
import com.kabouzeid.materialmusic.provider.AlbumJSONStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by karim on 24.12.14.
 */
public class LastFMAlbumInfoUtil {
    public static final String TAG = LastFMAlbumInfoUtil.class.getSimpleName();

    private static String AUTO_CORRECT = "1";

    public static String getAlbumUrl(String album, String artist) {
        if (album != null) {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority(LastFMUtil.BASE_URL)
                    .appendPath("2.0")
                    .appendQueryParameter("method", "album.getinfo")
                    .appendQueryParameter("album", album)
                    .appendQueryParameter("artist", artist)
                            //.appendQueryParameter("lang", "de")
                    .appendQueryParameter("autocorrect", AUTO_CORRECT)
                    .appendQueryParameter("api_key", LastFMUtil.API_KEY)
                    .appendQueryParameter("format", "json");
            return builder.build().toString();
        }
        return "";
    }

    public static String getAlbumNameFromJSON(JSONObject rootJSON) {
        try {
            return rootJSON.getJSONObject("album").getString("name");
        } catch (JSONException e) {
            //Log.e(TAG, "Error while getting album name from JSON parameter!", e);
            return "";
        }
    }

    public static String getAlbumThumbnailUrlFromJSON(JSONObject rootJSON) {
        try {
            JSONArray images = getAlbumImageArrayFromJSON(rootJSON);
            if (images.length() > 2) {
                return images.getJSONObject(2).getString("#text");
            } else if (images.length() > 1) {
                return images.getJSONObject(1).getString("#text");
            }
            return images.getJSONObject(0).getString("#text");
        } catch (JSONException | NullPointerException e) {
            //Log.e(TAG, "Error while getting album thumbnail image from JSON parameter!", e);
            return "";
        }
    }

    public static String getAlbumImageUrlFromJSON(JSONObject rootJSON) {
        try {
            JSONArray images = getAlbumImageArrayFromJSON(rootJSON);
            return images.getJSONObject(images.length() - 1).getString("#text");
        } catch (JSONException | NullPointerException e) {
            //Log.e(TAG, "Error while getting album image from JSON parameter!", e);
            return "";
        }
    }

    public static JSONArray getAlbumImageArrayFromJSON(JSONObject rootJSON) {
        try {
            return rootJSON.getJSONObject("album").getJSONArray("image");
        } catch (JSONException e) {
            //Log.e(TAG, "Error while getting album image array from JSON parameter!", e);
            return null;
        }
    }

    public static void saveAlbumJSONDataToCacheAndDisk(Context context, String album, String artist, JSONObject jsonObject) {
        Log.i(TAG, "Saving new JSON album data for " + album + "...");
        AlbumJSONStore.getInstance(context).addAlbumJSON(album + artist, jsonObject.toString());
    }
}
