package com.kabouzeid.gramophone.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.kabouzeid.gramophone.R;

public final class PreferenceUtils {

    /* Default start page (Album page) */
    public static final int DEFAULT_PAGE = 1;

    public static final String GENERAL_THEME = "general_theme";

    /* Saves the last page the pager was on in {@link MainActivity} */
    public static final String DEFAULT_START_PAGE = "default_start_page";

    /* Saves the last page the pager was on in {@link MainActivity} */
    public static final String LAST_START_PAGE = "last_start_page";

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
    public static final String ONLY_ON_WIFI = "auto_download_artist_images";

    // Key that gives permissions to download missing artist images
    public static final String DOWNLOAD_MISSING_ARTIST_IMAGES = "auto_download_artist_images";

    // Key used to en or disable palette
    public static final String COLORED_ALBUM_FOOTERS = "colored_album_footers";

    // Key used to en or disable the colored navigation bar
    public static final String COLORED_NAVIGATION_BAR_ALBUM = "colored_navigation_bar_album";

    // Key used to en or disable the colored navigation bar
    public static final String COLORED_NAVIGATION_BAR_ARTIST = "colored_navigation_bar_artist";

    // Key used to en or disable the colored navigation bar
    public static final String COLORED_NAVIGATION_BAR_CURRENT_PLAYING = "colored_navigation_bar_current_playing_enabled";

    // Key used to en or disable the colored navigation bar
    public static final String PLAYBACK_CONTROLLER_BOX = "playback_controller_card";

    private static PreferenceUtils sInstance;

    private final SharedPreferences mPreferences;

    public PreferenceUtils(final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static final PreferenceUtils getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceUtils(context.getApplicationContext());
        }
        return sInstance;
    }

    public int getGeneralTheme() {
        int value =  Integer.parseInt(mPreferences.getString(GENERAL_THEME, "1"));
        switch (value){
            case 0:
                return R.style.Theme_MaterialMusic_Light;
            case 1:
                return R.style.Theme_MaterialMusic;
        }
        return R.style.Theme_MaterialMusic;
    }

    public void setGeneralTheme(int appTheme) {
        int value = -1;
        switch (appTheme) {
            case R.style.Theme_MaterialMusic_Light:
                value = 0;
                break;
            case R.style.Theme_MaterialMusic:
                value = 1;
                break;
        }
        if (value != 0 && value != 1) {
            return;
        }
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(GENERAL_THEME, String.valueOf(value));
        editor.apply();
    }

    public void setDefaultStartPage(final int value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(DEFAULT_START_PAGE, String.valueOf(value));
        editor.apply();
    }

    public final int getDefaultStartPage() {
        return Integer.parseInt(mPreferences.getString(DEFAULT_START_PAGE, "-1"));
    }

    public void setLastStartPage(final int value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(LAST_START_PAGE, value);
        editor.apply();
    }

    public final int getLastStartPage() {
        return mPreferences.getInt(LAST_START_PAGE, DEFAULT_PAGE);
    }

    public final boolean autoDownloadOnlyOnWifi() {
        return mPreferences.getBoolean(ONLY_ON_WIFI, false);
    }

    public void setAutoDownloadOnlyOnWifi(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(ONLY_ON_WIFI, value);
        editor.apply();
    }

    public final boolean coloredAlbumFootersEnabled() {
        return mPreferences.getBoolean(COLORED_ALBUM_FOOTERS, true);
    }

    public void setColoredAlbumFootersEnabled(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(COLORED_ALBUM_FOOTERS, value);
        editor.apply();
    }

    public final boolean coloredNavigationBarAlbumEnabled() {
        return mPreferences.getBoolean(COLORED_NAVIGATION_BAR_ALBUM, true);
    }

    public void setColoredNavigationBarAlbumEnabled(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(COLORED_NAVIGATION_BAR_ALBUM, value);
        editor.apply();
    }

    public final boolean coloredNavigationBarArtistEnabled() {
        return mPreferences.getBoolean(COLORED_NAVIGATION_BAR_ARTIST, true);
    }

    public void setColoredNavigationBarArtistEnabled(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(COLORED_NAVIGATION_BAR_ARTIST, value);
        editor.apply();
    }

    public final boolean coloredNavigationBarCurrentPlayingEnabled() {
        return mPreferences.getBoolean(COLORED_NAVIGATION_BAR_CURRENT_PLAYING, true);
    }

    public void setColoredNavigationBarCurrentPlayingEnabled(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(COLORED_NAVIGATION_BAR_CURRENT_PLAYING, value);
        editor.apply();
    }

    public final boolean playbackControllerBoxEnabled() {
        return mPreferences.getBoolean(PLAYBACK_CONTROLLER_BOX, true);
    }

    public void setPlaybackControllerBoxEnabled(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(PLAYBACK_CONTROLLER_BOX, value);
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
