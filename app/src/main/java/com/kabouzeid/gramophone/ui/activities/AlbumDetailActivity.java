package com.kabouzeid.gramophone.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.songadapter.AlbumSongAdapter;
import com.kabouzeid.gramophone.comparator.SongTrackNumberComparator;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.loader.AlbumLoader;
import com.kabouzeid.gramophone.loader.AlbumSongLoader;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.misc.SmallObservableScrollViewCallbacks;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.AlbumTagEditorActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.nineoldandroids.view.ViewHelper;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/*
*
* A lot of hackery is done in this activity. Changing things may will brake the whole activity.
*
* Should be kinda stable ONLY AS IT IS!!!
*
* */

public class AlbumDetailActivity extends AbsFabActivity {
    public static final String TAG = AlbumDetailActivity.class.getSimpleName();

    private App app;

    private Album album;

    private ObservableRecyclerView recyclerView;
    private View statusBar;
    private ImageView albumArtImageView;
    private View songsBackgroundView;
    private TextView albumTitleView;
    private Toolbar toolbar;
    private int toolbarHeight;
    private int headerOffset;
    private int titleViewHeight;
    private int albumArtViewHeight;
    private int toolbarColor;
    private SmallObservableScrollViewCallbacks observableScrollViewCallbacks = new SmallObservableScrollViewCallbacks() {
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
            float alpha = Math.max(0, Math.min(1, (float) scrollY / flexibleRange));
            ViewUtil.setBackgroundAlpha(toolbar, alpha, toolbarColor);
            ViewUtil.setBackgroundAlpha(statusBar, alpha, toolbarColor);

            // Translate name text
            int maxTitleTranslationY = albumArtViewHeight;
            int titleTranslationY = maxTitleTranslationY - scrollY;
            titleTranslationY = Math.max(headerOffset, titleTranslationY);

            ViewHelper.setTranslationY(albumTitleView, titleTranslationY);

            // Translate FAB
            int fabTranslationY = titleTranslationY + titleViewHeight - (getFab().getHeight() / 2);
            ViewHelper.setTranslationY(getFab(), fabTranslationY);
        }
    };

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        app = (App) getApplicationContext();
        setTheme(app.getAppTheme());
        setUpTranslucence();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);

        if (Util.hasLollipopSDK()) postponeEnterTransition();

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

        if (Util.hasLollipopSDK()) startPostponedEnterTransition();
    }

    @Override
    public String getTag() {
        return TAG;
    }

    private void initViews() {
        albumArtImageView = (ImageView) findViewById(R.id.album_art);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (ObservableRecyclerView) findViewById(R.id.list);
        albumTitleView = (TextView) findViewById(R.id.album_title);
        songsBackgroundView = findViewById(R.id.list_background);
        statusBar = findViewById(R.id.statusBar);
    }

    private void setUpObservableListViewParams() {
        albumArtViewHeight = getResources().getDimensionPixelSize(R.dimen.header_image_height);
        toolbarColor = getResources().getColor(R.color.materialmusic_default_bar_color);
        toolbarHeight = Util.getActionBarSize(this);
        titleViewHeight = getResources().getDimensionPixelSize(R.dimen.title_view_height);
        headerOffset = toolbarHeight;
        headerOffset += getResources().getDimensionPixelSize(R.dimen.statusMargin);
    }

    private void setUpViews() {
        albumTitleView.setText(album.title);

        ViewUtil.addOnGlobalLayoutListener(albumArtImageView, new Runnable() {
            @Override
            public void run() {
                setUpAlbumArtAndApplyPalette();
            }
        });
        setUpListView();
        setUpSongsAdapter();
    }

    private void setUpAlbumArtAndApplyPalette() {
        Picasso.with(this).load(MusicUtil.getAlbumArtUri(album.id))
                .placeholder(R.drawable.default_album_art)
                .into(albumArtImageView, new Callback.EmptyCallback() {
                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                        final Bitmap bitmap = ((BitmapDrawable) albumArtImageView.getDrawable()).getBitmap();
                        if (bitmap != null) applyPalette(bitmap);
                    }
                });
    }

    private void applyPalette(Bitmap bitmap) {
        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                Palette.Swatch swatch = palette.getVibrantSwatch();
                if (swatch != null) {
                    toolbarColor = swatch.getRgb();
                    albumTitleView.setBackgroundColor(swatch.getRgb());
                    albumTitleView.setTextColor(swatch.getTitleTextColor());
                }
            }
        });
    }

    private void setUpListView() {
        recyclerView.setScrollViewCallbacks(observableScrollViewCallbacks);
        setListViewPadding();
        final View contentView = getWindow().getDecorView().findViewById(android.R.id.content);
        contentView.post(new Runnable() {
            @Override
            public void run() {
                songsBackgroundView.getLayoutParams().height = contentView.getHeight();
                observableScrollViewCallbacks.onScrollChanged(-(albumArtViewHeight + titleViewHeight), false, false);
            }
        });
    }

    private void setListViewPadding() {
        if (Util.isInPortraitMode(this) || Util.isTablet(this)) {
            recyclerView.setPadding(0, albumArtViewHeight + titleViewHeight, 0, Util.getNavigationBarHeight(this));
        } else {
            recyclerView.setPadding(0, albumArtViewHeight + titleViewHeight, 0, 0);
        }
    }

    private void setUpToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpTranslucence() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Util.setStatusBarTranslucent(getWindow(), true);
            if (Util.isInPortraitMode(this) || Util.isTablet(this)) {
                Util.setNavBarTranslucent(getWindow(), true);
            }
        }
    }

    private void setUpSongsAdapter() {
        final List<Song> songs = AlbumSongLoader.getAlbumSongList(this, album.id, new SongTrackNumberComparator());
        final AlbumSongAdapter albumSongAdapter = new AlbumSongAdapter(this, songs);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(albumSongAdapter);
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
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_settings:
                return true;
            case R.id.action_current_playing:
                NavigationUtil.openCurrentPlayingIfPossible(this, getSharedViewsWithFab(null));
                return true;
            case R.id.action_tag_editor:
                Intent intent = new Intent(this, AlbumTagEditorActivity.class);
                intent.putExtra(AppKeys.E_ID, album.id);
                startActivity(intent);
                return true;
            case R.id.action_go_to_artist:
                NavigationUtil.goToArtist(this, album.artistId, null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
