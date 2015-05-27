package com.kabouzeid.gramophone.ui.activities;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.songadapter.AlbumSongAdapter;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.bitmapblur.StackBlurManager;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.interfaces.PaletteColorHolder;
import com.kabouzeid.gramophone.loader.AlbumLoader;
import com.kabouzeid.gramophone.loader.AlbumSongLoader;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.misc.SmallObservableScrollViewCallbacks;
import com.kabouzeid.gramophone.misc.SmallTransitionListener;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.DataBaseChangedEvent;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.model.UIPreferenceChangedEvent;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.AlbumTagEditorActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * A lot of hackery is done in this activity. Changing things may will brake the whole activity.
 * <p/>
 * Should be kinda stable ONLY AS IT IS!!!
 */
public class AlbumDetailActivity extends AbsFabActivity implements PaletteColorHolder, CabHolder {

    public static final String TAG = AlbumDetailActivity.class.getSimpleName();
    private static final int TAG_EDITOR_REQUEST = 2001;
    private Album album;

    private ObservableRecyclerView recyclerView;
    private AlbumSongAdapter adapter;
    private ArrayList<Song> songs;
    private View statusBar;
    private ImageView albumArtImageView;
    private ImageView albumArtBackground;
    private View songsBackgroundView;
    private TextView albumTitleView;
    private Toolbar toolbar;
    private MaterialCab cab;
    private int headerOffset;
    private int titleViewHeight;
    private int albumArtViewHeight;
    private int toolbarColor;
    private float toolbarAlpha;
    private int bottomOffset;

    private final SmallObservableScrollViewCallbacks observableScrollViewCallbacks = new SmallObservableScrollViewCallbacks() {
        @Override
        public void onScrollChanged(int scrollY, boolean b, boolean b2) {
            scrollY += albumArtViewHeight + titleViewHeight;
            super.onScrollChanged(scrollY, b, b2);
            float flexibleRange = albumArtViewHeight - headerOffset;

            // Translate album cover
            ViewHelper.setTranslationY(albumArtImageView, Math.max(-albumArtViewHeight, -scrollY / 2));

            // Translate list background
            ViewHelper.setTranslationY(songsBackgroundView, Math.max(0, -scrollY + albumArtViewHeight));

            // Change alpha of overlay
            toolbarAlpha = Math.max(0, Math.min(1, (float) scrollY / flexibleRange));
            ViewUtil.setBackgroundAlpha(toolbar, toolbarAlpha, toolbarColor);
            ViewUtil.setBackgroundAlpha(statusBar, cab != null && cab.isActive() ? 1 : toolbarAlpha, toolbarColor);

            // Translate name text
            int maxTitleTranslationY = albumArtViewHeight;
            int titleTranslationY = maxTitleTranslationY - scrollY;
            titleTranslationY = Math.max(headerOffset, titleTranslationY);

            ViewHelper.setTranslationY(albumTitleView, titleTranslationY);
        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);

        App.bus.register(this);

        if (Util.isAtLeastLollipop()) {
            postponeEnterTransition();
            if (PreferenceUtils.getInstance(this).coloredNavigationBarAlbumEnabled())
                setNavigationBarColor(DialogUtils.resolveColor(this, R.attr.default_bar_color));
        }

        Bundle intentExtras = getIntent().getExtras();
        int albumId = -1;
        if (intentExtras != null) {
            albumId = intentExtras.getInt(AppKeys.E_ALBUM);
        }
        album = AlbumLoader.getAlbum(this, albumId);
        if (album.id == -1) {
            finish();
        }

        initViews();
        setUpObservableListViewParams();
        setUpToolBar();
        setUpViews();

        if (Util.isAtLeastLollipop()) {
            getWindow().getEnterTransition().addListener(new SmallTransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                    albumArtBackground.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    int cx = (albumArtBackground.getLeft() + albumArtBackground.getRight()) / 2;
                    int cy = (albumArtBackground.getTop() + albumArtBackground.getBottom()) / 2;
                    int finalRadius = Math.max(albumArtBackground.getWidth(), albumArtBackground.getHeight());

                    Animator animator = ViewAnimationUtils.createCircularReveal(albumArtBackground, cx, cy, albumArtImageView.getWidth() / 2, finalRadius);
                    animator.setInterpolator(new DecelerateInterpolator());
                    animator.setDuration(1000);
                    animator.start();

                    albumArtBackground.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    protected boolean shouldColorStatusBar() {
        return false;
    }

    @Override
    protected boolean shouldColorNavBar() {
        return false;
    }

    @Override
    protected boolean shouldSetStatusBarTranslucent() {
        return true;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    private void initViews() {
        albumArtImageView = (ImageView) findViewById(R.id.album_art);
        albumArtBackground = (ImageView) findViewById(R.id.album_art_background);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (ObservableRecyclerView) findViewById(R.id.list);
        albumTitleView = (TextView) findViewById(R.id.album_title);
        songsBackgroundView = findViewById(R.id.list_background);
        statusBar = findViewById(R.id.status_bar);
    }

    private void setUpObservableListViewParams() {
        bottomOffset = getResources().getDimensionPixelSize(R.dimen.bottom_offset_fab_activity);
        albumArtViewHeight = getResources().getDimensionPixelSize(R.dimen.header_image_height);
        toolbarColor = DialogUtils.resolveColor(this, R.attr.default_bar_color);
        int toolbarHeight = Util.getActionBarSize(this);
        titleViewHeight = getResources().getDimensionPixelSize(R.dimen.title_view_height);
        headerOffset = toolbarHeight;
        if (Util.isAtLeastKitKat())
            headerOffset += getResources().getDimensionPixelSize(R.dimen.status_bar_padding);
    }

    private void setUpViews() {
        albumTitleView.setText(album.title);
        setUpListView();
        setUpSongsAdapter();
        setUpAlbumArtAndApplyPalette();
    }

    private void setUpAlbumArtAndApplyPalette() {
        ImageLoader.getInstance().displayImage(
                MusicUtil.getAlbumArtUri(album.id).toString(),
                albumArtImageView,
                new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .showImageOnFail(R.drawable.default_album_art)
                        .resetViewBeforeLoading(true)
                        .build(),
                new SimpleImageLoadingListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        applyPalette(null);
                        albumArtBackground.setImageBitmap(new StackBlurManager(BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art)).process(10));
                        if (Util.isAtLeastLollipop()) startPostponedEnterTransition();
                    }

                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        applyPalette(loadedImage);
                        albumArtBackground.setImageBitmap(new StackBlurManager(loadedImage).process(10));
                        if (Util.isAtLeastLollipop()) startPostponedEnterTransition();
                    }
                }
        );
    }

    private void applyPalette(Bitmap bitmap) {
        if (bitmap != null) {
            Palette.from(bitmap)
                    .generate(new Palette.PaletteAsyncListener() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onGenerated(Palette palette) {
                            final Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                            if (vibrantSwatch != null) {
                                toolbarColor = vibrantSwatch.getRgb();
                                albumTitleView.setBackgroundColor(toolbarColor);
                                albumTitleView.setTextColor(vibrantSwatch.getTitleTextColor());
                                if (Util.isAtLeastLollipop() && PreferenceUtils.getInstance(AlbumDetailActivity.this).coloredNavigationBarAlbumEnabled())
                                    setNavigationBarColor(toolbarColor);
                                notifyTaskColorChange(toolbarColor);
                            } else {
                                resetColors();
                            }
                        }
                    });
        } else {
            resetColors();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void resetColors() {
        int titleTextColor = DialogUtils.resolveColor(this, R.attr.title_text_color);
        int defaultBarColor = DialogUtils.resolveColor(this, R.attr.default_bar_color);

        toolbarColor = defaultBarColor;
        albumTitleView.setBackgroundColor(defaultBarColor);
        albumTitleView.setTextColor(titleTextColor);

        if (Util.isAtLeastLollipop() && PreferenceUtils.getInstance(this).coloredNavigationBarArtistEnabled())
            setNavigationBarColor(DialogUtils.resolveColor(this, R.attr.default_bar_color));

        notifyTaskColorChange(toolbarColor);
    }

    @Override
    protected boolean overridesTaskColor() {
        return true;
    }

    @Override
    public int getPaletteColor() {
        return toolbarColor;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setNavigationBarColored(boolean colored) {
        if (colored) {
            setNavigationBarColor(toolbarColor);
        } else {
            setNavigationBarColor(Color.BLACK);
        }
    }

    private void setUpListView() {
        recyclerView.setScrollViewCallbacks(observableScrollViewCallbacks);
        recyclerView.setPadding(0, albumArtViewHeight + titleViewHeight, 0, bottomOffset);
        final View contentView = getWindow().getDecorView().findViewById(android.R.id.content);
        contentView.post(new Runnable() {
            @Override
            public void run() {
                songsBackgroundView.getLayoutParams().height = contentView.getHeight();
                observableScrollViewCallbacks.onScrollChanged(-(albumArtViewHeight + titleViewHeight), false, false);
                recyclerView.scrollBy(0, 1);
                recyclerView.scrollBy(0, -1);
            }
        });
    }

    private void setUpToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpSongsAdapter() {
        songs = AlbumSongLoader.getAlbumSongList(this, album.id);
        adapter = new AlbumSongAdapter(this, songs, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void enableViews() {
        super.enableViews();
        recyclerView.setEnabled(true);
        toolbar.setEnabled(true);
    }

    @Override
    public void disableViews() {
        super.disableViews();
        recyclerView.setEnabled(false);
        toolbar.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_equalizer:
                NavigationUtil.openEqualizer(this);
                return true;
            case R.id.action_shuffle_all:
                MusicPlayerRemote.shuffleAllSongs(this);
                return true;
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_playing_queue:
                NavigationUtil.openPlayingQueueDialog(this);
                return true;
            case R.id.action_current_playing:
                NavigationUtil.openCurrentPlayingIfPossible(this, getSharedViewsWithFab(null));
                return true;
            case R.id.action_tag_editor:
                Intent intent = new Intent(this, AlbumTagEditorActivity.class);
                intent.putExtra(AppKeys.E_ID, album.id);
                startActivityForResult(intent, TAG_EDITOR_REQUEST);
                return true;
            case R.id.action_go_to_artist:
                Pair[] artistPairs = getSharedViewsWithFab(null);
                NavigationUtil.goToArtist(this, album.artistId, artistPairs);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAG_EDITOR_REQUEST) {
            setUpAlbumArtAndApplyPalette();
            setResult(RESULT_OK);
        }
    }

    @Subscribe
    public void onDataBaseEvent(DataBaseChangedEvent event) {
        switch (event.getAction()) {
            case DataBaseChangedEvent.SONGS_CHANGED:
            case DataBaseChangedEvent.ALBUMS_CHANGED:
            case DataBaseChangedEvent.DATABASE_CHANGED:
                songs = AlbumSongLoader.getAlbumSongList(this, album.id);
                adapter.updateDataSet(songs);
                if (songs.size() < 1) finish();
                break;
        }
    }

    @Subscribe
    public void onUIPreferenceChanged(UIPreferenceChangedEvent event) {
        switch (event.getAction()) {
            case UIPreferenceChangedEvent.COLORED_NAVIGATION_BAR_ALBUM_CHANGED:
                setNavigationBarColored((boolean) event.getValue());
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.bus.unregister(this);
    }

    @Override
    public MaterialCab openCab(int menuRes, final MaterialCab.Callback callback) {
        if (cab != null && cab.isActive()) cab.finish();
        cab = new MaterialCab(this, R.id.cab_stub)
                .setMenu(menuRes)
                .setBackgroundColor(getPaletteColor())
                .start(new MaterialCab.Callback() {
                    @Override
                    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
                        ViewUtil.setBackgroundAlpha(statusBar, 1, toolbarColor);
                        return callback.onCabCreated(materialCab, menu);
                    }

                    @Override
                    public boolean onCabItemClicked(MenuItem menuItem) {
                        return callback.onCabItemClicked(menuItem);
                    }

                    @Override
                    public boolean onCabFinished(MaterialCab materialCab) {
                        ViewUtil.setBackgroundAlpha(statusBar, toolbarAlpha, toolbarColor);
                        return callback.onCabFinished(materialCab);
                    }
                });
        return cab;
    }

    @Override
    public void onBackPressed() {
        if (cab != null && cab.isActive()) cab.finish();
        else super.onBackPressed();
    }
}