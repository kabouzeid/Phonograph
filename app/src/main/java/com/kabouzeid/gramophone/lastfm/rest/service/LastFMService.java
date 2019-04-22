package com.kabouzeid.gramophone.lastfm.rest.service;

import androidx.annotation.Nullable;

import com.kabouzeid.gramophone.lastfm.rest.model.LastFmAlbum;
import com.kabouzeid.gramophone.lastfm.rest.model.LastFmArtist;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public interface LastFMService {
    String API_KEY = "bd9c6ea4d55ec9ed3af7d276e5ece304";
    String BASE_QUERY_PARAMETERS = "?format=json&autocorrect=1&api_key=" + API_KEY;

    @GET(BASE_QUERY_PARAMETERS + "&method=album.getinfo")
    Call<LastFmAlbum> getAlbumInfo(@Query("album") String albumName, @Query("artist") String artistName, @Nullable @Query("lang") String language);

    @GET(BASE_QUERY_PARAMETERS + "&method=artist.getinfo")
    Call<LastFmArtist> getArtistInfo(@Query("artist") String artistName, @Nullable @Query("lang") String language, @Nullable @Header("Cache-Control") String cacheControl);
}