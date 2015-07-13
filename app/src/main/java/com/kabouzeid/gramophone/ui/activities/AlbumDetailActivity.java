package com.kabouzeid.gramophone.ui.activities;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.song.AlbumSongAdapter;
import com.kabouzeid.gramophone.dialogs.SleepTimerDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.imageloader.BlurProcessor;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.interfaces.PaletteColorHolder;
import com.kabouzeid.gramophone.loader.AlbumLoader;
import com.kabouzeid.gramophone.loader.AlbumSongLoader;
import com.kabouzeid.gramophone.misc.SmallObservableScrollViewCallbacks;
import com.kabouzeid.gramophone.misc.SmallTransitionListener;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.AbsTagEditorActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.AlbumTagEditorActivity;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A lot of hackery is done in this activity. Changing things may will brake the whole activity.
 * <p/>
 * Should be kinda stable ONLY AS IT IS!!!
 */
public class AlbumDetailActivity extends AbsSlidingMusicPanelActivity implements PaletteColorHolder, CabHolder {

    public static final String TAG = AlbumDetailActivity.class.getSimpleName();
    private static final int TAG_EDITOR_REQUEST = 2001;

    public static final String EXTRA_ALBUM_ID = "extra_album_id";

    private Album album;

    @InjectView(R.id.list)
    ObservableRecyclerView recyclerView;
    @InjectView(R.id.image)
    ImageView albumArtImageView;
    @InjectView(R.id.album_art_background)
    ImageView albumArtBackground;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.title)
    TextView albumTitleView;
    @InjectView(R.id.list_background)
    View songsBackgroundView;

    private AlbumSongAdapter adapter;

    private MaterialCab cab;
    private int headerOffset;
    private int titleViewHeight;
    private int albumArtViewHeight;
    private int toolbarColor;
    private float toolbarAlpha;
    private int bottomOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setStatusBarTransparent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);
        ButterKnife.inject(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
            if (PreferenceUtil.getInstance(this).coloredNavigationBarAlbum())
                setNavigationBarColor(DialogUtils.resolveColor(this, R.attr.default_bar_color));
        }

        getAlbumFromIntentExtras();
        setUpObservableListViewParams();
        setUpToolBar();
        setUpViews();
        animateFabCircularRevealOnEnterTransitionEnd();
    }

    private void animateFabCircularRevealOnEnterTransitionEnd() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getEnterTransition().addListener(new SmallTransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                    albumArtBackground.setVisibility(View.INVISIBLE);
                }

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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

    private final SmallObservableScrollViewCallbacks observableScrollViewCallbacks = new SmallObservableScrollViewCallbacks() {
        @Override
        public void onScrollChanged(int scrollY, boolean b, boolean b2) {
            scrollY += albumArtViewHeight + titleViewHeight;
            super.onScrollChanged(scrollY, b, b2);
            float flexibleRange = albumArtViewHeight - headerOffset;

            // Translate album cover
            albumArtImageView.setTranslationY(Math.max(-albumArtViewHeight, -scrollY / 2));
            albumArtBackground.setTranslationY(Math.max(-albumArtViewHeight, -scrollY / 2));

            // Translate list background
            songsBackgroundView.setTranslationY(Math.max(0, -scrollY + albumArtViewHeight));

            // Change alpha of overlay
            toolbarAlpha = Math.max(0, Math.min(1, (float) scrollY / flexibleRange));
            ViewUtil.setBackgroundAlpha(toolbar, toolbarAlpha, toolbarColor);
            setStatusBarColor(ColorUtil.getColorWithAlpha(cab != null && cab.isActive() ? 1 : toolbarAlpha, toolbarColor));

            // Translate name text
            int maxTitleTranslationY = albumArtViewHeight;
            int titleTranslationY = maxTitleTranslationY - scrollY;
            titleTranslationY = Math.max(headerOffset, titleTranslationY);

            albumTitleView.setTranslationY(titleTranslationY);
        }
    };

    private void getAlbumFromIntentExtras() {
        Bundle intentExtras = getIntent().getExtras();
        final int albumId = intentExtras.getInt(EXTRA_ALBUM_ID);
        album = AlbumLoader.getAlbum(this, albumId);
        if (album.id == -1) {
            finish();
        }
    }

    private void setUpObservableListViewParams() {
        bottomOffset = getResources().getDimensionPixelSize(R.dimen.bottom_offset_fab_activity);
        albumArtViewHeight = getResources().getDimensionPixelSize(R.dimen.header_image_height);
        toolbarColor = DialogUtils.resolveColor(this, R.attr.default_bar_color);
        int toolbarHeight = Util.getActionBarSize(this);
        titleViewHeight = getResources().getDimensionPixelSize(R.dimen.title_view_height);
        headerOffset = toolbarHeight;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            headerOffset += getResources().getDimensionPixelSize(R.dimen.status_bar_padding);
        }
    }

    private void setUpViews() {
        albumTitleView.setText(album.title);
        setUpListView();
        setUpSongsAdapter();
        setUpAlbumArtAndApplyPalette();
    }

    private void setUpAlbumArtAndApplyPalette() {
        ImageLoader.getInstance().displayImage(
                MusicUtil.getAlbumImageLoaderString(album),
                albumArtImageView,
                new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .showImageOnFail(R.drawable.default_album_art)
                        .resetViewBeforeLoading(true)
                        .build(),
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingFailed(String imageUri, View view, @Nullable FailReason failReason) {
                        applyPalette(null);

                        ImageLoader.getInstance().displayImage(
                                "drawable://" + R.drawable.default_album_art,
                                albumArtBackground,
                                new DisplayImageOptions.Builder().postProcessor(new BlurProcessor(10)).build()
                        );

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            startPostponedEnterTransition();
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, @Nullable Bitmap loadedImage) {
                        if (loadedImage == null) {
                            onLoadingFailed(imageUri, view, null);
                            return;
                        }
                        applyPalette(loadedImage);

                        ImageLoader.getInstance().displayImage(
                                imageUri,
                                albumArtBackground,
                                new DisplayImageOptions.Builder().postProcessor(new BlurProcessor(10)).build()
                        );

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            startPostponedEnterTransition();
                    }
                }
        );
    }

    private void applyPalette(@Nullable Bitmap bitmap) {
        final int defaultBarColor = ColorUtil.resolveColor(this, R.attr.default_bar_color);
        if (bitmap != null) {
            Palette.from(bitmap)
                    .resizeBitmapSize(100)
                    .generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(@NonNull Palette palette) {
                            setColors(palette.getVibrantColor(defaultBarColor));
                        }
                    });
        } else {
            setColors(defaultBarColor);
        }
    }


    private void setColors(int vibrantColor) {
        toolbarColor = vibrantColor;
        albumTitleView.setBackgroundColor(vibrantColor);
        albumTitleView.setTextColor(ColorUtil.getTextColorForBackground(vibrantColor));

        if (PreferenceUtil.getInstance(this).coloredNavigationBarArtist())
            setNavigationBarColor(vibrantColor);

        notifyTaskColorChange(vibrantColor);
    }

    @Override
    protected boolean overridesTaskColor() {
        return true;
    }

    @Override
    public int getPaletteColor() {
        return toolbarColor;
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
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpSongsAdapter() {
        adapter = new AlbumSongAdapter(this, AlbumSongLoader.getAlbumSongList(this, album.id), R.layout.item_list, this);
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_sleep_timer:
                new SleepTimerDialog().show(getSupportFragmentManager(), "SET_SLEEP_TIMER");
                return true;
            case R.id.action_equalizer:
                NavigationUtil.openEqualizer(this);
                return true;
            case R.id.action_shuffle_album:
                MusicPlayerRemote.openAndShuffleQueue(adapter.getDataSet(), true);
                return true;
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_playing_queue:
                NavigationUtil.openPlayingQueueDialog(this);
                return true;
            case R.id.action_now_playing:
                NavigationUtil.openCurrentPlayingIfPossible(this, getSharedViewsWithPlayPauseFab(null));
                return true;
            case R.id.action_tag_editor:
                Intent intent = new Intent(this, AlbumTagEditorActivity.class);
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, album.id);
                startActivityForResult(intent, TAG_EDITOR_REQUEST);
                return true;
            case R.id.action_go_to_artist:
                Pair[] artistPairs = getSharedViewsWithPlayPauseFab(null);
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

    @Override
    public MaterialCab openCab(int menuRes, @NonNull final MaterialCab.Callback callback) {
        if (cab != null && cab.isActive()) cab.finish();
        cab = new MaterialCab(this, R.id.cab_stub)
                .setMenu(menuRes)
                .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
                .setBackgroundColor(getPaletteColor())
                .start(new MaterialCab.Callback() {
                    @Override
                    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
                        setStatusBarColor(ColorUtil.getOpaqueColor(toolbarColor));
                        return callback.onCabCreated(materialCab, menu);
                    }

                    @Override
                    public boolean onCabItemClicked(MenuItem menuItem) {
                        return callback.onCabItemClicked(menuItem);
                    }

                    @Override
                    public boolean onCabFinished(MaterialCab materialCab) {
                        setStatusBarColor(ColorUtil.getColorWithAlpha(toolbarAlpha, toolbarColor));
                        return callback.onCabFinished(materialCab);
                    }
                });
        return cab;
    }

    @Override
    public void onBackPressed() {
        if (cab != null && cab.isActive()) cab.finish();
        else {
            recyclerView.stopScroll();
            super.onBackPressed();
        }
    }
}