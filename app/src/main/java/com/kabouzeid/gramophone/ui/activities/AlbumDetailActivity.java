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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.kabouzeid.gramophone.misc.SimpleObservableScrollViewCallbacks;
import com.kabouzeid.gramophone.misc.SimpleTransitionListener;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.AbsTagEditorActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.AlbumTagEditorActivity;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Be careful when changing things in this Activity!
 */
public class AlbumDetailActivity extends AbsSlidingMusicPanelActivity implements PaletteColorHolder, CabHolder {

    public static final String TAG = AlbumDetailActivity.class.getSimpleName();
    private static final int TAG_EDITOR_REQUEST = 2001;

    public static final String EXTRA_ALBUM_ID = "extra_album_id";

    private Album album;

    @Bind(R.id.list)
    ObservableRecyclerView recyclerView;
    @Bind(R.id.image)
    ImageView albumArtImageView;
    @Bind(R.id.album_art_background)
    ImageView albumArtBackground;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.title)
    TextView albumTitleView;
    @Bind(R.id.list_background)
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
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
        }

        if (shouldColorNavigationBar())
            setNavigationBarColor(DialogUtils.resolveColor(this, R.attr.default_bar_color));

        getAlbumFromIntentExtras();
        setUpObservableListViewParams();
        setUpToolBar();
        setUpViews();
        animateFabCircularRevealOnEnterTransitionEnd();
    }

    @Override
    protected View createContentView() {
        return wrapSlidingMusicPanelAndFab(R.layout.activity_album_detail);
    }

    private void animateFabCircularRevealOnEnterTransitionEnd() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getEnterTransition().addListener(new SimpleTransitionListener() {
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

    private final SimpleObservableScrollViewCallbacks observableScrollViewCallbacks = new SimpleObservableScrollViewCallbacks() {
        @Override
        public void onScrollChanged(int scrollY, boolean b, boolean b2) {
            scrollY += albumArtViewHeight + titleViewHeight;
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
        setUpRecyclerViewView();
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
                        .postProcessor(new BitmapProcessor() {
                            @Override
                            public Bitmap process(Bitmap bitmap) {
                                final int color = ColorUtil.generateColor(AlbumDetailActivity.this, bitmap);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setColors(color);
                                    }
                                });
                                return bitmap;
                            }
                        })
                        .build(),
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingFailed(String imageUri, View view, @Nullable FailReason failReason) {
                        setUpBackground("drawable://" + R.drawable.default_album_art);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            startPostponedEnterTransition();
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, @Nullable Bitmap loadedImage) {
                        if (loadedImage == null) {
                            onLoadingFailed(imageUri, view, null);
                            return;
                        }
                        setUpBackground(imageUri);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            startPostponedEnterTransition();
                    }
                }
        );
    }

    private void setUpBackground(String imageUri) {
        ImageLoader.getInstance().displayImage(
                imageUri,
                albumArtBackground,
                new DisplayImageOptions.Builder().postProcessor(new BlurProcessor(10)).build()
        );
    }

    private void setColors(int vibrantColor) {
        toolbarColor = vibrantColor;
        albumTitleView.setBackgroundColor(vibrantColor);
        albumTitleView.setTextColor(ColorUtil.getPrimaryTextColorForBackground(this, vibrantColor));

        if (shouldColorNavigationBar())
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

    private void setUpRecyclerViewView() {
        recyclerView.setScrollViewCallbacks(observableScrollViewCallbacks);
        recyclerView.setPadding(0, albumArtViewHeight + titleViewHeight, 0, bottomOffset);
        final View contentView = getWindow().getDecorView().findViewById(android.R.id.content);
        contentView.post(new Runnable() {
            @Override
            public void run() {
                songsBackgroundView.getLayoutParams().height = contentView.getHeight();
                observableScrollViewCallbacks.onScrollChanged(-(albumArtViewHeight + titleViewHeight), false, false);
                // necessary to fix a bug
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
        adapter = new AlbumSongAdapter(this, loadSongDataSet(), R.layout.item_list, false, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (adapter.getItemCount() == 0) finish();
            }
        });
    }

    private void reloadDataSet() {
        adapter.swapDataSet(loadSongDataSet());
    }

    private ArrayList<Song> loadSongDataSet() {
        return AlbumSongLoader.getAlbumSongList(this, album.id);
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
            case R.id.action_tag_editor:
                Intent intent = new Intent(this, AlbumTagEditorActivity.class);
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, album.id);
                startActivityForResult(intent, TAG_EDITOR_REQUEST);
                return true;
            case R.id.action_go_to_artist:
                Pair[] artistPairs = addPlayPauseFabToSharedViews(null);
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

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        reloadDataSet();
    }
}