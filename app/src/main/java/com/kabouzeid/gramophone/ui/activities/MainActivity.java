package com.kabouzeid.gramophone.ui.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.AppBarLayout.OnOffsetChangedListener;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.ThemeSingleton;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.PagerAdapter;
import com.kabouzeid.gramophone.dialogs.AboutDialog;
import com.kabouzeid.gramophone.dialogs.CreatePlaylistDialog;
import com.kabouzeid.gramophone.dialogs.SleepTimerDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.SearchQueryHelper;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.loader.AlbumSongLoader;
import com.kabouzeid.gramophone.loader.ArtistSongLoader;
import com.kabouzeid.gramophone.loader.PlaylistSongLoader;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.kabouzeid.gramophone.ui.fragments.mainactivityfragments.AbsMainActivityFragment;
import com.kabouzeid.gramophone.ui.fragments.mainactivityfragments.AbsMainActivityRecyclerViewLayoutModeFragment;
import com.kabouzeid.gramophone.ui.fragments.mainactivityfragments.AlbumViewFragment;
import com.kabouzeid.gramophone.ui.fragments.mainactivityfragments.ArtistViewFragment;
import com.kabouzeid.gramophone.ui.fragments.mainactivityfragments.PlaylistViewFragment;
import com.kabouzeid.gramophone.ui.fragments.mainactivityfragments.SongViewFragment;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AbsSlidingMusicPanelActivity
        implements KabViewsDisableAble, CabHolder {

    public static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.tabs)
    TabLayout tabs;
    @Bind(R.id.appbar)
    AppBarLayout appbar;
    @Bind(R.id.pager)
    ViewPager pager;
    @Bind(R.id.navigation_view)
    NavigationView navigationView;
    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    private ActionBarDrawerToggle drawerToggle;
    private PagerAdapter pagerAdapter;
    private MaterialCab cab;
    @Nullable
    private View navigationDrawerHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setUpDrawerLayout();
        setUpToolbar();
        setUpViewPager();

        if (shouldColorNavigationBar())
            setNavigationBarThemeColor();
    }

    private void setUpViewPager() {
        pagerAdapter = new PagerAdapter(this, getSupportFragmentManager());
        final PagerAdapter.MusicFragments[] fragments = PagerAdapter.MusicFragments.values();
        for (final PagerAdapter.MusicFragments fragment : fragments) {
            pagerAdapter.add(fragment.getFragmentClass(), null);
        }

        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(pagerAdapter.getCount() - 1);

        int startPosition = PreferenceUtil.getInstance(this).getDefaultStartPage();
        startPosition = startPosition == -1 ? PreferenceUtil.getInstance(this).getLastStartPage() : startPosition;

        navigationView.getMenu().getItem(startPosition).setChecked(true);

        tabs.setupWithViewPager(pager);
        setUpTabStripColor();

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                navigationView.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        pager.setCurrentItem(startPosition);
    }

    private void setUpTabStripColor() {
        // use reflection to set the selected indicator color
        try {
            Field tabStripField = tabs.getClass().getDeclaredField("mTabStrip");
            tabStripField.setAccessible(true);
            Object tabStrip = tabStripField.get(tabs);

            Method setSelectedIndicatorColorMethod = tabStrip.getClass().getDeclaredMethod("setSelectedIndicatorColor", int.class);
            setSelectedIndicatorColorMethod.setAccessible(true);
            setSelectedIndicatorColorMethod.invoke(tabStrip, ThemeSingleton.get().positiveColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpToolbar() {
        setTitle(getResources().getString(R.string.app_name));
        setAppBarColor();
        setSupportActionBar(toolbar);
        setUpDrawerToggle();
    }

    private void setAppBarColor() {
        appbar.setBackgroundColor(getThemeColorPrimary());
    }

    private void setUpNavigationView() {
        final int colorAccent = ThemeSingleton.get().positiveColor;
        navigationView.setItemTextColor(new ColorStateList(
                new int[][]{
                        //{-android.R.attr.state_enabled}, // disabled
                        {android.R.attr.state_checked}, // checked
                        {} // default
                },
                new int[]{
                        // 0,
                        colorAccent,
                        ThemeSingleton.get().darkTheme ? Color.argb(222, 255, 255, 255) : Color.argb(222, 0, 0, 0)
                }
        ));
        navigationView.setItemIconTintList(new ColorStateList(
                new int[][]{
                        //{-android.R.attr.state_enabled}, // disabled
                        {android.R.attr.state_checked}, // checked
                        {} // default
                },
                new int[]{
                        // 0,
                        colorAccent,
                        ThemeSingleton.get().darkTheme ? Color.argb(138, 255, 255, 255) : Color.argb(138, 0, 0, 0)
                }
        ));
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.nav_songs:
                        menuItem.setChecked(true);
                        pager.setCurrentItem(PagerAdapter.MusicFragments.SONG.ordinal(), true);
                        break;
                    case R.id.nav_albums:
                        menuItem.setChecked(true);
                        pager.setCurrentItem(PagerAdapter.MusicFragments.ALBUM.ordinal(), true);
                        break;
                    case R.id.nav_artists:
                        menuItem.setChecked(true);
                        pager.setCurrentItem(PagerAdapter.MusicFragments.ARTIST.ordinal(), true);
                        break;
                    case R.id.nav_playlists:
                        menuItem.setChecked(true);
                        pager.setCurrentItem(PagerAdapter.MusicFragments.PLAYLIST.ordinal(), true);
                        break;
                    case R.id.nav_settings:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                            }
                        }, 200);
                        break;
                    case R.id.nav_about:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                new AboutDialog().show(getSupportFragmentManager(), "ABOUT_DIALOG");
                            }
                        }, 200);
                        break;
                }
                return true;
            }
        });
    }

    private void setUpDrawerToggle() {
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });
        drawerLayout.setDrawerListener(drawerToggle);
    }

    private void setUpDrawerLayout() {
        drawerLayout.setStatusBarBackgroundColor(getThemeColorPrimaryDarker());
        setUpNavigationView();
    }

    private void updateNavigationDrawerHeader() {
        Song song = MusicPlayerRemote.getCurrentSong();
        if (song.id != -1) {
            if (navigationDrawerHeader == null) {
                navigationDrawerHeader = navigationView.inflateHeaderView(R.layout.navigation_drawer_header);
                //noinspection ConstantConditions
                navigationDrawerHeader.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //noinspection ConstantConditions
                        NavigationUtil.openCurrentPlayingIfPossible(MainActivity.this, getSharedViewsWithPlayPauseFab(new Pair[]{
                                Pair.create(navigationDrawerHeader.findViewById(R.id.image),
                                        getResources().getString(R.string.transition_album_art)
                                )
                        }));
                    }
                });
            }
            ((TextView) navigationDrawerHeader.findViewById(R.id.title)).setText(song.title);
            ((TextView) navigationDrawerHeader.findViewById(R.id.text)).setText(song.artistName);
            ImageLoader.getInstance().displayImage(
                    MusicUtil.getSongImageLoaderString(song),
                    ((ImageView) navigationDrawerHeader.findViewById(R.id.image)),
                    new DisplayImageOptions.Builder()
                            .cacheInMemory(true)
                            .showImageOnFail(R.drawable.default_album_art)
                            .resetViewBeforeLoading(true)
                            .build()
            );
        } else {
            if (navigationDrawerHeader != null) {
                navigationView.removeHeaderView(navigationDrawerHeader);
                navigationDrawerHeader = null;
            }
        }
    }

    @Override
    public void enableViews() {
        try {
            super.enableViews();
            toolbar.setEnabled(true);
            ((AbsMainActivityFragment) getCurrentFragment()).enableViews();
        } catch (NullPointerException ignored) {
        }
    }

    @Override
    public void disableViews() {
        try {
            super.disableViews();
            ((AbsMainActivityFragment) getCurrentFragment()).disableViews();
        } catch (NullPointerException ignored) {
        }
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        updateNavigationDrawerHeader();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        handlePlaybackIntent(getIntent());
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        //noinspection ConstantConditions
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (isPlaylistPage()) {
            menu.add(0, R.id.action_new_playlist, 0, R.string.new_playlist_title);
        }
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof AbsMainActivityRecyclerViewLayoutModeFragment) {
            setUpLayoutModeMenu((AbsMainActivityRecyclerViewLayoutModeFragment) currentFragment, menu);
        } else {
            menu.removeItem(R.id.action_view_as);
        }
        restoreActionBar();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ViewUtil.invalidateToolbarPopupMenuTint(toolbar);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        ViewUtil.invalidateToolbarPopupMenuTint(toolbar);

        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof AbsMainActivityRecyclerViewLayoutModeFragment) {
            if (handleLayoutModeMenuItem((AbsMainActivityRecyclerViewLayoutModeFragment) currentFragment, item))
                return true;
        }

        int id = item.getItemId();
        switch (id) {
            case R.id.action_sleep_timer:
                new SleepTimerDialog().show(getSupportFragmentManager(), "SET_SLEEP_TIMER");
                return true;
            case R.id.action_equalizer:
                NavigationUtil.openEqualizer(this);
                return true;
            case R.id.action_shuffle_all:
                MusicPlayerRemote.openAndShuffleQueue(SongLoader.getAllSongs(this), true);
                return true;
            case R.id.action_new_playlist:
                CreatePlaylistDialog.create().show(getSupportFragmentManager(), "CREATE_PLAYLIST");
                return true;
            case R.id.action_search:
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                return true;
            case R.id.action_now_playing:
                NavigationUtil.openCurrentPlayingIfPossible(this, getSharedViewsWithPlayPauseFab(null));
                return true;
            case R.id.action_playing_queue:
                NavigationUtil.openPlayingQueueDialog(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpLayoutModeMenu(@NonNull AbsMainActivityRecyclerViewLayoutModeFragment fragment, @NonNull Menu menu) {
        SubMenu layoutModeMenu = menu.findItem(R.id.action_view_as).getSubMenu();

        switch (fragment.getLayoutMode()) {
            case PreferenceUtil.LAYOUT_MODE_LIST:
                layoutModeMenu.findItem(R.id.action_layout_mode_list).setChecked(true);
                layoutModeMenu.findItem(R.id.action_colored_footers).setEnabled(false);
                break;
            case PreferenceUtil.LAYOUT_MODE_GRID:
                layoutModeMenu.findItem(R.id.action_layout_mode_grid).setChecked(true);
                layoutModeMenu.findItem(R.id.action_colored_footers).setEnabled(true);
                break;
        }

        layoutModeMenu.findItem(R.id.action_colored_footers).setChecked(fragment.loadUsePalette());
    }

    private boolean handleLayoutModeMenuItem(AbsMainActivityRecyclerViewLayoutModeFragment fragment, @NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_colored_footers) {
            item.setChecked(!item.isChecked());
            fragment.setUsePaletteAndSaveValue(item.isChecked());
            return true;
        } else {
            switch (item.getItemId()) {
                case R.id.action_layout_mode_list:
                    item.setChecked(true);
                    fragment.setLayoutModeAndSaveValue(PreferenceUtil.LAYOUT_MODE_LIST);
                    toolbar.getMenu().findItem(R.id.action_colored_footers).setEnabled(false);
                    return true;
                case R.id.action_layout_mode_grid:
                    item.setChecked(true);
                    fragment.setLayoutModeAndSaveValue(PreferenceUtil.LAYOUT_MODE_GRID);
                    toolbar.getMenu().findItem(R.id.action_colored_footers).setEnabled(true);
                    return true;
            }
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        drawerToggle.onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) drawerLayout.closeDrawers();
        else if (cab != null && cab.isActive()) cab.finish();
        else super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceUtil.getInstance(MainActivity.this).setLastStartPage(pager.getCurrentItem());
    }

    private void handlePlaybackIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        Uri uri = intent.getData();
        String mimeType = intent.getType();
        boolean handled = false;

        if (intent.getAction() != null
                && intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            MusicPlayerRemote.openQueue(SearchQueryHelper.getSongs(this, intent.getExtras()), 0, true);
        }
        if (uri != null && uri.toString().length() > 0) {
            MusicPlayerRemote.playFile(new File(uri.getPath()).getAbsolutePath());
            handled = true;
        } else if (MediaStore.Audio.Playlists.CONTENT_TYPE.equals(mimeType)) {
            final int id = (int) parseIdFromIntent(intent, "playlistId", "playlist");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                ArrayList<Song> songs = new ArrayList<>();
                songs.addAll(PlaylistSongLoader.getPlaylistSongList(this, id));
                MusicPlayerRemote.openQueue(songs, position, true);
                handled = true;
            }
        } else if (MediaStore.Audio.Albums.CONTENT_TYPE.equals(mimeType)) {
            final int id = (int) parseIdFromIntent(intent, "albumId", "album");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                MusicPlayerRemote.openQueue(AlbumSongLoader.getAlbumSongList(this, id), position, true);
                handled = true;
            }
        } else if (MediaStore.Audio.Artists.CONTENT_TYPE.equals(mimeType)) {
            final int id = (int) parseIdFromIntent(intent, "artistId", "artist");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                MusicPlayerRemote.openQueue(ArtistSongLoader.getArtistSongList(this, id), position, true);
                handled = true;
            }
        }
        if (handled) {
            setIntent(new Intent());
        }
    }

    private long parseIdFromIntent(@NonNull Intent intent, String longKey,
                                   String stringKey) {
        long id = intent.getLongExtra(longKey, -1);
        if (id < 0) {
            String idString = intent.getStringExtra(stringKey);
            if (idString != null) {
                try {
                    id = Long.parseLong(idString);
                } catch (NumberFormatException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        return id;
    }

    public Fragment getCurrentFragment() {
        return pagerAdapter.getFragment(pager.getCurrentItem());
    }

    private boolean isArtistPage() {
        return pager.getCurrentItem() == PagerAdapter.MusicFragments.ARTIST.ordinal();
    }

    public ArtistViewFragment getArtistFragment() {
        return (ArtistViewFragment) pagerAdapter.getFragment(PagerAdapter.MusicFragments.ARTIST.ordinal());
    }

    private boolean isAlbumPage() {
        return pager.getCurrentItem() == PagerAdapter.MusicFragments.ALBUM.ordinal();
    }

    @NonNull
    public AlbumViewFragment getAlbumFragment() {
        return (AlbumViewFragment) pagerAdapter.getFragment(PagerAdapter.MusicFragments.ALBUM.ordinal());
    }

    private boolean isSongPage() {
        return pager.getCurrentItem() == PagerAdapter.MusicFragments.SONG.ordinal();
    }

    public SongViewFragment getSongFragment() {
        return (SongViewFragment) pagerAdapter.getFragment(PagerAdapter.MusicFragments.SONG.ordinal());
    }

    private boolean isPlaylistPage() {
        return pager.getCurrentItem() == PagerAdapter.MusicFragments.PLAYLIST.ordinal();
    }

    public PlaylistViewFragment getPlaylistFragment() {
        return (PlaylistViewFragment) pagerAdapter.getFragment(PagerAdapter.MusicFragments.PLAYLIST.ordinal());
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_UP) {
            if (toolbar != null)
                toolbar.showOverflowMenu();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public MaterialCab openCab(final int menu, final MaterialCab.Callback callback) {
        if (cab != null && cab.isActive()) cab.finish();
        cab = new MaterialCab(this, R.id.cab_stub)
                .setMenu(menu)
                .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
                .setBackgroundColor(getThemeColorPrimary())
                .start(callback);
        return cab;
    }

    public void addOnAppBarOffsetChangedListener(OnOffsetChangedListener onOffsetChangedListener) {
        appbar.addOnOffsetChangedListener(onOffsetChangedListener);
    }

    public void removeOnAppBarOffsetChangedListener(OnOffsetChangedListener onOffsetChangedListener) {
        appbar.removeOnOffsetChangedListener(onOffsetChangedListener);
    }

    public int getTotalAppBarScrollingRange() {
        return appbar.getTotalScrollRange();
    }
}