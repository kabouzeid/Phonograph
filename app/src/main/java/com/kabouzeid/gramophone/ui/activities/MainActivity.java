package com.kabouzeid.gramophone.ui.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.samples.apps.iosched.ui.widget.SlidingTabLayout;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.AboutDeveloperDialogHelper;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.PlayingQueueDialogHelper;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.model.MusicRemoteEvent;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.ui.fragments.NavigationDrawerFragment;
import com.kabouzeid.gramophone.ui.fragments.mainactivityfragments.AlbumViewFragment;
import com.kabouzeid.gramophone.ui.fragments.mainactivityfragments.ArtistViewFragment;
import com.kabouzeid.gramophone.ui.fragments.mainactivityfragments.AbsMainActivityFragment;
import com.kabouzeid.gramophone.ui.fragments.mainactivityfragments.SongViewFragment;
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
    private MainActivityViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private SlidingTabLayout slidingTabLayout;

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
        viewPagerAdapter = new MainActivityViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        int startPosition = PreferenceUtils.getInstace(this).getStartPage();
        viewPager.setCurrentItem(startPosition);
        navigationDrawerFragment.setItemChecked(startPosition);

        slidingTabLayout.setSelectedIndicatorColors(Util.resolveColor(MainActivity.this, R.attr.colorAccent));
        slidingTabLayout.setViewPager(viewPager);
        slidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                PreferenceUtils.getInstace(MainActivity.this).setStartPage(position);
                navigationDrawerFragment.setItemChecked(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initViews() {
        viewPager = (ViewPager) findViewById(R.id.pager);
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
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
            ((AbsMainActivityFragment) viewPagerAdapter.getItem(viewPager.getCurrentItem())).enableViews();
        } catch (NullPointerException e) {
            //Log.e(TAG, "wasn't able to enable the views", e);
        }
    }

    @Override
    public void disableViews() {
        try {
            super.disableViews();
            ((AbsMainActivityFragment) viewPagerAdapter.getItem(viewPager.getCurrentItem())).disableViews();
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
        getMenuInflater().inflate(R.menu.drawer, menu);
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
            case R.id.action_search:
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                return true;
            case R.id.action_settings:
                return true;
            case R.id.action_about:
                AboutDeveloperDialogHelper.getDialog(this).show();
                return true;
            case R.id.action_current_playing:
                NavigationUtil.openCurrentPlayingIfPossible(this, getSharedViewsWithFab(null));
                return true;
            case R.id.action_playing_queue:
                final MaterialDialog materialDialog = PlayingQueueDialogHelper.getDialog(this);
                if (materialDialog != null) {
                    materialDialog.show();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.nothing_playing), Toast.LENGTH_SHORT).show();
                }
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

    public static class PlaceholderFragmentAbs extends AbsMainActivityFragment {

        public PlaceholderFragmentAbs() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_place_holder, container, false);
            TextView text = (TextView) rootView.findViewById(R.id.text);
            text.setText("Coming soon!");
            return rootView;
        }

        @Override
        public void enableViews() {

        }

        @Override
        public void disableViews() {

        }

        @Override
        public boolean areViewsEnabled() {
            return false;
        }
    }

    private class MainActivityViewPagerAdapter extends FragmentPagerAdapter {

        private String[] titles;

        private SparseArray<AbsMainActivityFragment> pages; //TODO check if this must be static
        private Context context;

        public MainActivityViewPagerAdapter(Activity activity) {
            super(activity.getFragmentManager());
            pages = new SparseArray<>();
            context = activity;
            titles = new String[]{
                    context.getResources().getString(R.string.songs),
                    context.getResources().getString(R.string.albums),
                    context.getResources().getString(R.string.artists),
                    context.getResources().getString(R.string.genres),
                    context.getResources().getString(R.string.playlists)
            };
        }

        @Override
        public Fragment getItem(final int position) {
            switch (position) {
                case 0:
                    return pages.get(position, new SongViewFragment());
                case 1:
                    return pages.get(position, new AlbumViewFragment());
                case 2:
                    return pages.get(position, new ArtistViewFragment());
                case 3:
                    //TODO genres
                case 4:
                    //TODO playlists
            }
            return pages.get(position, new PlaceholderFragmentAbs());
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}
