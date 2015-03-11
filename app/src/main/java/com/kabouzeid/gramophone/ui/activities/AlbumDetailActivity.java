package com.kabouzeid.gramophone.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.songadapter.SongAdapter;
import com.kabouzeid.gramophone.comparator.SongTrackNumberComparator;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.interfaces.OnMusicRemoteEventListener;
import com.kabouzeid.gramophone.loader.AlbumLoader;
import com.kabouzeid.gramophone.loader.AlbumSongLoader;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.misc.SmallObservableScrollViewCallbacks;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.AlbumTagEditorActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.ScaleInAnimationAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
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

public class AlbumDetailActivity extends AbsFabActivity implements OnMusicRemoteEventListener, KabViewsDisableAble {
    public static final String TAG = AlbumDetailActivity.class.getSimpleName();

    private static final boolean TOOLBAR_IS_STICKY = true;
    private static final int DEFAULT_DELAY_NO_TRANSITION = 200;
    private static final int DEFAULT_DELAY = 450;
    private static final int DEFAULT_ANIMATION_TIME = 1000;

    private App app;

    private Album album;

    private AnimationAdapter animatedSongsAdapter;
    private ObservableListView absSongListView;
    private View statusBar;
    private ImageView albumArtImageView;
    private View albumArtOverlayView;
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
            super.onScrollChanged(scrollY, b, b2);
            // Translate overlay and image
            float flexibleRange = albumArtViewHeight - headerOffset;
            int minOverlayTransitionY = headerOffset - albumArtOverlayView.getHeight();
            ViewHelper.setTranslationY(albumArtOverlayView, Math.max(minOverlayTransitionY, Math.min(0, -scrollY)));
            ViewHelper.setTranslationY(albumArtImageView, Math.max(minOverlayTransitionY, Math.min(0, -scrollY / 2)));

            // Translate list background
            ViewHelper.setTranslationY(songsBackgroundView, Math.max(0, -scrollY + albumArtViewHeight));

            // Change alpha of overlay
            ViewHelper.setAlpha(albumArtOverlayView, Math.max(0, Math.min(1, (float) scrollY / flexibleRange)));

            // Translate name text
            int maxTitleTranslationY = albumArtViewHeight;
            int titleTranslationY = maxTitleTranslationY - scrollY;
            if (TOOLBAR_IS_STICKY) {
                titleTranslationY = Math.max(headerOffset, titleTranslationY);
            }
            ViewHelper.setTranslationY(albumTitleView, titleTranslationY);

            // Translate FAB
            int fabTranslationY = titleTranslationY + titleViewHeight - (getFab().getHeight() / 2);
            ViewHelper.setTranslationY(getFab(), fabTranslationY);

            if (TOOLBAR_IS_STICKY) {
                // Change alpha of toolbar background
                if (-scrollY + albumArtViewHeight <= headerOffset) {
                    ViewUtil.setBackgroundAlpha(toolbar, 1, toolbarColor);
                    ViewUtil.setBackgroundAlpha(statusBar, 1, toolbarColor);

                } else {
                    ViewUtil.setBackgroundAlpha(toolbar, 0, toolbarColor);
                    ViewUtil.setBackgroundAlpha(statusBar, 0, toolbarColor);
                }
            } else {
                // Translate Toolbar
                if (scrollY < albumArtViewHeight) {
                    ViewHelper.setTranslationY(toolbar, 0);
                } else {
                    ViewHelper.setTranslationY(toolbar, -scrollY);
                }
            }
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
        animateEnterActivity();
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public void goToAlbum(int albumId) {
        if (album.id != albumId) {
            goToAlbum(albumId);
        }
    }

    private void initViews() {
        albumArtImageView = (ImageView) findViewById(R.id.album_art);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        albumArtOverlayView = findViewById(R.id.overlay);
        absSongListView = (ObservableListView) findViewById(R.id.list);
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
        ViewHelper.setAlpha(albumArtOverlayView, 0);

        prepareViewsForOpenAnimation();
        setUpAlbumArtAndApplyPalette();
        setUpListView();
    }

    private void prepareViewsForOpenAnimation() {
        albumTitleView.setPivotY(0);
        albumTitleView.setScaleY(0);
    }

    @SuppressLint("NewApi")
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
                    albumArtOverlayView.setBackgroundColor(swatch.getRgb());
                    albumTitleView.setBackgroundColor(swatch.getRgb());
                    albumTitleView.setTextColor(swatch.getTitleTextColor());
                }
            }
        });
    }

    private void setUpListView() {
        absSongListView.setScrollViewCallbacks(observableScrollViewCallbacks);
        setListViewPadding();
        final View contentView = getWindow().getDecorView().findViewById(android.R.id.content);
        contentView.post(new Runnable() {
            @Override
            public void run() {
                songsBackgroundView.getLayoutParams().height = contentView.getHeight();
                observableScrollViewCallbacks.onScrollChanged(0, false, false);
            }
        });
    }

    private void setListViewPadding() {
        setListViewPaddingTop();
        if (app.isInPortraitMode() || app.isTablet()) {
            setListViewPaddingBottom();
        }
    }

    private void setListViewPaddingTop() {
        final View paddingView = new View(AlbumDetailActivity.this);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                albumArtViewHeight + titleViewHeight);
        paddingView.setLayoutParams(lp);
        paddingView.setClickable(true);
        absSongListView.addHeaderView(paddingView);
    }

    private void setListViewPaddingBottom() {
        final View paddingView = new View(AlbumDetailActivity.this);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                Util.getNavigationBarHeight(this));
        paddingView.setLayoutParams(lp);
        paddingView.setClickable(true);
        absSongListView.addFooterView(paddingView);
    }

    private void setUpToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (!TOOLBAR_IS_STICKY) {
            toolbar.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void setUpTranslucence() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Util.setStatusBarTranslucent(getWindow(), true);
            if (app.isInPortraitMode() || app.isTablet()) {
                Util.setNavBarTranslucent(getWindow(), true);
            }
        }
    }

    private void animateEnterActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    animateHeader(0);
                    setUpSongsAdapter();
                }
            }, DEFAULT_DELAY);

        } else {
            setUpSongsAdapter();
            getFab().setScaleX(0);
            getFab().setScaleY(0);
            animateHeader(DEFAULT_DELAY_NO_TRANSITION);
            animateFab(DEFAULT_DELAY_NO_TRANSITION);
        }
    }

    private void setUpSongsAdapter() {
        final List<Song> songs = AlbumSongLoader.getAlbumSongList(this, album.id, new SongTrackNumberComparator());
        final SongAdapter songAdapter = new SongAdapter(this, this, songs);

//        SwingBottomInAnimationAdapter songsAdapter = new SwingBottomInAnimationAdapter(songAdapter);
//        SwingRightInAnimationAdapter songsAdapter = new SwingRightInAnimationAdapter(songAdapter);
//        SwingLeftInAnimationAdapter songsAdapter = new SwingLeftInAnimationAdapter(songAdapter);
        ScaleInAnimationAdapter songsAdapter = new ScaleInAnimationAdapter(songAdapter);
//        AlphaInAnimationAdapter songsAdapter = new AlphaInAnimationAdapter(songAdapter);

        animatedSongsAdapter = songsAdapter;
        animatedSongsAdapter.setAbsListView(absSongListView);

        absSongListView.setAdapter(animatedSongsAdapter);
        absSongListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    app.getMusicPlayerRemote().openQueue(songs, position - 1, true);
                }
            }
        });
    }

    private void animateHeader(int startDelay) {
        ViewPropertyAnimator.animate(albumTitleView)
                .scaleX(1)
                .scaleY(1)
                .setInterpolator(new DecelerateInterpolator(4))
                .setDuration(DEFAULT_ANIMATION_TIME)
                .setStartDelay(startDelay)
                .start();
    }

    private void animateFab(int startDelay) {
        ViewPropertyAnimator.animate(getFab())
                .scaleX(1)
                .scaleY(1)
                .setInterpolator(new DecelerateInterpolator(4))
                .setDuration(DEFAULT_ANIMATION_TIME)
                .setStartDelay(startDelay)
                .start();
    }

    @Override
    public void enableViews() {
        super.enableViews();
        absSongListView.setEnabled(true);
        toolbar.setEnabled(true);
    }

    @Override
    public void disableViews() {
        super.disableViews();
        absSongListView.setEnabled(false);
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
                return openCurrentPlayingIfPossible(null);
            case R.id.action_tag_editor:
                Intent intent = new Intent(this, AlbumTagEditorActivity.class);
                intent.putExtra(AppKeys.E_ID, album.id);
                startActivity(intent);
                return true;
            case R.id.action_go_to_artist:
                goToArtist(album.artistId, null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
