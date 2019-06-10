package com.kabouzeid.gramophone.deezer.rest.service;

import androidx.annotation.Nullable;
import com.kabouzeid.gramophone.deezer.rest.model.DeezerArtist;
import com.kabouzeid.gramophone.deezer.rest.model.DeezerResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public interface DeezerService {

    String BASE_QUERY_ARTIST = "search/artist";

    @GET(BASE_QUERY_ARTIST + "?limit=10")
    Call<DeezerResponse<DeezerArtist>> getArtistInfo(@Query("q") String artistName, @Nullable @Header("Cache-Control") String cacheControl);

}