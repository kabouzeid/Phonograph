package com.kabouzeid.gramophone.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class PreferenceUtils {

    /* Default start page (Artist page) */
    public static final int DEFAULT_PAGE = 2;

    /* Saves the last page the pager was on in {@link MusicBrowserPhoneFragment} */
    public static final String START_PAGE = "start_page";

    // Sort order for the artist list
    public static final String ARTIST_SORT_ORDER = "artist_sort_order";

    // Sort order for the artist song list
    public static final String ARTIST_SONG_SORT_ORDER = "artist_song_sort_order";

    // Sort order for the artist album list
    public static final String ARTIST_ALBUM_SORT_ORDER = "artist_album_sort_order";

    // Sort order for the album list
    public static final String ALBUM_SORT_ORDER = "album_sort_order";

    // Sort order for the album song list
    public static final String ALBUM_SONG_SORT_ORDER = "album_song_sort_order";

    // Sort order for the song list
    public static final String SONG_SORT_ORDER = "song_sort_order";

    // Key used to download images only on Wi-Fi
    public static final String ONLY_ON_WIFI = "only_on_wifi";

    // Key that gives permissions to download missing artist images
    public static final String DOWNLOAD_MISSING_ARTIST_IMAGES = "download_missing_artist_images";

    private static PreferenceUtils sInstance;

    private final SharedPreferences mPreferences;

    public PreferenceUtils(final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static final PreferenceUtils getInstace(final Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceUtils(context.getApplicationContext());
        }
        return sInstance;
    }

    public void setStartPage(final int value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(START_PAGE, value);
        editor.apply();
    }

    public final int getStartPage() {
        return mPreferences.getInt(START_PAGE, DEFAULT_PAGE);
    }

    public final boolean onlyOnWifi() {
        return mPreferences.getBoolean(ONLY_ON_WIFI, true);
    }

    public void setOnlyOnWifi(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(ONLY_ON_WIFI, value);
        editor.apply();
    }

    public final boolean downloadMissingArtistImages() {
        return mPreferences.getBoolean(DOWNLOAD_MISSING_ARTIST_IMAGES, true);
    }

    public void setDownloadMissingArtistImages(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(DOWNLOAD_MISSING_ARTIST_IMAGES, value);
        editor.apply();
    }

    private void setSortOrder(final String key, final String value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void setArtistSortOrder(final String value) {
        setSortOrder(ARTIST_SORT_ORDER, value);
    }

    public final String getArtistSortOrder() {
        return mPreferences.getString(ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.ARTIST_A_Z);
    }

    public void setArtistSongSortOrder(final String value) {
        setSortOrder(ARTIST_SONG_SORT_ORDER, value);
    }

    public final String getArtistSongSortOrder() {
        return mPreferences.getString(ARTIST_SONG_SORT_ORDER,
                SortOrder.ArtistSongSortOrder.SONG_A_Z);
    }

    public void setArtistAlbumSortOrder(final String value) {
        setSortOrder(ARTIST_ALBUM_SORT_ORDER, value);
    }

    public final String getArtistAlbumSortOrder() {
        return mPreferences.getString(ARTIST_ALBUM_SORT_ORDER,
                SortOrder.ArtistAlbumSortOrder.ALBUM_A_Z);
    }

    public void setAlbumSortOrder(final String value) {
        setSortOrder(ALBUM_SORT_ORDER, value);
    }

    public final String getAlbumSortOrder() {
        return mPreferences.getString(ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_A_Z);
    }

    public void setAlbumSongSortOrder(final String value) {
        setSortOrder(ALBUM_SONG_SORT_ORDER, value);
    }

    public final String getAlbumSongSortOrder() {
        return mPreferences.getString(ALBUM_SONG_SORT_ORDER,
                SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST);
    }

    public void setSongSortOrder(final String value) {
        setSortOrder(SONG_SORT_ORDER, value);
    }

    public final String getSongSortOrder() {
        return mPreferences.getString(SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_A_Z);
    }
}
