package com.kabouzeid.gramophone.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.ColorChooserDialog;

public final class PreferenceUtils {

    public static final int DEFAULT_PAGE = 1;
    public static final String GENERAL_THEME = "general_theme";
    public static final String DEFAULT_START_PAGE = "default_start_page";
    public static final String LAST_START_PAGE = "last_start_page";
    public static final String ARTIST_SORT_ORDER = "artist_sort_order";
    public static final String ARTIST_SONG_SORT_ORDER = "artist_song_sort_order";
    public static final String ARTIST_ALBUM_SORT_ORDER = "artist_album_sort_order";
    public static final String ALBUM_SORT_ORDER = "album_sort_order";
    public static final String ALBUM_SONG_SORT_ORDER = "album_song_sort_order";
    public static final String SONG_SORT_ORDER = "song_sort_order";
    public static final String ONLY_ON_WIFI = "auto_download_artist_images";
    //    public static final String DOWNLOAD_MISSING_ARTIST_IMAGES = "auto_download_artist_images";
    public static final String COLORED_ALBUM_FOOTERS = "colored_album_footers";
    public static final String COLORED_NAVIGATION_BAR = "colored_navigation_bar";
    public static final String COLORED_NAVIGATION_BAR_ALBUM = "colored_navigation_bar_album";
    public static final String COLORED_NAVIGATION_BAR_ARTIST = "colored_navigation_bar_artist";
    public static final String COLORED_NAVIGATION_BAR_CURRENT_PLAYING = "colored_navigation_bar_current_playing";
    public static final String COLORED_NAVIGATION_BAR_PLAYIST = "colored_navigation_bar_playlist";
    public static final String COLORED_NAVIGATION_BAR_TAG_EDITOR = "colored_navigation_bar_tag_editor";
    public static final String COLORED_NAVIGATION_BAR_OTHER_SCREENS = "colored_navigation_bar_other_screens";
    public static final String PLAYBACK_CONTROLLER_BOX = "playback_controller_card";
    public static final String TRANSPARENT_TOOLBAR = "transparent_toolbar";
    public static final String ALBUM_GRID_COLUMNS = "album_grid_columns";
    public static final String ALBUM_GRID_COLUMNS_LAND = "album_grid_columns_land";

    private static PreferenceUtils sInstance;

    private final Context mContext;
    private final SharedPreferences mPreferences;

    public PreferenceUtils(final Context context) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferenceUtils getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceUtils(context.getApplicationContext());
        }
        return sInstance;
    }

    public int getGeneralTheme() {
        int value = Integer.parseInt(mPreferences.getString(GENERAL_THEME, "0"));
        switch (value) {
            case 0:
                return R.style.Theme_MaterialMusic_Light;
            case 1:
                return R.style.Theme_MaterialMusic;
        }
        return R.style.Theme_MaterialMusic_Light;
    }

    public int getThemeColorPrimary() {
        return mPreferences.getInt("primary_color", mContext.getResources().getColor(R.color.indigo_500));
    }

    public int getThemeColorPrimaryDarker() {
        return ColorChooserDialog.shiftColorDown(getThemeColorPrimary());
    }

    @SuppressLint("CommitPrefEdits")
    public void setThemeColorPrimary(int color) {
        mPreferences.edit().putInt("primary_color", color).commit();
    }

    public int getThemeColorAccent() {
        return mPreferences.getInt("accent_color", mContext.getResources().getColor(R.color.pink_500));
    }

    @SuppressLint("CommitPrefEdits")
    public void setThemeColorAccent(int color) {
        mPreferences.edit().putInt("accent_color", color).commit();
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

//    public final boolean autoDownloadOnlyOnWifi() {
//        return mPreferences.getBoolean(ONLY_ON_WIFI, false);
//    }

    public final boolean coloredAlbumFootersEnabled() {
        return mPreferences.getBoolean(COLORED_ALBUM_FOOTERS, true);
    }

    public final boolean coloredNavigationBarAlbumEnabled() {
        return coloredNavigationBarFor(COLORED_NAVIGATION_BAR_ALBUM);
    }

    public final boolean coloredNavigationBarArtistEnabled() {
        return coloredNavigationBarFor(COLORED_NAVIGATION_BAR_ARTIST);
    }

    public final boolean coloredNavigationBarCurrentPlayingEnabled() {
        return coloredNavigationBarFor(COLORED_NAVIGATION_BAR_CURRENT_PLAYING);
    }

    public final boolean coloredNavigationBarPlaylistEnabled() {
        return coloredNavigationBarFor(COLORED_NAVIGATION_BAR_PLAYIST);
    }

    public final boolean coloredNavigationBarTagEditorEnabled() {
        return coloredNavigationBarFor(COLORED_NAVIGATION_BAR_TAG_EDITOR);
    }

    public final boolean coloredNavigationBarOtherScreensEnabled() {
        return coloredNavigationBarFor(COLORED_NAVIGATION_BAR_OTHER_SCREENS);
    }

    private final boolean coloredNavigationBarFor(String key) {
        try {
            return mPreferences.getStringSet(COLORED_NAVIGATION_BAR, null).contains(key);
        } catch (NullPointerException e) {
            return false;
        }
    }

    @SuppressLint("CommitPrefEdits")
    private void setColoredNavigationBarOtherScreens(boolean coloredNavbar) {
        mPreferences.edit().putBoolean(COLORED_NAVIGATION_BAR_OTHER_SCREENS, coloredNavbar).commit();
    }

    public final boolean playbackControllerBoxEnabled() {
        return mPreferences.getBoolean(PLAYBACK_CONTROLLER_BOX, false);
    }

    public final boolean transparentToolbar() {
        return mPreferences.getBoolean(TRANSPARENT_TOOLBAR, false);
    }

//    public final boolean downloadMissingArtistImages() {
//        return mPreferences.getBoolean(DOWNLOAD_MISSING_ARTIST_IMAGES, true);
//    }
//

//    private void setSortOrder(final String key, final String value) {
//        final SharedPreferences.Editor editor = mPreferences.edit();
//        editor.putString(key, value);
//        editor.apply();
//    }

    //    public void setArtistSortOrder(final String value) {
//        setSortOrder(ARTIST_SORT_ORDER, value);
//    }
//
    public final String getArtistSortOrder() {
        return mPreferences.getString(ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.ARTIST_A_Z);
    }

//    public void setArtistSongSortOrder(final String value) {
//        setSortOrder(ARTIST_SONG_SORT_ORDER, value);
//    }

    public final String getArtistSongSortOrder() {
        return mPreferences.getString(ARTIST_SONG_SORT_ORDER,
                SortOrder.ArtistSongSortOrder.SONG_A_Z);
    }

//    public void setArtistAlbumSortOrder(final String value) {
//        setSortOrder(ARTIST_ALBUM_SORT_ORDER, value);
//    }

    public final String getArtistAlbumSortOrder() {
        return mPreferences.getString(ARTIST_ALBUM_SORT_ORDER,
                SortOrder.ArtistAlbumSortOrder.ALBUM_A_Z);
    }

//    public void setAlbumSortOrder(final String value) {
//        setSortOrder(ALBUM_SORT_ORDER, value);
//    }

    public final String getAlbumSortOrder() {
        return mPreferences.getString(ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_A_Z);
    }

//    public void setAlbumSongSortOrder(final String value) {
//        setSortOrder(ALBUM_SONG_SORT_ORDER, value);
//    }

    public final String getAlbumSongSortOrder() {
        return mPreferences.getString(ALBUM_SONG_SORT_ORDER,
                SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST);
    }

//    public void setSongSortOrder(final String value) {
//        setSortOrder(SONG_SORT_ORDER, value);
//    }

    public final String getSongSortOrder() {
        return mPreferences.getString(SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_A_Z);
    }

    public void setAlbumGridColumns(final int value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(ALBUM_GRID_COLUMNS, value);
        editor.apply();
    }

    public final int getAlbumGridColumns() {
        return mPreferences.getInt(ALBUM_GRID_COLUMNS, 2);
    }

    public void setAlbumGridColumnsLand(final int value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(ALBUM_GRID_COLUMNS_LAND, value);
        editor.apply();
    }

    public final int getAlbumGridColumnsLand() {
        return mPreferences.getInt(ALBUM_GRID_COLUMNS_LAND, 3);
    }
}
