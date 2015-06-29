package com.kabouzeid.gramophone.lastfm.rest.service;

import android.support.annotation.Nullable;

import com.kabouzeid.gramophone.lastfm.rest.model.albuminfo.AlbumInfo;
import com.kabouzeid.gramophone.lastfm.rest.model.artistinfo.ArtistInfo;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Query;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public interface LastFMService {
    String API_KEY = "bd9c6ea4d55ec9ed3af7d276e5ece304";
    String BASE_QUERY_PARAMETERS = "/?format=json&autocorrect=1&api_key=" + API_KEY;

    @GET(BASE_QUERY_PARAMETERS + "&method=album.getinfo")
    void getAlbumInfo(@Query("album") String albumName, @Query("artist") String artistName, Callback<AlbumInfo> callback);

    @GET(BASE_QUERY_PARAMETERS + "&method=artist.getinfo")
    void getArtistInfo(@Query("artist") String artistName, @Nullable @Header("Cache-Control") String cacheControl, Callback<ArtistInfo> callback);
}