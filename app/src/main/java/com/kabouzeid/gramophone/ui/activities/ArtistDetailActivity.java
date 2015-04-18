package com.kabouzeid.gramophone.ui.activities;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.ArtistAlbumAdapter;
import com.kabouzeid.gramophone.adapter.songadapter.ArtistSongAdapter;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.PaletteColorHolder;
import com.kabouzeid.gramophone.lastfm.artist.LastFMArtistBiographyLoader;
import com.kabouzeid.gramophone.lastfm.artist.LastFMArtistImageUrlLoader;
import com.kabouzeid.gramophone.loader.ArtistAlbumLoader;
import com.kabouzeid.gramophone.loader.ArtistLoader;
import com.kabouzeid.gramophone.loader.ArtistSongLoader;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.misc.SmallObservableScrollViewCallbacks;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.model.UIPreferenceChangedEvent;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nineoldandroids.view.ViewHelper;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * A lot of hackery is done in this activity. Changing things may will brake the whole activity.
 * <p/>
 * Should be kinda stable ONLY AS IT IS!!!
 */
public class ArtistDetailActivity extends AbsFabActivity implements PaletteColorHolder {

    public static final String TAG = ArtistDetailActivity.class.getSimpleName();
    private Artist artist;

    private ObservableListView songListView;
    private ImageView artistImage;
    private View songsBackgroundView;
    private TextView artistNameTv;
    private int headerOffset;
    private int titleViewHeight;
    private int artistImageViewHeight;
    private int toolbarColor;
    public Toolbar toolbar;

    private View songListHeader;
    private RecyclerView albumRecyclerView;
    private Spanned biography;

    private final SmallObservableScrollViewCallbacks observableScrollViewCallbacks = new SmallObservableScrollViewCallbacks() {
        @Override
        public void onScrollChanged(int scrollY, boolean b, boolean b2) {
            scrollY += artistImageViewHeight + titleViewHeight;
            super.onScrollChanged(scrollY, b, b2);
            float flexibleRange = artistImageViewHeight - headerOffset;

            // Translate album cover
            ViewHelper.setTranslationY(artistImage, Math.max(-artistImageViewHeight, -scrollY / 2));

            // Translate list background
            ViewHelper.setTranslationY(songsBackgroundView, Math.max(0, -scrollY + artistImageViewHeight));

            // Change alpha of overlay
            float alpha = Math.max(0, Math.min(1, (float) scrollY / flexibleRange));
            ViewUtil.setBackgroundAlpha(toolbar, alpha, toolbarColor);

            // Translate name text
            int maxTitleTranslationY = artistImageViewHeight;
            int titleTranslationY = maxTitleTranslationY - scrollY;
            titleTranslationY = Math.max(headerOffset, titleTranslationY);

            ViewHelper.setTranslationY(artistNameTv, titleTranslationY);

            // Translate FAB
            int fabTranslationY = titleTranslationY + titleViewHeight - (getFab().getHeight() / 2);
            ViewHelper.setTranslationY(getFab(), fabTranslationY);
        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUpTranslucence(true, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);

        App.bus.register(this);

        if (Util.hasLollipopSDK()) postponeEnterTransition();
        if (Util.hasLollipopSDK() && PreferenceUtils.getInstance(this).coloredNavigationBarArtistEnabled())
            getWindow().setNavigationBarColor(DialogUtils.resolveColor(this, R.attr.default_bar_color));

        getIntentExtras();
        initViews();
        setUpObservableListViewParams();
        setUpViews();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Util.hasLollipopSDK()) fixLollipopTransitionImageWrongSize();
        if (Util.hasLollipopSDK()) startPostponedEnterTransition();
    }

    @Override
    protected boolean shouldColorStatusBar() {
        return false;
    }

    @Override
    protected boolean shouldColorNavBar() {
        return false;
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        artistImage = (ImageView) findViewById(R.id.artist_image);
        songListView = (ObservableListView) findViewById(R.id.list);
        artistNameTv = (TextView) findViewById(R.id.artist_name);
        songsBackgroundView = findViewById(R.id.list_background);

        songListHeader = LayoutInflater.from(this).inflate(R.layout.artist_detail_header, songListView, false);
        albumRecyclerView = (RecyclerView) songListHeader.findViewById(R.id.recycler_view);
    }

    private void setUpObservableListViewParams() {
        artistImageViewHeight = getResources().getDimensionPixelSize(R.dimen.header_image_height);
        toolbarColor = DialogUtils.resolveColor(this, R.attr.default_bar_color);
        int toolbarHeight = Util.getActionBarSize(this);
        titleViewHeight = getResources().getDimensionPixelSize(R.dimen.title_view_height);
        headerOffset = toolbarHeight;
        if (Util.hasKitKatSDK() && !Util.hasLollipopSDK())
            headerOffset += getResources().getDimensionPixelSize(R.dimen.statusMargin);
    }

    @Override
    public String getTag() {
        return TAG;
    }

    private void setUpViews() {
        artistNameTv.setText(artist.name);

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setNavigationBarColored(boolean colored) {
        if (colored) {
            if (Util.hasLollipopSDK()) getWindow().setNavigationBarColor(toolbarColor);
        } else {
            if (Util.hasLollipopSDK()) getWindow().setNavigationBarColor(Color.BLACK);
        }
    }

    private void setUpSongListView() {
        songListView.setScrollViewCallbacks(observableScrollViewCallbacks);
        songListView.setPadding(0, artistImageViewHeight + titleViewHeight, 0, 0);
        songListView.addHeaderView(songListHeader);

        final ArrayList<Song> songs = ArtistSongLoader.getArtistSongList(this, artist.id);
        ArtistSongAdapter songAdapter = new ArtistSongAdapter(this, songs);
        songListView.setAdapter(songAdapter);

        final View contentView = getWindow().getDecorView().findViewById(android.R.id.content);
        contentView.post(new Runnable() {
            @Override
            public void run() {
                songsBackgroundView.getLayoutParams().height = contentView.getHeight();
                observableScrollViewCallbacks.onScrollChanged(-(artistImageViewHeight + titleViewHeight), false, false);
            }
        });

        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // header view has position 0
                if (position == 0) {
                    return;
                }
                MusicPlayerRemote.openQueue(songs, position - 1, true);
            }
        });
    }

    private void setUpAlbumRecyclerView() {
        albumRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<Album> albums = ArtistAlbumLoader.getArtistAlbumList(this, artist.id);
        ArtistAlbumAdapter albumAdapter = new ArtistAlbumAdapter(this, albums);
        albumRecyclerView.setAdapter(albumAdapter);
    }

    private void loadBiography() {
        LastFMArtistBiographyLoader.loadArtistBio(this, artist.name, new LastFMArtistBiographyLoader.ArtistBioLoaderCallback() {
            @Override
            public void onArtistBioLoaded(String bio) {
                if (bio != null && !bio.trim().equals("")) {
                    biography = Html.fromHtml(bio);
                } else {
                    biography = null;
                }
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
        LastFMArtistImageUrlLoader.loadArtistImageUrl(this, artist.name, forceDownload, new LastFMArtistImageUrlLoader.ArtistImageUrlLoaderCallback() {
            @Override
            public void onArtistImageUrlLoaded(final String url) {
                artistImage.post(new Runnable() {
                    @Override
                    public void run() {
                        Ion.with(ArtistDetailActivity.this)
                                .load(url)
                                .withBitmap()
                                .resize(artistImage.getWidth(), artistImage.getHeight())
                                .centerCrop()
                                .asBitmap()
                                .setCallback(new FutureCallback<Bitmap>() {
                                    @Override
                                    public void onCompleted(Exception e, Bitmap result) {
                                        if (result != null) {
                                            artistImage.setImageBitmap(result);
                                            applyPalette(result);
                                        } else {
                                            artistImage.setImageResource(R.drawable.default_artist_image);
                                            resetColors();
                                        }
                                    }
                                });
                    }
                });
            }
        });
    }

    private void applyPalette(Bitmap bitmap) {
        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onGenerated(Palette palette) {
                Palette.Swatch swatch = palette.getVibrantSwatch();
                if (swatch != null) {
                    toolbarColor = swatch.getRgb();
                    artistNameTv.setBackgroundColor(swatch.getRgb());
                    artistNameTv.setTextColor(swatch.getTitleTextColor());
                    if (Util.hasLollipopSDK() && PreferenceUtils.getInstance(ArtistDetailActivity.this).coloredNavigationBarArtistEnabled())
                        getWindow().setNavigationBarColor(swatch.getRgb());
                } else {
                    resetColors();
                }
            }
        });
    }

    @Override
    public int getPaletteColor() {
        return toolbarColor;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void resetColors() {
        int titleTextColor = DialogUtils.resolveColor(this, R.attr.title_text_color);
        int defaultBarColor = DialogUtils.resolveColor(this, R.attr.default_bar_color);

        toolbarColor = defaultBarColor;
        artistNameTv.setBackgroundColor(defaultBarColor);
        artistNameTv.setTextColor(titleTextColor);

        if (Util.hasLollipopSDK() && PreferenceUtils.getInstance(this).coloredNavigationBarArtistEnabled())
            getWindow().setNavigationBarColor(DialogUtils.resolveColor(this, R.attr.default_bar_color));
    }

    private void getIntentExtras() {
        Bundle intentExtras = getIntent().getExtras();
        final int artistId = intentExtras.getInt(AppKeys.E_ARTIST);
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
            case R.id.action_shuffle_all:
                MusicPlayerRemote.shuffleAllSongs(this);
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
            case R.id.action_settings:
                Toast.makeText(this, "This feature is not available yet", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_current_playing:
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
    }

    @Override
    public void disableViews() {
        super.disableViews();
        songListView.setEnabled(false);
        toolbar.setEnabled(false);
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
}
