package com.kabouzeid.gramophone.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.song.AlbumSongAdapter;
import com.kabouzeid.gramophone.dialogs.SleepTimerDialog;
import com.kabouzeid.gramophone.glide.PhonographColoredTarget;
import com.kabouzeid.gramophone.glide.SongGlideRequest;
import com.kabouzeid.gramophone.glide.palette.BitmapPaletteWrapper;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.interfaces.LoaderIds;
import com.kabouzeid.gramophone.interfaces.PaletteColorHolder;
import com.kabouzeid.gramophone.loader.AlbumLoader;
import com.kabouzeid.gramophone.misc.SimpleObservableScrollViewCallbacks;
import com.kabouzeid.gramophone.misc.WrappedAsyncTaskLoader;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.AbsTagEditorActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.AlbumTagEditorActivity;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PhonographColorUtil;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.kabouzeid.gramophone.views.TouchInterceptFrameLayout;
import com.kabouzeid.gramophone.views.TouchInterceptHorizontalScrollView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Be careful when changing things in this Activity!
 */
public class AlbumDetailActivity extends AbsSlidingMusicPanelActivity implements PaletteColorHolder, CabHolder, LoaderManager.LoaderCallbacks<Album> {

    public static final String TAG = AlbumDetailActivity.class.getSimpleName();
    private static final int TAG_EDITOR_REQUEST = 2001;
    private static final int LOADER_ID = LoaderIds.ALBUM_DETAIL_ACTIVITY;

    public static final String EXTRA_ALBUM_ID = "extra_album_id";

    private Album album;

    @BindView(R.id.container)
    TouchInterceptFrameLayout container;
    @BindView(R.id.list)
    ObservableRecyclerView recyclerView;
    @BindView(R.id.image)
    ImageView albumArtImageView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title)
    RelativeLayout albumTitleView;
    @BindView(R.id.album_title)
    TextView albumTitleTextView;
    @BindView(R.id.title_scrollview)
    TouchInterceptHorizontalScrollView titleScrollView;
    @BindView(R.id.list_background)
    View songsBackgroundView;
    @BindView(R.id.status_bar)
    View statusBar;

    private AlbumSongAdapter adapter;

    private MaterialCab cab;
    private int headerOffset;
    private int titleViewHeight;
    private int albumArtViewHeight;
    private int toolbarColor;
    private float toolbarAlpha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDrawUnderStatusbar(true);
        ButterKnife.bind(this);

        supportPostponeEnterTransition();

        setUpObservableListViewParams();
        setUpToolBar();
        setUpViews();
        ViewUtil.setStatusBarHeight(this, statusBar);

        getSupportLoaderManager().initLoader(LOADER_ID, getIntent().getExtras(), this);
    }

    @Override
    protected View createContentView() {
        return wrapSlidingMusicPanel(R.layout.activity_album_detail);
    }

    private final SimpleObservableScrollViewCallbacks observableScrollViewCallbacks = new SimpleObservableScrollViewCallbacks() {
        @Override
        public void onScrollChanged(int scrollY, boolean b, boolean b2) {
            scrollY += albumArtViewHeight + titleViewHeight;
            float flexibleRange = albumArtViewHeight - headerOffset;

            // Translate album cover
            albumArtImageView.setTranslationY(Math.max(-albumArtViewHeight, -scrollY / 2));

            // Translate list background
            songsBackgroundView.setTranslationY(Math.max(0, -scrollY + albumArtViewHeight));

            // Change alpha of overlay
            toolbarAlpha = Math.max(0, Math.min(1, (float) scrollY / flexibleRange));
            toolbar.setBackgroundColor(ColorUtil.withAlpha(toolbarColor, toolbarAlpha));
            setStatusbarColor(ColorUtil.withAlpha(toolbarColor, cab != null && cab.isActive() ? 1 : toolbarAlpha));

            // Translate name text
            int maxTitleTranslationY = albumArtViewHeight;
            int titleTranslationY = maxTitleTranslationY - scrollY;
            titleTranslationY = Math.max(headerOffset, titleTranslationY);

            albumTitleView.setTranslationY(titleTranslationY);
        }
    };

    private void setUpObservableListViewParams() {
        albumArtViewHeight = getResources().getDimensionPixelSize(R.dimen.header_image_height);
        toolbarColor = DialogUtils.resolveColor(this, R.attr.defaultFooterColor);
        int toolbarHeight = Util.getActionBarSize(this);
        titleViewHeight = getResources().getDimensionPixelSize(R.dimen.title_view_height);
        headerOffset = toolbarHeight;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            headerOffset += getResources().getDimensionPixelSize(R.dimen.status_bar_padding);
        }
    }

    private void setUpViews() {
        setUpRecyclerView();
        setUpSongsAdapter();
    }

    private void loadAlbumCover() {
        SongGlideRequest.Builder.from(Glide.with(this), getAlbum().safeGetFirstSong())
                .checkIgnoreMediaStore(this)
                .generatePalette(this).build()
                .dontAnimate()
                .listener(new RequestListener<Object, BitmapPaletteWrapper>() {
                    @Override
                    public boolean onException(Exception e, Object model, Target<BitmapPaletteWrapper> target, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(BitmapPaletteWrapper resource, Object model, Target<BitmapPaletteWrapper> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }
                })
                .into(new PhonographColoredTarget(albumArtImageView) {
                    @Override
                    public void onColorReady(int color) {
                        setColors(color);
                    }
                });
    }

    private void setColors(int color) {
        toolbarColor = color;
        albumTitleView.setBackgroundColor(color);
        albumTitleTextView.setTextColor(MaterialValueHelper.getPrimaryTextColor(this, ColorUtil.isColorLight(color)));
        setNavigationbarColor(color);
        setTaskDescriptionColor(color);
    }

    @Override
    public int getPaletteColor() {
        return toolbarColor;
    }

    private void setUpRecyclerView() {
        setUpRecyclerViewPadding();
        recyclerView.setScrollViewCallbacks(observableScrollViewCallbacks);
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

    private void setUpRecyclerViewPadding() {
        recyclerView.setPadding(0, albumArtViewHeight + titleViewHeight, 0, 0);
    }

    private void setUpToolBar() {
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpSongsAdapter() {
        adapter = new AlbumSongAdapter(this, getAlbum().songs, R.layout.item_list, false, this);
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

    private void reload() {
        getSupportLoaderManager().restartLoader(LOADER_ID, getIntent().getExtras(), this);
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
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, getAlbum().getId());
                startActivityForResult(intent, TAG_EDITOR_REQUEST);
                return true;
            case R.id.action_go_to_artist:
                NavigationUtil.goToArtist(this, getAlbum().getArtistId());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAG_EDITOR_REQUEST) {
            reload();
            setResult(RESULT_OK);
        }
    }

    @NonNull
    @Override
    public MaterialCab openCab(int menuRes, @NonNull final MaterialCab.Callback callback) {
        if (cab != null && cab.isActive()) cab.finish();
        cab = new MaterialCab(this, R.id.cab_stub)
                .setMenu(menuRes)
                .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
                .setBackgroundColor(PhonographColorUtil.shiftBackgroundColorForLightText(getPaletteColor()))
                .start(new MaterialCab.Callback() {
                    @Override
                    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
                        setStatusbarColor(ColorUtil.stripAlpha(toolbarColor));
                        return callback.onCabCreated(materialCab, menu);
                    }

                    @Override
                    public boolean onCabItemClicked(MenuItem menuItem) {
                        return callback.onCabItemClicked(menuItem);
                    }

                    @Override
                    public boolean onCabFinished(MaterialCab materialCab) {
                        setStatusbarColor(ColorUtil.withAlpha(toolbarColor, toolbarAlpha));
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
        reload();
    }

    @Override
    public void setStatusbarColor(int color) {
        super.setStatusbarColor(color);
        setLightStatusbar(false);
    }

    private void setAlbum(Album album) {
        this.album = album;
        loadAlbumCover();
        albumTitleTextView.setText(album.getTitle());
        container.setViews(titleScrollView, albumTitleTextView);
        container.setTruncateText(album.getTitle());
        container.setListParent(recyclerView);
        adapter.swapDataSet(album.songs);
    }

    private Album getAlbum() {
        if (album == null) album = new Album();
        return album;
    }

    @Override
    public Loader<Album> onCreateLoader(int id, Bundle args) {
        return new AsyncAlbumLoader(this, args.getInt(EXTRA_ALBUM_ID));
    }

    @Override
    public void onLoadFinished(Loader<Album> loader, Album data) {
        supportStartPostponedEnterTransition();
        setAlbum(data);
    }

    @Override
    public void onLoaderReset(Loader<Album> loader) {
        this.album = new Album();
        adapter.swapDataSet(album.songs);
    }

    private static class AsyncAlbumLoader extends WrappedAsyncTaskLoader<Album> {
        private final int albumId;

        public AsyncAlbumLoader(Context context, int albumId) {
            super(context);
            this.albumId = albumId;
        }

        @Override
        public Album loadInBackground() {
            return AlbumLoader.getAlbum(getContext(), albumId);
        }
    }
}