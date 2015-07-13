package com.kabouzeid.gramophone.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.SortOrder;

import java.util.HashSet;
import java.util.Set;

public final class PreferenceUtil {

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
    public static final String AUTO_DOWNLOAD_ARTIST_IMAGES_ONLY_ON_WIFI = "auto_download_artist_images_only_on_wifi";
    public static final String COLORED_ALBUM_FOOTERS = "colored_album_footers";
    public static final String COLORED_NAVIGATION_BAR = "colored_navigation_bar";
    public static final String COLORED_NAVIGATION_BAR_ALBUM = "colored_navigation_bar_album";
    public static final String COLORED_NAVIGATION_BAR_ARTIST = "colored_navigation_bar_artist";
    public static final String COLORED_NAVIGATION_BAR_CURRENT_PLAYING = "colored_navigation_bar_current_playing";
    public static final String COLORED_NAVIGATION_BAR_PLAYIST = "colored_navigation_bar_playlist";
    public static final String COLORED_NAVIGATION_BAR_TAG_EDITOR = "colored_navigation_bar_tag_editor";
    public static final String COLORED_NAVIGATION_BAR_OTHER_SCREENS = "colored_navigation_bar_other_screens";
    public static final String ALBUM_GRID_COLUMNS = "album_grid_columns";
    public static final String ALBUM_GRID_COLUMNS_LAND = "album_grid_columns_land";
    public static final String OPAQUE_TOOLBAR_NOW_PLAYING = "opaque_toolbar_now_playing";
    public static final String OPAQUE_STATUSBAR_NOW_PLAYING = "opaque_statusbar_now_playing";
    public static final String FORCE_SQUARE_ALBUM_ART = "force_square_album_art";
    public static final String LARGER_TITLE_BOX_NOW_PLAYING = "larger_title_box_now_playing";
    public static final String ALTERNATIVE_PROGRESS_SLIDER_NOW_PLAYING = "alternative_progress_slider_now_playing";
    public static final String PLAYBACK_CONTROLLER_CARD_NOW_PLAYING = "playback_controller_card_now_playing";
    public static final String COLORED_NOTIFICATION = "colored_notification";
    public static final String GAPLESS_PLAYBACK = "gapless_playback";
    public static final String LAST_ADDED_CUTOFF_TIMESTAMP = "last_added_cutoff_timestamp";
    public static final String ALBUM_ART_ON_LOCKSCREEN = "album_art_on_lockscreen";
    public static final String LAST_SLEEP_TIMER_VALUE = "last_sleep_timer_value";
    public static final String NEXT_SLEEP_TIMER_ELAPSED_REALTIME = "next_sleep_timer_elapsed_real_time";
    public static final String IGNORE_MEDIA_STORE_ARTWORK = "ignore_media_store_artwork";

    private static PreferenceUtil sInstance;

    @NonNull
    private final Context mContext;
    private final SharedPreferences mPreferences;

    public PreferenceUtil(@NonNull final Context context) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferenceUtil getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceUtil(context.getApplicationContext());
        }
        return sInstance;
    }

    public void registerOnSharedPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener) {
        mPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    public void unregisterOnSharedPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener) {
        mPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
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
        return ColorUtil.shiftColorDown(getThemeColorPrimary());
    }

    @SuppressLint("CommitPrefEdits")
    public void setThemeColorPrimary(int color) {
        mPreferences.edit().putInt("primary_color", color).commit();
    }

    public int getThemeColorAccent() {
        return mPreferences.getInt("accent_color", mContext.getResources().getColor(R.color.pink_A200));
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

    public final boolean autoDownloadArtistImagesOnlyOnWifi() {
        return mPreferences.getBoolean(AUTO_DOWNLOAD_ARTIST_IMAGES_ONLY_ON_WIFI, false);
    }

    public final boolean coloredAlbumFooters() {
        return mPreferences.getBoolean(COLORED_ALBUM_FOOTERS, true);
    }

    public final boolean coloredNotification() {
        return mPreferences.getBoolean(COLORED_NOTIFICATION, false);
    }

    public final boolean coloredNavigationBarAlbum() {
        return coloredNavigationBarFor(COLORED_NAVIGATION_BAR_ALBUM);
    }

    public final boolean coloredNavigationBarArtist() {
        return coloredNavigationBarFor(COLORED_NAVIGATION_BAR_ARTIST);
    }

    public final boolean coloredNavigationBarCurrentPlaying() {
        return coloredNavigationBarFor(COLORED_NAVIGATION_BAR_CURRENT_PLAYING);
    }

    public final boolean coloredNavigationBarPlaylist() {
        return coloredNavigationBarFor(COLORED_NAVIGATION_BAR_PLAYIST);
    }

    public final boolean coloredNavigationBarTagEditor() {
        return coloredNavigationBarFor(COLORED_NAVIGATION_BAR_TAG_EDITOR);
    }

    public final boolean coloredNavigationBarOtherScreens() {
        return coloredNavigationBarFor(COLORED_NAVIGATION_BAR_OTHER_SCREENS);
    }

    @SuppressLint("CommitPrefEdits")
    private boolean coloredNavigationBarFor(String key) {
        final Set<String> defaultVals = new HashSet<>();
        defaultVals.add(COLORED_NAVIGATION_BAR_ALBUM);
        defaultVals.add(COLORED_NAVIGATION_BAR_ARTIST);
        defaultVals.add(COLORED_NAVIGATION_BAR_CURRENT_PLAYING);
        defaultVals.add(COLORED_NAVIGATION_BAR_PLAYIST);
        defaultVals.add(COLORED_NAVIGATION_BAR_TAG_EDITOR);
        defaultVals.add(COLORED_NAVIGATION_BAR_OTHER_SCREENS);

        if (!mPreferences.contains(COLORED_NAVIGATION_BAR))
            mPreferences.edit().putStringSet(COLORED_NAVIGATION_BAR, defaultVals).commit();

        try {
            //noinspection ConstantConditions
            return mPreferences.getStringSet(COLORED_NAVIGATION_BAR, defaultVals).contains(key);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public final boolean opaqueStatusbarNowPlaying() {
        return mPreferences.getBoolean(OPAQUE_STATUSBAR_NOW_PLAYING, false);
    }

    public final boolean opaqueToolbarNowPlaying() {
        return mPreferences.getBoolean(OPAQUE_TOOLBAR_NOW_PLAYING, false);
    }

    public final boolean forceAlbumArtSquared() {
        return mPreferences.getBoolean(FORCE_SQUARE_ALBUM_ART, false);
    }

    public final boolean playbackControllerCardNowPlaying() {
        return mPreferences.getBoolean(PLAYBACK_CONTROLLER_CARD_NOW_PLAYING, false);
    }

    public final boolean largerTitleBoxNowPlaying() {
        return mPreferences.getBoolean(LARGER_TITLE_BOX_NOW_PLAYING, false);
    }

    public final boolean alternativeProgressSliderNowPlaying() {
        return mPreferences.getBoolean(ALTERNATIVE_PROGRESS_SLIDER_NOW_PLAYING, false);
    }

    public final boolean gaplessPlayback() {
        return mPreferences.getBoolean(GAPLESS_PLAYBACK, false);
    }

    public final boolean albumArtOnLockscreen() {
        return mPreferences.getBoolean(ALBUM_ART_ON_LOCKSCREEN, true);
    }

    public final boolean ignoreMediaStoreArtwork() {
        return mPreferences.getBoolean(IGNORE_MEDIA_STORE_ARTWORK, false);
    }

    @Nullable
    public final String getArtistSortOrder() {
        return mPreferences.getString(ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.ARTIST_A_Z);
    }

    @Nullable
    public final String getArtistSongSortOrder() {
        return mPreferences.getString(ARTIST_SONG_SORT_ORDER,
                SortOrder.ArtistSongSortOrder.SONG_A_Z);
    }

    @Nullable
    public final String getArtistAlbumSortOrder() {
        return mPreferences.getString(ARTIST_ALBUM_SORT_ORDER,
                SortOrder.ArtistAlbumSortOrder.ALBUM_YEAR_ASC);
    }

    @Nullable
    public final String getAlbumSortOrder() {
        return mPreferences.getString(ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_A_Z);
    }

    @Nullable
    public final String getAlbumSongSortOrder() {
        return mPreferences.getString(ALBUM_SONG_SORT_ORDER,
                SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST);
    }

    @Nullable
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

    public long getLastAddedCutOffTimestamp() {
        return mPreferences.getLong(LAST_ADDED_CUTOFF_TIMESTAMP, 0L);
    }

    @SuppressLint("CommitPrefEdits")
    public void setLastAddedCutoffTimestamp(final long timestamp) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(LAST_ADDED_CUTOFF_TIMESTAMP, timestamp);
        editor.commit();
    }

    public int getLastSleepTimerValue() {
        return mPreferences.getInt(LAST_SLEEP_TIMER_VALUE, 30);
    }

    public void setLastSleepTimerValue(final int value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(LAST_SLEEP_TIMER_VALUE, value);
        editor.apply();
    }

    public long getNextSleepTimerElapsedRealTime() {
        return mPreferences.getLong(NEXT_SLEEP_TIMER_ELAPSED_REALTIME, -1);
    }

    public void setNextSleepTimerElapsedRealtime(final long value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(NEXT_SLEEP_TIMER_ELAPSED_REALTIME, value);
        editor.apply();
    }
}
