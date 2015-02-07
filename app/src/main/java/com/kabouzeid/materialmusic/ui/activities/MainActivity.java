package com.kabouzeid.materialmusic.ui.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.helper.AboutDeveloperDialogHelper;
import com.kabouzeid.materialmusic.helper.PlayingQueueDialogHelper;
import com.kabouzeid.materialmusic.interfaces.KabSearchAbleFragment;
import com.kabouzeid.materialmusic.interfaces.KabViewsDisableAble;
import com.kabouzeid.materialmusic.interfaces.OnMusicRemoteEventListener;
import com.kabouzeid.materialmusic.model.MusicRemoteEvent;
import com.kabouzeid.materialmusic.model.Song;
import com.kabouzeid.materialmusic.ui.activities.base.AbsFabActivity;
import com.kabouzeid.materialmusic.ui.fragments.NavigationDrawerFragment;
import com.kabouzeid.materialmusic.ui.fragments.mainactivityfragments.AlbumViewFragment;
import com.kabouzeid.materialmusic.ui.fragments.mainactivityfragments.ArtistViewFragment;
import com.kabouzeid.materialmusic.ui.fragments.mainactivityfragments.SongViewFragment;
import com.kabouzeid.materialmusic.util.ImageLoaderUtil;
import com.kabouzeid.materialmusic.util.MusicUtil;
import com.kabouzeid.materialmusic.util.Util;
import com.kabouzeid.materialmusic.util.ViewUtil;
import com.nostra13.universalimageloader.core.ImageLoader;


public class MainActivity extends AbsFabActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, OnMusicRemoteEventListener, KabViewsDisableAble {
    public static final String TAG = MainActivity.class.getSimpleName();

    private int currentFragmentPosition = -1;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationDrawerFragment navigationDrawerFragment;
    private CharSequence toolbarTitle;
    private Toolbar toolbar;
    private View statusBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUpTranslucence(true, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setUpToolBar();

        navigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                drawerLayout
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(new Explode());
        }
    }

    private void initViews() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        updateNavigationDrawerHeader();
    }

    private void updateNavigationDrawerHeader() {
        if (navigationDrawerFragment != null) {
            Song song = getApp().getMusicPlayerRemote().getCurrentSong();
            if (song.id != -1) {
                ImageLoader.getInstance().displayImage(MusicUtil.getAlbumArtUri(song.albumId).toString(), navigationDrawerFragment.getAlbumArtImageView(), new ImageLoaderUtil.defaultAlbumArtOnFailed());
                navigationDrawerFragment.getSongTitle().setText(song.title);
                navigationDrawerFragment.getSongArtist().setText(song.artistName);
            }
        }
    }

    private void setUpToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        statusBar = findViewById(R.id.statusBar);
        setSupportActionBar(toolbar);
        ViewUtil.setBackgroundAlpha(toolbar, 0.97f, Util.resolveColor(this, R.attr.colorPrimary));
        ViewUtil.setBackgroundAlpha(statusBar, 0.97f, Util.resolveColor(this, R.attr.colorPrimary));
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
    protected void onResume() {
        super.onResume();
        updateNavigationDrawerHeader();
    }

    @Override
    public void enableViews() {
        try {
            super.enableViews();
            toolbar.setEnabled(true);
        } catch (NullPointerException e) {
            Log.e(TAG, "wasn't able to enable the views", e.fillInStackTrace());
        }
    }

    @Override
    public void disableViews() {
        try {
            super.disableViews();
            toolbar.setEnabled(false);
        } catch (NullPointerException e) {
            Log.e(TAG, "wasn't able to disable the views", e.fillInStackTrace());
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if (position == NavigationDrawerFragment.NAVIGATION_DRAWER_HEADER) {
            openCurrentPlayingIfPossible(null);
        } else {
            setFragment(position);
        }
    }

    private void setFragment(int position) {
        if (currentFragmentPosition != position) {
            switch (position) {
                case 0:
                    if (getApp().MainActivityFragments[position] == null) {
                        getApp().MainActivityFragments[position] = new SongViewFragment();
                    }
                    toolbarTitle = getString(R.string.all_songs);
                    break;
                case 1:
                    if (getApp().MainActivityFragments[position] == null) {
                        getApp().MainActivityFragments[position] = new AlbumViewFragment();
                    }
                    toolbarTitle = getString(R.string.albums);
                    break;
                case 2:
                    if (getApp().MainActivityFragments[position] == null) {
                        getApp().MainActivityFragments[position] = new ArtistViewFragment();
                    }
                    toolbarTitle = getString(R.string.artists);
                    break;
                case 3:
                    if (getApp().MainActivityFragments[position] == null) {
                        getApp().MainActivityFragments[position] = new PlaceholderFragment();
                    }
                    toolbarTitle = getString(R.string.genres);
                    break;
                case 4:
                    if (getApp().MainActivityFragments[position] == null) {
                        getApp().MainActivityFragments[position] = new PlaceholderFragment();
                    }
                    toolbarTitle = getString(R.string.playlists);
                    break;
                default:
                    toolbarTitle = getString(R.string.app_name);
                    return;
            }
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, getApp().MainActivityFragments[position])
                    .commit();
            currentFragmentPosition = position;
            supportInvalidateOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer, menu);
        restoreActionBar();

        final MenuItem search = menu.findItem(R.id.action_search);
        search.setVisible(currentFragmentPosition != -1 && getApp().MainActivityFragments[currentFragmentPosition] instanceof KabSearchAbleFragment);


        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (currentFragmentPosition != -1 && getApp().MainActivityFragments[currentFragmentPosition] instanceof KabSearchAbleFragment) {
                    ((KabSearchAbleFragment) getApp().MainActivityFragments[currentFragmentPosition]).search(newText);
                }
                return false;
            }
        });
        MenuItemCompat.setOnActionExpandListener(search, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (currentFragmentPosition != -1 && getApp().MainActivityFragments[currentFragmentPosition] instanceof KabSearchAbleFragment) {
                    ((KabSearchAbleFragment) getApp().MainActivityFragments[currentFragmentPosition]).returnToNonSearch();
                }
                return true;
            }
        });
        return true;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(toolbarTitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_about:
                AboutDeveloperDialogHelper.getDialog(this).show();
                return true;
            case R.id.action_current_playing:
                openCurrentPlayingIfPossible(null);
                return true;
            case R.id.action_playing_queue:
                final MaterialDialog materialDialog = PlayingQueueDialogHelper.getDialog(this, this);
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
    protected void onDestroy() {
        super.onDestroy();
        getApp().getMusicPlayerRemote().removeAllOnMusicRemoteEventListeners();
    }

    @Override
    public void onBackPressed() {
        if (navigationDrawerFragment.isDrawerOpen()) {
            drawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    private void disableFragmentViews() {
        if (currentFragmentPosition >= 0 && currentFragmentPosition < getApp().MainActivityFragments.length) {
            if (getApp().MainActivityFragments[currentFragmentPosition] instanceof KabViewsDisableAble) {
                ((KabViewsDisableAble) getApp().MainActivityFragments[currentFragmentPosition]).disableViews();
            }
        }
    }

    private void enableFragmentViews() {
        if (currentFragmentPosition >= 0 && currentFragmentPosition < getApp().MainActivityFragments.length) {
            if (getApp().MainActivityFragments[currentFragmentPosition] instanceof KabViewsDisableAble) {
                ((KabViewsDisableAble) getApp().MainActivityFragments[currentFragmentPosition]).enableViews();
            }
        }
    }

    private boolean areFragmentViewsEnabled() {
        if (currentFragmentPosition >= 0 && currentFragmentPosition < getApp().MainActivityFragments.length) {
            if (getApp().MainActivityFragments[currentFragmentPosition] instanceof KabViewsDisableAble) {
                return ((KabViewsDisableAble) getApp().MainActivityFragments[currentFragmentPosition]).areViewsEnabled();
            }
        }
        return true;
    }

    @Override
    public void onMusicRemoteEvent(MusicRemoteEvent event) {
        super.onMusicRemoteEvent(event);
        if (event.getAction() == MusicRemoteEvent.STATE_RESTORED || event.getAction() == MusicRemoteEvent.TRACK_CHANGED) {
            updateNavigationDrawerHeader();
        }
    }

    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_place_holder, container, false);
            return rootView;
        }
    }
}
