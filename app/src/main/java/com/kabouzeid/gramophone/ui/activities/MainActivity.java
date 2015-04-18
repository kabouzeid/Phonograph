package com.kabouzeid.gramophone.ui.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.internal.view.menu.MenuPopupHelper;
import android.support.v7.widget.ActionMenuPresenter;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.afollestad.materialdialogs.ThemeSingleton;
import com.astuetz.PagerSlidingTabStrip;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.PagerAdapter;
import com.kabouzeid.gramophone.dialogs.AboutDialog;
import com.kabouzeid.gramophone.dialogs.CreatePlaylistDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.loader.AlbumSongLoader;
import com.kabouzeid.gramophone.loader.ArtistSongLoader;
import com.kabouzeid.gramophone.loader.PlaylistSongLoader;
import com.kabouzeid.gramophone.model.MusicRemoteEvent;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.model.UIPreferenceChangedEvent;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.ui.fragments.NavigationDrawerFragment;
import com.kabouzeid.gramophone.ui.fragments.mainactivityfragments.AbsMainActivityFragment;
import com.kabouzeid.gramophone.ui.fragments.mainactivityfragments.AlbumViewFragment;
import com.kabouzeid.gramophone.ui.fragments.mainactivityfragments.ArtistViewFragment;
import com.kabouzeid.gramophone.ui.fragments.mainactivityfragments.PlaylistViewFragment;
import com.kabouzeid.gramophone.ui.fragments.mainactivityfragments.SongViewFragment;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AbsFabActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, KabViewsDisableAble {

    public static final String TAG = MainActivity.class.getSimpleName();
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationDrawerFragment navigationDrawerFragment;
    private Toolbar toolbar;
    private View statusBar;
    private PagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private PagerSlidingTabStrip slidingTabLayout;
    private int currentPage = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUpTranslucence(true, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        navigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                drawerLayout
        );
        setUpToolBar();
        setUpViewPager();

        handlePlaybackIntent(getIntent());
    }

    @Override
    protected boolean shouldColorStatusBar() {
        return false;
    }

    @Override
    protected boolean shouldColorNavBar() {
        return false;
    }

    private void setUpViewPager() {
        pagerAdapter = new PagerAdapter(this, getSupportFragmentManager());
        final PagerAdapter.MusicFragments[] fragments = PagerAdapter.MusicFragments.values();
        for (final PagerAdapter.MusicFragments fragment : fragments) {
            pagerAdapter.add(fragment.getFragmentClass(), null);
        }

        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(pagerAdapter.getCount() - 1);

        int startPosition = PreferenceUtils.getInstance(this).getDefaultStartPage();
        startPosition = startPosition == -1 ? PreferenceUtils.getInstance(this).getLastStartPage() : startPosition;
        currentPage = startPosition;
        viewPager.setCurrentItem(startPosition);

        navigationDrawerFragment.setItemChecked(startPosition);

        slidingTabLayout.setIndicatorColor(ThemeSingleton.get().positiveColor);
        slidingTabLayout.setViewPager(viewPager);

        slidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                navigationDrawerFragment.setItemChecked(position);
                currentPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initViews() {
        viewPager = (ViewPager) findViewById(R.id.pager);
        slidingTabLayout = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
    }

    private void setUpToolBar() {
        setTitle(getResources().getString(R.string.app_name));
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        statusBar = findViewById(R.id.statusBar);
        setToolBarTransparent(PreferenceUtils.getInstance(this).transparentToolbar());
        setSupportActionBar(toolbar);
        setUpDrawerToggle();
    }

    private void setToolBarTransparent(boolean transparent) {
        float alpha = transparent ? 0.97f : 1f;
        final int colorPrimary = PreferenceUtils.getInstance(this).getThemeColorPrimary();
        ViewUtil.setBackgroundAlpha(toolbar, alpha, colorPrimary);
        ViewUtil.setBackgroundAlpha(statusBar, alpha, colorPrimary);
        ViewUtil.setBackgroundAlpha(slidingTabLayout, alpha, colorPrimary);
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

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavigationDrawerHeader();
    }

    private void updateNavigationDrawerHeader() {
        if (navigationDrawerFragment != null) {
            Song song = MusicPlayerRemote.getCurrentSong();
            if (song.id != -1) {
                Ion.with(this)
                        .load(MusicUtil.getAlbumArtUri(song.albumId).toString())
                        .withBitmap()
                        .smartSize(false)
                        .asBitmap()
                        .setCallback(new FutureCallback<Bitmap>() {
                            @Override
                            public void onCompleted(Exception e, Bitmap result) {
                                if (result != null)
                                    navigationDrawerFragment.getAlbumArtImageView().setImageBitmap(result);
                                else
                                    navigationDrawerFragment.getAlbumArtImageView().setImageResource(R.drawable.default_album_art);
                            }
                        });
                navigationDrawerFragment.getSongTitle().setText(song.title);
                navigationDrawerFragment.getSongArtist().setText(song.artistName);
            }
        }
    }

    @Override
    public void enableViews() {
        try {
            super.enableViews();
            toolbar.setEnabled(true);
            ((AbsMainActivityFragment) pagerAdapter.getItem(viewPager.getCurrentItem())).enableViews();
        } catch (NullPointerException e) {
            //Log.e(TAG, "wasn't able to enable the views", e);
        }
    }

    @Override
    public void disableViews() {
        try {
            super.disableViews();
            ((AbsMainActivityFragment) pagerAdapter.getItem(viewPager.getCurrentItem())).disableViews();
        } catch (NullPointerException e) {
            //Log.e(TAG, "wasn't able to disable the views", e);
        }
    }

    @Override
    public void onMusicRemoteEvent(MusicRemoteEvent event) {
        super.onMusicRemoteEvent(event);
        if (event.getAction() == MusicRemoteEvent.STATE_RESTORED || event.getAction() == MusicRemoteEvent.TRACK_CHANGED) {
            updateNavigationDrawerHeader();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if (position == NavigationDrawerFragment.NAVIGATION_DRAWER_HEADER) {
            NavigationUtil.openCurrentPlayingIfPossible(this, getSharedViewsWithFab(new Pair[]{
                    Pair.create(navigationDrawerFragment.getAlbumArtImageView(),
                            getResources().getString(R.string.transition_album_cover)
                    )
            }));
        } else if (position == NavigationDrawerFragment.ABOUT_INDEX) {
            drawerLayout.closeDrawers();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new AboutDialog().show(getSupportFragmentManager(), "ABOUT_DIALOG");
                }
            }, 200);
        } else if (position == NavigationDrawerFragment.SETTINGS_INDEX) {
            drawerLayout.closeDrawers();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                }
            }, 200);
        } else {
            if (viewPager != null) {
                viewPager.setCurrentItem(position, true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isAlbumPage()) {
            getMenuInflater().inflate(R.menu.menu_albums, menu);
            setUpGridMenu(menu);
        } else if (isPlaylistPage()) {
            getMenuInflater().inflate(R.menu.menu_playlists, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        restoreActionBar();
        return true;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (handleGridSize(item)) return true;

        int id = item.getItemId();
        switch (id) {
            case R.id.action_equalizer:
                NavigationUtil.openEqualizer(this);
                return true;
            case R.id.action_shuffle_all:
                MusicPlayerRemote.shuffleAllSongs(this);
                return true;
            case R.id.action_new_playlist:
                CreatePlaylistDialog.create().show(getSupportFragmentManager(), "CREATE_PLAYLIST");
                return true;
            case R.id.action_search:
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                return true;
            case R.id.action_current_playing:
                NavigationUtil.openCurrentPlayingIfPossible(this, getSharedViewsWithFab(null));
                return true;
            case R.id.action_playing_queue:
                NavigationUtil.openPlayingQueueDialog(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        drawerToggle.onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onUIPreferenceChangedEvent(UIPreferenceChangedEvent event) {
        super.onUIPreferenceChangedEvent(event);
        switch (event.getAction()) {
            case UIPreferenceChangedEvent.TOOLBAR_TRANSPARENT_CHANGED:
                setToolBarTransparent((boolean) event.getValue());
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (navigationDrawerFragment.isDrawerOpen()) {
            drawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceUtils.getInstance(MainActivity.this).setLastStartPage(currentPage);
    }

    private void handlePlaybackIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        Uri uri = intent.getData();
        String mimeType = intent.getType();
        boolean handled = false;

        if (uri != null && uri.toString().length() > 0) {
            MusicPlayerRemote.playFile(uri);
            handled = true;
        } else if (MediaStore.Audio.Playlists.CONTENT_TYPE.equals(mimeType)) {
            final int id = (int) parseIdFromIntent(intent, "playlistId", "playlist");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                //noinspection unchecked
                MusicPlayerRemote.openQueue((ArrayList<Song>) (List<? extends Song>) PlaylistSongLoader.getPlaylistSongList(this, id), position, true);
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

    private long parseIdFromIntent(Intent intent, String longKey,
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

    private boolean isArtistPage() {
        return viewPager.getCurrentItem() == PagerAdapter.MusicFragments.ARTIST.ordinal();
    }

    public ArtistViewFragment getArtistFragment() {
        return (ArtistViewFragment) pagerAdapter.getFragment(PagerAdapter.MusicFragments.ARTIST.ordinal());
    }

    private boolean isAlbumPage() {
        return viewPager.getCurrentItem() == PagerAdapter.MusicFragments.ALBUM.ordinal();
    }

    public AlbumViewFragment getAlbumFragment() {
        return (AlbumViewFragment) pagerAdapter.getFragment(PagerAdapter.MusicFragments.ALBUM.ordinal());
    }

    private boolean isSongPage() {
        return viewPager.getCurrentItem() == PagerAdapter.MusicFragments.SONG.ordinal();
    }

    public SongViewFragment getSongFragment() {
        return (SongViewFragment) pagerAdapter.getFragment(PagerAdapter.MusicFragments.SONG.ordinal());
    }

    private boolean isPlaylistPage() {
        return viewPager.getCurrentItem() == PagerAdapter.MusicFragments.PLAYLIST.ordinal();
    }

    public PlaylistViewFragment getPlaylistFragment() {
        return (PlaylistViewFragment) pagerAdapter.getFragment(PagerAdapter.MusicFragments.PLAYLIST.ordinal());
    }

    private void setUpGridMenu(Menu menu) {
        boolean isPortrait = Util.isInPortraitMode(this);
        int columns = isPortrait ? PreferenceUtils.getInstance(this).getAlbumGridColumns() : PreferenceUtils.getInstance(this).getAlbumGridColumnsLand();
        String title = isPortrait ? getResources().getString(R.string.action_grid_columns) : getResources().getString(R.string.action_grid_columns_land);

        MenuItem gridSizeItem = menu.findItem(R.id.action_grid_columns);
        gridSizeItem.setTitle(title);

        SubMenu gridSizeMenu = gridSizeItem.getSubMenu();
        gridSizeMenu.getItem(columns - 1).setChecked(true);
    }

    private boolean handleGridSize(MenuItem item) {
        int size = -1;

        switch (item.getItemId()) {
            case R.id.gridSizeOne:
                size = 1;
                break;
            case R.id.gridSizeTwo:
                size = 2;
                break;
            case R.id.gridSizeThree:
                size = 3;
                break;
            case R.id.gridSizeFour:
                size = 4;
                break;
            case R.id.gridSizeFive:
                size = 5;
                break;
            case R.id.gridSizeSix:
                size = 6;
                break;
        }

        if (size > 0) {
            item.setChecked(true);
            if (isAlbumPage()) {
                getAlbumFragment().setColumns(size);
                if (Util.isInPortraitMode(this)) {
                    PreferenceUtils.getInstance(this).setAlbumGridColumns(size);
                } else {
                    PreferenceUtils.getInstance(this).setAlbumGridColumnsLand(size);
                }
            }
            return true;
        }
        return false;
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
    public boolean onMenuOpened(int featureId, Menu menu) {
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Field f1 = Toolbar.class.getDeclaredField("mMenuView");
                    f1.setAccessible(true);
                    ActionMenuView actionMenuView = (ActionMenuView) f1.get(toolbar);

                    Field f2 = ActionMenuView.class.getDeclaredField("mPresenter");
                    f2.setAccessible(true);
                    ActionMenuPresenter presenter = (ActionMenuPresenter) f2.get(actionMenuView);

                    Field f3 = presenter.getClass().getDeclaredField("mOverflowPopup");
                    f3.setAccessible(true);
                    MenuPopupHelper overflowMenuPopupHelper = (MenuPopupHelper) f3.get(presenter);
                    ViewUtil.setCheckBoxTintForMenu(overflowMenuPopupHelper);

                    Field f4 = presenter.getClass().getDeclaredField("mActionButtonPopup");
                    f4.setAccessible(true);
                    MenuPopupHelper subMenuPopupHelper = (MenuPopupHelper) f4.get(presenter);
                    ViewUtil.setCheckBoxTintForMenu(subMenuPopupHelper);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return super.onMenuOpened(featureId, menu);
    }
}
