package com.kabouzeid.gramophone.ui.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.AppBarLayout.OnOffsetChangedListener;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.ThemeSingleton;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.BillingProcessor.IBillingHandler;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.PagerAdapter;
import com.kabouzeid.gramophone.dialogs.AboutDialog;
import com.kabouzeid.gramophone.dialogs.ChangelogDialog;
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
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AbsSlidingMusicPanelActivity
        implements KabViewsDisableAble, CabHolder, IBillingHandler {

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

    @Nullable
    private View navigationDrawerHeader;
    private PagerAdapter pagerAdapter;
    private MaterialCab cab;

    private BillingProcessor billingProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Util.setStatusBarTranslucent(getWindow());
            drawerLayout.setFitsSystemWindows(false);
            navigationView.setFitsSystemWindows(false);
            findViewById(R.id.drawer_content_container).setFitsSystemWindows(false);
        }

        setUpDrawerLayout();
        setUpToolbar();
        setUpViewPager();

        if (shouldColorNavigationBar())
            setNavigationBarThemeColor();
        setStatusBarThemeColor();

        billingProcessor = new BillingProcessor(this, App.GOOGLE_PLAY_LICENSE_KEY, this);

        checkChangelog();
    }

    @Override
    protected View createContentView() {
        @SuppressLint("InflateParams")
        View contentView = getLayoutInflater().inflate(R.layout.activity_main_drawer_layout, null);
        ViewGroup drawerContent = ButterKnife.findById(contentView, R.id.drawer_content_container);
        drawerContent.addView(wrapSlidingMusicPanelAndFab(R.layout.activity_main_content));
        return contentView;
    }

    private void setUpViewPager() {
        pagerAdapter = new PagerAdapter(this, getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(pagerAdapter.getCount() - 1);

        int startPosition = PreferenceUtil.getInstance(this).getDefaultStartPage();
        startPosition = startPosition == -1 ? PreferenceUtil.getInstance(this).getLastStartPage() : startPosition;

        navigationView.getMenu().getItem(startPosition).setChecked(true);

        tabs.setupWithViewPager(pager);
        setUpTabStripColor(getThemeColorAccent() == Color.WHITE ? Color.WHITE : ThemeSingleton.get().positiveColor.getDefaultColor());

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

    private void setUpTabStripColor(@ColorInt int color) {
        // use reflection to set the selected indicator color
        try {
            Field tabStripField = tabs.getClass().getDeclaredField("mTabStrip");
            tabStripField.setAccessible(true);
            Object tabStrip = tabStripField.get(tabs);

            Method setSelectedIndicatorColorMethod = tabStrip.getClass().getDeclaredMethod("setSelectedIndicatorColor", Integer.TYPE);
            setSelectedIndicatorColorMethod.setAccessible(true);
            setSelectedIndicatorColorMethod.invoke(tabStrip, color);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        setTitle(getResources().getString(R.string.app_name));
        setAppBarColor();
        setSupportActionBar(toolbar);
    }

    private void setAppBarColor() {
        appbar.setBackgroundColor(getThemeColorPrimary());
    }

    private void setUpNavigationView() {
        final int colorAccent = ThemeSingleton.get().positiveColor.getDefaultColor();
        navigationView.setItemTextColor(new ColorStateList(
                new int[][]{
                        //{-android.R.attr.state_enabled}, // disabled
                        {android.R.attr.state_checked}, // checked
                        {} // default
                },
                new int[]{
                        // 0,
                        colorAccent,
                        ThemeSingleton.get().darkTheme ? ContextCompat.getColor(this, R.color.primary_text_default_material_dark) : ContextCompat.getColor(this, R.color.primary_text_default_material_light)
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
                        ThemeSingleton.get().darkTheme ? ContextCompat.getColor(this, R.color.secondary_text_default_material_dark) : ContextCompat.getColor(this, R.color.secondary_text_default_material_light)
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
                    case R.id.support_development:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showDonationDialog();
                            }
                        }, 200);
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

    private void setUpDrawerLayout() {
        setUpNavigationView();
    }

    private void updateNavigationDrawerHeader() {
        if (!MusicPlayerRemote.getPlayingQueue().isEmpty()) {
            Song song = MusicPlayerRemote.getCurrentSong();
            if (navigationDrawerHeader == null) {
                navigationDrawerHeader = navigationView.inflateHeaderView(R.layout.navigation_drawer_header);
                //noinspection ConstantConditions
                navigationDrawerHeader.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawerLayout.closeDrawers();
                        if (getSlidingUpPanelLayout().getPanelState() != SlidingUpPanelLayout.PanelState.EXPANDED) {
                            getSlidingUpPanelLayout().setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                        }
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
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ViewUtil.invalidateToolbarPopupMenuTint(toolbar);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
            }
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

        if (intent.getAction() != null && intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
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

//    private boolean isArtistPage() {
//        return pager.getCurrentItem() == PagerAdapter.MusicFragments.ARTIST.ordinal();
//    }
//
//    public ArtistViewFragment getArtistFragment() {
//        return (ArtistViewFragment) pagerAdapter.getFragment(PagerAdapter.MusicFragments.ARTIST.ordinal());
//    }
//
//    private boolean isAlbumPage() {
//        return pager.getCurrentItem() == PagerAdapter.MusicFragments.ALBUM.ordinal();
//    }
//
//    public AlbumViewFragment getAlbumFragment() {
//        return (AlbumViewFragment) pagerAdapter.getFragment(PagerAdapter.MusicFragments.ALBUM.ordinal());
//    }
//
//    private boolean isSongPage() {
//        return pager.getCurrentItem() == PagerAdapter.MusicFragments.SONG.ordinal();
//    }
//
//    public SongViewFragment getSongFragment() {
//        return (SongViewFragment) pagerAdapter.getFragment(PagerAdapter.MusicFragments.SONG.ordinal());
//    }

    private boolean isPlaylistPage() {
        return pager.getCurrentItem() == PagerAdapter.MusicFragments.PLAYLIST.ordinal();
    }

//    public PlaylistViewFragment getPlaylistFragment() {
//        return (PlaylistViewFragment) pagerAdapter.getFragment(PagerAdapter.MusicFragments.PLAYLIST.ordinal());
//    }


    @Override
    protected void showOverflowMenu() {
        super.showOverflowMenu();
        if (toolbar != null && getSlidingUpPanelLayout().getPanelState() != SlidingUpPanelLayout.PanelState.EXPANDED)
            toolbar.showOverflowMenu();
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

    @Override
    public void onPanelExpanded(View view) {
        super.onPanelExpanded(view);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void onPanelCollapsed(View view) {
        super.onPanelCollapsed(view);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    private void checkChangelog() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int currentVersion = pInfo.versionCode;
            if (currentVersion != PreferenceUtil.getInstance(this).getLastChangelogVersion()) {
                ChangelogDialog.create().show(getSupportFragmentManager(), "CHANGE_LOG_DIALOG");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    static class SkuDetailsAdapter extends ArrayAdapter<SkuDetails> {
        @LayoutRes
        private static int LAYOUT_RES_ID = R.layout.item_donation_option;

        public SkuDetailsAdapter(@NonNull Context context, @NonNull List<SkuDetails> objects) {
            super(context, LAYOUT_RES_ID, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(LAYOUT_RES_ID, parent, false);
            }

            SkuDetails skuDetails = getItem(position);
            ViewHolder viewHolder = new ViewHolder(convertView);

            viewHolder.title.setText(skuDetails.title.replace("(Phonograph Music Player)", "").trim());
            viewHolder.text.setText(skuDetails.description);
            viewHolder.price.setText(skuDetails.priceText);

            return convertView;
        }

        static class ViewHolder {
            @Bind(R.id.title)
            TextView title;
            @Bind(R.id.text)
            TextView text;
            @Bind(R.id.price)
            TextView price;

            public ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }

    private void showDonationDialog() {
        final String[] ids = getResources().getStringArray(R.array.donation_ids);
        List<SkuDetails> skuDetailsList = billingProcessor.getPurchaseListingDetails(new ArrayList<>(Arrays.asList(ids)));
        if (skuDetailsList == null) return;

        new MaterialDialog.Builder(this)
                .title(R.string.support_development)
                .adapter(new SkuDetailsAdapter(this, skuDetailsList), new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        donate(i);
                    }
                }).show();
    }

    private void donate(int i) {
        final String[] ids = getResources().getStringArray(R.array.donation_ids);
        billingProcessor.purchase(MainActivity.this, ids[i]);
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        billingProcessor.consumePurchase(productId);
        Toast.makeText(this, R.string.thank_you, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPurchaseHistoryRestored() {
        // ignore
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Toast.makeText(this, "Billing error: code = " + errorCode +
                (error != null ? ", error: " + error.getMessage() : ""), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBillingInitialized() {
        // ignore
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        if (billingProcessor != null) {
            billingProcessor.release();
        }
        super.onDestroy();
    }
}