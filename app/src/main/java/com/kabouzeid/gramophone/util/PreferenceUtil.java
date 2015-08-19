package com.kabouzeid.gramophone.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.SortOrder;

public final class PreferenceUtil {
    public static final String GENERAL_THEME = "general_theme";
    public static final String DEFAULT_START_PAGE = "default_start_page";
    public static final String LAST_START_PAGE = "last_start_page";

    public static final String ARTIST_SORT_ORDER = "artist_sort_order";
    public static final String ARTIST_SONG_SORT_ORDER = "artist_song_sort_order";
    public static final String ARTIST_ALBUM_SORT_ORDER = "artist_album_sort_order";
    public static final String ALBUM_SORT_ORDER = "album_sort_order";
    public static final String ALBUM_SONG_SORT_ORDER = "album_song_sort_order";
    public static final String SONG_SORT_ORDER = "song_sort_order";

    // don't use "colored_navigation_bar" key here as this causes a class cast exception for users upgrading from older versions
    public static final String COLORED_NAVIGATION_BAR = "should_color_navigation_bar";

    public static final String ALBUM_LAYOUT_MODE = "album_layout_mode";
    public static final String ALBUM_COLORED_FOOTERS = "album_colored_footers";
    public static final String SONG_LAYOUT_MODE = "song_layout_mode";
    public static final String SONG_COLORED_FOOTERS = "song_colored_footers";
    public static final String ARTIST_LAYOUT_MODE = "artist_layout_mode";
    public static final String ARTIST_COLORED_FOOTERS = "artist_colored_footers";

    public static final String OPAQUE_TOOLBAR_NOW_PLAYING = "opaque_toolbar_now_playing";
    public static final String OPAQUE_STATUSBAR_NOW_PLAYING = "opaque_statusbar_now_playing";
    public static final String FORCE_SQUARE_ALBUM_ART = "force_square_album_art";
    public static final String LARGER_TITLE_BOX_NOW_PLAYING = "larger_title_box_now_playing";
    public static final String ALTERNATIVE_PROGRESS_SLIDER_NOW_PLAYING = "alternative_progress_slider_now_playing";
    public static final String PLAYBACK_CONTROLLER_CARD_NOW_PLAYING = "playback_controller_card_now_playing";
    public static final String COLOR_PLAYBACK_CONTROLS_NOW_PLAYING = "color_playback_controls_now_playing";

    public static final String COLORED_NOTIFICATION = "colored_notification";

    public static final String GAPLESS_PLAYBACK = "gapless_playback";

    public static final String LAST_ADDED_CUTOFF_TIMESTAMP = "last_added_cutoff_timestamp";

    public static final String ALBUM_ART_ON_LOCKSCREEN = "album_art_on_lockscreen";

    public static final String LAST_SLEEP_TIMER_VALUE = "last_sleep_timer_value";
    public static final String NEXT_SLEEP_TIMER_ELAPSED_REALTIME = "next_sleep_timer_elapsed_real_time";

    public static final String IGNORE_MEDIA_STORE_ARTWORK = "ignore_media_store_artwork";

    public static final String HIDE_BOTTOM_BAR = "hide_bottom_bar";

    public static final String LAST_CHANGELOG_VERSION = "last_changelog_version";

    private static PreferenceUtil sInstance;

    private final SharedPreferences mPreferences;

    public PreferenceUtil(@NonNull final Context context) {
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

    @SuppressLint("CommitPrefEdits")
    public void setGeneralTheme(Context context, String value) {
        String[] allowedValues = context.getResources().getStringArray(R.array.pref_general_theme_list_values);
        for (String allowedValue : allowedValues) {
            if (value.equals(allowedValue)) {
                mPreferences.edit().putString(GENERAL_THEME, value).commit();
                return;
            }
        }
    }

    public int getGeneralTheme() {
        try {
            int value = Integer.parseInt(mPreferences.getString(GENERAL_THEME, "0"));
            switch (value) {
                case 0:
                    return R.style.Theme_MaterialMusic_Light;
                case 1:
                    return R.style.Theme_MaterialMusic;
            }
        } catch (NumberFormatException ignored) {
        }

        return R.style.Theme_MaterialMusic_Light;
    }

    public int getThemeColorPrimary(Context context) {
        return mPreferences.getInt("primary_color", ContextCompat.getColor(context, R.color.indigo_500));
    }

    @SuppressLint("CommitPrefEdits")
    public void setThemeColorPrimary(int color) {
        mPreferences.edit().putInt("primary_color", color).commit();
    }

    public int getThemeColorAccent(Context context) {
        return mPreferences.getInt("accent_color", ContextCompat.getColor(context, R.color.pink_A200));
    }

    @SuppressLint("CommitPrefEdits")
    public void setThemeColorAccent(int color) {
        mPreferences.edit().putInt("accent_color", color).commit();
    }

    public final boolean shouldUseColoredNavigationBar() {
        return mPreferences.getBoolean(COLORED_NAVIGATION_BAR, false);
    }

    @SuppressLint("CommitPrefEdits")
    public void setColoredNavigationBar(boolean coloredNavigationBar) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(COLORED_NAVIGATION_BAR, coloredNavigationBar);
        editor.commit();
    }

    public final int getDefaultStartPage() {
        return Integer.parseInt(mPreferences.getString(DEFAULT_START_PAGE, "-1"));
    }

    public void setLastStartPage(final int value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(LAST_START_PAGE, value);
        editor.apply();
    }

    public static final int DEFAULT_PAGE = 0;

    public final int getLastStartPage() {
        return mPreferences.getInt(LAST_START_PAGE, DEFAULT_PAGE);
    }

    public final boolean coloredNotification() {
        return mPreferences.getBoolean(COLORED_NOTIFICATION, true);
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

    public final boolean colorPlaybackControlsNowPlaying() {
        return mPreferences.getBoolean(COLOR_PLAYBACK_CONTROLS_NOW_PLAYING, true);
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

    public final boolean hideBottomBar() {
        return mPreferences.getBoolean(HIDE_BOTTOM_BAR, false);
    }

    public final String getArtistSortOrder() {
        return mPreferences.getString(ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.ARTIST_A_Z);
    }

    public final String getArtistSongSortOrder() {
        return mPreferences.getString(ARTIST_SONG_SORT_ORDER,
                SortOrder.ArtistSongSortOrder.SONG_A_Z);
    }

    public final String getArtistAlbumSortOrder() {
        return mPreferences.getString(ARTIST_ALBUM_SORT_ORDER,
                SortOrder.ArtistAlbumSortOrder.ALBUM_YEAR_ASC);
    }

    public final String getAlbumSortOrder() {
        return mPreferences.getString(ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_A_Z);
    }

    public final String getAlbumSongSortOrder() {
        return mPreferences.getString(ALBUM_SONG_SORT_ORDER,
                SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST);
    }

    public final String getSongSortOrder() {
        return mPreferences.getString(SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_A_Z);
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

    public static final int LAYOUT_MODE_LIST = 0;
    public static final int LAYOUT_MODE_GRID = 1;

    public void setAlbumLayoutMode(final int value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(ALBUM_LAYOUT_MODE, value);
        editor.apply();
    }

    public final int getAlbumLayoutMode() {
        return mPreferences.getInt(ALBUM_LAYOUT_MODE, LAYOUT_MODE_GRID);
    }

    public void setSongLayoutMode(final int value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(SONG_LAYOUT_MODE, value);
        editor.apply();
    }

    public final int getSongLayoutMode() {
        return mPreferences.getInt(SONG_LAYOUT_MODE, LAYOUT_MODE_LIST);
    }

    public void setArtistLayoutMode(final int value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(ARTIST_LAYOUT_MODE, value);
        editor.apply();
    }

    public final int getArtistLayoutMode() {
        return mPreferences.getInt(ARTIST_LAYOUT_MODE, LAYOUT_MODE_GRID);
    }

    public void setAlbumColoredFooters(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(ALBUM_COLORED_FOOTERS, value);
        editor.apply();
    }

    public final boolean albumColoredFooters() {
        return mPreferences.getBoolean(ALBUM_COLORED_FOOTERS, true);
    }

    public void setSongColoredFooters(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(SONG_COLORED_FOOTERS, value);
        editor.apply();
    }

    public final boolean songColoredFooters() {
        return mPreferences.getBoolean(SONG_COLORED_FOOTERS, true);
    }

    public void setArtistColoredFooters(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(ARTIST_COLORED_FOOTERS, value);
        editor.apply();
    }

    public final boolean artistColoredFooters() {
        return mPreferences.getBoolean(ARTIST_COLORED_FOOTERS, true);
    }

    public void setLastChangeLogVersion(int version) {
        mPreferences.edit().putInt(LAST_CHANGELOG_VERSION, version).apply();
    }

    public final int getLastChangelogVersion() {
        return mPreferences.getInt(LAST_CHANGELOG_VERSION, -1);
    }
}
