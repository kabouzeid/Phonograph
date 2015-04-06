package com.kabouzeid.gramophone.ui.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.PagerAdapter;
import com.kabouzeid.gramophone.helper.AboutDeveloperDialogHelper;
import com.kabouzeid.gramophone.helper.CreatePlaylistDialogHelper;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.model.MusicRemoteEvent;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.ui.fragments.NavigationDrawerFragment;
import com.kabouzeid.gramophone.ui.fragments.mainactivityfragments.AbsMainActivityFragment;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.squareup.picasso.Picasso;


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

        final int accentColor = Util.resolveColor(MainActivity.this, R.attr.colorAccent);
        slidingTabLayout.setIndicatorColor(accentColor);
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
        setSupportActionBar(toolbar);
        float alpha = 0.97f;
        ViewUtil.setBackgroundAlpha(toolbar, alpha, Util.resolveColor(this, R.attr.colorPrimary));
        ViewUtil.setBackgroundAlpha(statusBar, alpha, Util.resolveColor(this, R.attr.colorPrimary));
        ViewUtil.setBackgroundAlpha(slidingTabLayout, alpha, Util.resolveColor(this, R.attr.colorPrimary));
        setUpDrawerToggle();
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
                Picasso.with(this)
                        .load(MusicUtil.getAlbumArtUri(song.albumId))
                        .error(R.drawable.default_album_art)
                        .placeholder(R.drawable.default_album_art)
                        .into(navigationDrawerFragment.getAlbumArtImageView());
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
        } else {
            if (viewPager != null) {
                viewPager.setCurrentItem(position, true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        switch (currentPage) {
            case 3:
                getMenuInflater().inflate(R.menu.menu_playlists, menu);
                break;
            default:
                getMenuInflater().inflate(R.menu.menu_main, menu);
                break;
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
        int id = item.getItemId();
        switch (id) {
            case R.id.action_shuffle_all:
                MusicPlayerRemote.shuffleAllSongs(this);
                return true;
            case R.id.action_new_playlist:
                CreatePlaylistDialogHelper.getDialog(this).show();
                return true;
            case R.id.action_search:
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_about:
                AboutDeveloperDialogHelper.getDialog(this).show();
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
}
