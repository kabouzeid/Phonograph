package com.kabouzeid.gramophone.ui.activities;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.ArtistAlbumAdapter;
import com.kabouzeid.gramophone.adapter.songadapter.ArtistSongAdapter;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.bitmapblur.StackBlurManager;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.interfaces.PaletteColorHolder;
import com.kabouzeid.gramophone.lastfm.rest.LastFMRestClient;
import com.kabouzeid.gramophone.lastfm.rest.model.artistinfo.ArtistInfo;
import com.kabouzeid.gramophone.lastfm.rest.model.artistinfo.Image;
import com.kabouzeid.gramophone.loader.ArtistAlbumLoader;
import com.kabouzeid.gramophone.loader.ArtistLoader;
import com.kabouzeid.gramophone.loader.ArtistSongLoader;
import com.kabouzeid.gramophone.misc.SmallObservableScrollViewCallbacks;
import com.kabouzeid.gramophone.misc.SmallTransitionListener;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.model.DataBaseChangedEvent;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.model.UIPreferenceChangedEvent;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.kabouzeid.gramophone.views.SquareIfPlaceImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A lot of hackery is done in this activity. Changing things may will brake the whole activity.
 * <p/>
 * Should be kinda stable ONLY AS IT IS!!!
 */
public class ArtistDetailActivity extends AbsFabActivity implements PaletteColorHolder, CabHolder {

    public static final String TAG = ArtistDetailActivity.class.getSimpleName();

    public static final String EXTRA_ARTIST_ID = "extra_artist_id";

    @InjectView(R.id.artist_image_background)
    ImageView artistImageBackground;
    @InjectView(R.id.artist_image)
    SquareIfPlaceImageView artistImage;
    @InjectView(R.id.list_background)
    View songListBackground;
    @InjectView(R.id.list)
    ObservableListView songListView;
    @InjectView(R.id.artist_name)
    TextView artistName;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    View songListHeader;
    RecyclerView albumRecyclerView;

    private MaterialCab cab;
    private int headerOffset;
    private int titleViewHeight;
    private int artistImageViewHeight;
    private int toolbarColor;
    private float toolbarAlpha;
    private int bottomOffset;

    private Artist artist;
    private Spanned biography;
    private ArtistAlbumAdapter albumAdapter;
    private ArtistSongAdapter songAdapter;
    private ArrayList<Song> songs;
    private ArrayList<Album> albums;

    private LastFMRestClient lastFMRestClient;

    private StackBlurManager defaultArtistImageBlurManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setStatusBarTransparent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);
        ButterKnife.inject(this);

        App.bus.register(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
            if (PreferenceUtils.getInstance(this).coloredNavigationBarArtist())
                setNavigationBarColor(DialogUtils.resolveColor(this, R.attr.default_bar_color));
        }

        lastFMRestClient = new LastFMRestClient(this);

        getIntentExtras();
        initViews();
        setUpObservableListViewParams();
        setUpViews();

        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fixLollipopTransitionImageWrongSize();
            startPostponedEnterTransition();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getEnterTransition().addListener(new SmallTransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                    artistImageBackground.setVisibility(View.INVISIBLE);
                }

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onTransitionEnd(Transition transition) {
                    int cx = (artistImageBackground.getLeft() + artistImageBackground.getRight()) / 2;
                    int cy = (artistImageBackground.getTop() + artistImageBackground.getBottom()) / 2;
                    int finalRadius = Math.max(artistImageBackground.getWidth(), artistImageBackground.getHeight());

                    Animator animator = ViewAnimationUtils.createCircularReveal(artistImageBackground, cx, cy, artistImage.getWidth() / 2, finalRadius);
                    animator.setInterpolator(new DecelerateInterpolator());
                    animator.setDuration(1000);
                    animator.start();

                    artistImageBackground.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private final SmallObservableScrollViewCallbacks observableScrollViewCallbacks = new SmallObservableScrollViewCallbacks() {
        @Override
        public void onScrollChanged(int scrollY, boolean b, boolean b2) {
            scrollY += artistImageViewHeight + titleViewHeight;
            super.onScrollChanged(scrollY, b, b2);
            float flexibleRange = artistImageViewHeight - headerOffset;

            // Translate album cover
            artistImage.setTranslationY(Math.max(-artistImageViewHeight, -scrollY / 2));

            // Translate list background
            songListBackground.setTranslationY(Math.max(0, -scrollY + artistImageViewHeight));

            // Change alpha of overlay
            toolbarAlpha = Math.max(0, Math.min(1, (float) scrollY / flexibleRange));
            ViewUtil.setBackgroundAlpha(toolbar, toolbarAlpha, toolbarColor);
            setStatusBarColor(Util.getColorWithAlpha(cab != null && cab.isActive() ? 1 : toolbarAlpha, toolbarColor));

            // Translate name text
            int maxTitleTranslationY = artistImageViewHeight;
            int titleTranslationY = maxTitleTranslationY - scrollY;
            titleTranslationY = Math.max(headerOffset, titleTranslationY);

            artistName.setTranslationY(titleTranslationY);
        }
    };

    private void setUpObservableListViewParams() {
        bottomOffset = getResources().getDimensionPixelSize(R.dimen.bottom_offset_fab_activity);
        artistImageViewHeight = getResources().getDimensionPixelSize(R.dimen.header_image_height);
        toolbarColor = DialogUtils.resolveColor(this, R.attr.default_bar_color);
        int toolbarHeight = Util.getActionBarSize(this);
        titleViewHeight = getResources().getDimensionPixelSize(R.dimen.title_view_height);
        headerOffset = toolbarHeight;
        if (Util.isAtLeastKitKat())
            headerOffset += getResources().getDimensionPixelSize(R.dimen.status_bar_padding);
    }

    private void initViews() {
        songListHeader = LayoutInflater.from(this).inflate(R.layout.artist_detail_header, songListView, false);
        albumRecyclerView = ButterKnife.findById(songListHeader, R.id.recycler_view);
    }

    @Override
    public String getTag() {
        return TAG;
    }

    private void setUpViews() {
        artistName.setText(artist.name);

        ViewUtil.addOnGlobalLayoutListener(artistImage, new Runnable() {
            @Override
            public void run() {
                setUpArtistImageAndApplyPalette(false);
            }
        });
        setUpSongListView();
        setUpAlbumRecyclerView();
        loadBiography();
    }


    private void setNavigationBarColored(boolean colored) {
        if (colored) {
            setNavigationBarColor(toolbarColor);
        } else {
            setNavigationBarColor(Color.BLACK);
        }
    }

    private void setUpSongListView() {
        songListView.setScrollViewCallbacks(observableScrollViewCallbacks);
        songListView.setPadding(0, artistImageViewHeight + titleViewHeight, 0, bottomOffset);
        songListView.addHeaderView(songListHeader);

        songs = ArtistSongLoader.getArtistSongList(this, artist.id);
        songAdapter = new ArtistSongAdapter(this, songs, this);
        songListView.setAdapter(songAdapter);

        final View contentView = getWindow().getDecorView().findViewById(android.R.id.content);
        contentView.post(new Runnable() {
            @Override
            public void run() {
                songListBackground.getLayoutParams().height = contentView.getHeight();
                observableScrollViewCallbacks.onScrollChanged(-(artistImageViewHeight + titleViewHeight), false, false);
            }
        });
    }

    private void setUpAlbumRecyclerView() {
        albumRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        albums = ArtistAlbumLoader.getArtistAlbumList(this, artist.id);
        albumAdapter = new ArtistAlbumAdapter(this, albums, this);
        albumRecyclerView.setAdapter(albumAdapter);
    }

    private void loadBiography() {
        lastFMRestClient.getApiService().getArtistInfo(artist.name, null, new Callback<ArtistInfo>() {
            @Override
            public void success(ArtistInfo artistInfo, Response response) {
                if (artistInfo.getArtist() != null) {
                    String bio = artistInfo.getArtist().getBio().getContent();
                    if (bio != null && !bio.trim().equals("")) {
                        biography = Html.fromHtml(bio);
                        return;
                    }
                }
                biography = null;
            }

            @Override
            public void failure(RetrofitError error) {
                biography = null;
            }
        });
    }

    private MaterialDialog getBiographyDialog() {
        return new MaterialDialog.Builder(ArtistDetailActivity.this)
                .title(artist.name)
                .content(biography)
                .positiveText(android.R.string.ok)
                .build();
    }

    private void setUpArtistImageAndApplyPalette(final boolean forceDownload) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        if (defaultArtistImageBlurManager == null) {
            defaultArtistImageBlurManager = new StackBlurManager(BitmapFactory.decodeResource(getResources(), R.drawable.default_artist_image, options));
        }
        if (MusicUtil.isArtistNameUnknown(artist.name)) {
            artistImage.setImageResource(R.drawable.default_artist_image);
            resetPaletteAndArtistImageBackground();
            return;
        }
        lastFMRestClient.getApiService().getArtistInfo(artist.name, forceDownload ? "no-cache" : null, new Callback<ArtistInfo>() {
            @Override
            public void success(ArtistInfo artistInfo, Response response) {
                if (artistInfo.getArtist() != null) {
                    List<Image> images = artistInfo.getArtist().getImage();
                    int lastElementIndex = images.size() - 1;
                    ImageLoader.getInstance().displayImage(images.get(lastElementIndex).getText(),
                            artistImage,
                            new DisplayImageOptions.Builder()
                                    .cacheInMemory(true)
                                    .cacheOnDisk(true)
                                    .showImageOnFail(R.drawable.default_artist_image)
                                    .showImageForEmptyUri(R.drawable.default_artist_image)
                                    .resetViewBeforeLoading(true)
                                    .build(),
                            new SimpleImageLoadingListener() {
                                @Override
                                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                    resetPaletteAndArtistImageBackground();
                                    toastUpdatedArtistImageIfDownloadWasForced();
                                }

                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    if (loadedImage != null) {
                                        applyPalette(loadedImage);
                                        artistImageBackground.setImageBitmap(new StackBlurManager(loadedImage).process(10));
                                    } else {
                                        resetPaletteAndArtistImageBackground();
                                    }
                                    toastUpdatedArtistImageIfDownloadWasForced();
                                }

                                private void toastUpdatedArtistImageIfDownloadWasForced() {
                                    if (forceDownload) {
                                        Toast.makeText(ArtistDetailActivity.this, getString(R.string.updated_artist_image), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                    );
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (forceDownload) {
                    Toast.makeText(ArtistDetailActivity.this, getString(R.string.could_not_update_artist_image), Toast.LENGTH_SHORT).show();
                } else {
                    artistImage.setImageResource(R.drawable.default_artist_image);
                    resetPaletteAndArtistImageBackground();
                }
            }
        });
    }

    private void resetPaletteAndArtistImageBackground() {
        applyPalette(null);
        artistImageBackground.setImageBitmap(defaultArtistImageBlurManager.process(10));
    }

    private void applyPalette(Bitmap bitmap) {
        if (bitmap != null) {
            Palette.from(bitmap)
                    .generate(new Palette.PaletteAsyncListener() {

                        @Override
                        public void onGenerated(Palette palette) {
                            final Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                            if (vibrantSwatch != null) {
                                toolbarColor = vibrantSwatch.getRgb();
                                artistName.setBackgroundColor(vibrantSwatch.getRgb());
                                artistName.setTextColor(Util.getOpaqueColor(vibrantSwatch.getTitleTextColor()));
                                if (Util.isAtLeastLollipop() && PreferenceUtils.getInstance(ArtistDetailActivity.this).coloredNavigationBarArtist())
                                    setNavigationBarColor(vibrantSwatch.getRgb());
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

    @Override
    protected boolean overridesTaskColor() {
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
            albumAdapter.notifyDataSetChanged();
    }

    @Override
    public int getPaletteColor() {
        return toolbarColor;
    }


    private void resetColors() {
        int titleTextColor = DialogUtils.resolveColor(this, R.attr.title_text_color);
        int defaultBarColor = DialogUtils.resolveColor(this, R.attr.default_bar_color);

        toolbarColor = defaultBarColor;
        artistName.setBackgroundColor(defaultBarColor);
        artistName.setTextColor(titleTextColor);

        if (Util.isAtLeastLollipop() && PreferenceUtils.getInstance(this).coloredNavigationBarArtist())
            setNavigationBarColor(DialogUtils.resolveColor(this, R.attr.default_bar_color));

        notifyTaskColorChange(toolbarColor);
    }

    private void getIntentExtras() {
        Bundle intentExtras = getIntent().getExtras();
        final int artistId = intentExtras.getInt(EXTRA_ARTIST_ID);
        artist = ArtistLoader.getArtist(this, artistId);
        if (artist == null) {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_artist_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_equalizer:
                NavigationUtil.openEqualizer(this);
                return true;
            case R.id.action_shuffle_artist:
                MusicPlayerRemote.openAndShuffleQueue(this, songs, true);
                return true;
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_playing_queue:
                NavigationUtil.openPlayingQueueDialog(this);
                return true;
            case R.id.action_biography:
                if (biography != null) {
                    getBiographyDialog().show();
                } else {
                    Toast.makeText(ArtistDetailActivity.this, getResources().getString(R.string.biography_unavailable), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_re_download_artist_image:
                Toast.makeText(ArtistDetailActivity.this, getResources().getString(R.string.updating), Toast.LENGTH_SHORT).show();
                setUpArtistImageAndApplyPalette(true);
                return true;
            case R.id.action_now_playing:
                NavigationUtil.openCurrentPlayingIfPossible(this, getSharedViewsWithFab(null));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void enableViews() {
        super.enableViews();
        songListView.setEnabled(true);
        toolbar.setEnabled(true);
        albumRecyclerView.setEnabled(true);
    }

    @Override
    public void disableViews() {
        super.disableViews();
        songListView.setEnabled(false);
        toolbar.setEnabled(false);
        albumRecyclerView.setEnabled(false);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void fixLollipopTransitionImageWrongSize() {
        getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                setUpArtistImageAndApplyPalette(false);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });
    }

    @Subscribe
    public void onDataBaseEvent(DataBaseChangedEvent event) {
        switch (event.getAction()) {
            case DataBaseChangedEvent.SONGS_CHANGED:
            case DataBaseChangedEvent.ALBUMS_CHANGED:
            case DataBaseChangedEvent.ARTISTS_CHANGED:
            case DataBaseChangedEvent.DATABASE_CHANGED:
                songs = ArtistSongLoader.getArtistSongList(this, artist.id);
                songAdapter.updateDataSet(songs);
                albums = ArtistAlbumLoader.getArtistAlbumList(this, artist.id);
                albumAdapter.updateDataSet(albums);
                if (songs.size() < 1) finish();
                break;
        }
    }

    @Subscribe
    public void onUIPreferenceChanged(UIPreferenceChangedEvent event) {
        switch (event.getAction()) {
            case UIPreferenceChangedEvent.COLORED_NAVIGATION_BAR_ARTIST_CHANGED:
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
                        setStatusBarColor(Util.getOpaqueColor(toolbarColor));
                        return callback.onCabCreated(materialCab, menu);
                    }

                    @Override
                    public boolean onCabItemClicked(MenuItem menuItem) {
                        return callback.onCabItemClicked(menuItem);
                    }

                    @Override
                    public boolean onCabFinished(MaterialCab materialCab) {
                        setStatusBarColor(Util.getColorWithAlpha(toolbarAlpha, toolbarColor));
                        return callback.onCabFinished(materialCab);
                    }
                });
        return cab;
    }

    @Override
    public void onBackPressed() {
        if (cab != null && cab.isActive()) cab.finish();
        else {
            albumRecyclerView.stopScroll();
            super.onBackPressed();
        }
    }
}