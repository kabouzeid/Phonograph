package com.kabouzeid.gramophone.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.album.HorizontalAlbumAdapter;
import com.kabouzeid.gramophone.adapter.song.ArtistSongAdapter;
import com.kabouzeid.gramophone.dialogs.SleepTimerDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.interfaces.PaletteColorHolder;
import com.kabouzeid.gramophone.lastfm.rest.LastFMRestClient;
import com.kabouzeid.gramophone.lastfm.rest.model.LastFmArtist;
import com.kabouzeid.gramophone.loader.ArtistAlbumLoader;
import com.kabouzeid.gramophone.loader.ArtistLoader;
import com.kabouzeid.gramophone.loader.ArtistSongLoader;
import com.kabouzeid.gramophone.misc.SimpleObservableScrollViewCallbacks;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.Util;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Be careful when changing things in this Activity!
 */
public class ArtistDetailActivity extends AbsSlidingMusicPanelActivity implements PaletteColorHolder, CabHolder {

    public static final String TAG = ArtistDetailActivity.class.getSimpleName();

    public static final String EXTRA_ARTIST_ID = "extra_artist_id";

    @Bind(R.id.image)
    ImageView artistImage;
    @Bind(R.id.list_background)
    View songListBackground;
    @Bind(R.id.list)
    ObservableListView songListView;
    @Bind(R.id.title)
    TextView artistName;
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    View songListHeader;
    RecyclerView albumRecyclerView;

    private MaterialCab cab;
    private int headerOffset;
    private int titleViewHeight;
    private int artistImageViewHeight;
    private int toolbarColor;
    private float toolbarAlpha;

    private Artist artist;
    @Nullable
    private Spanned biography;
    private HorizontalAlbumAdapter albumAdapter;
    private ArtistSongAdapter songAdapter;

    private LastFMRestClient lastFMRestClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarTransparent();
        ButterKnife.bind(this);

        if (shouldColorNavigationBar())
            setNavigationBarColor(DialogUtils.resolveColor(this, R.attr.default_bar_color));

        lastFMRestClient = new LastFMRestClient(this);

        getArtistFromIntentExtras();
        initViews();
        setUpObservableListViewParams();
        setUpViews();
        setUpToolbar();
    }

    @Override
    protected View createContentView() {
        return wrapSlidingMusicPanel(R.layout.activity_artist_detail);
    }

    private final SimpleObservableScrollViewCallbacks observableScrollViewCallbacks = new SimpleObservableScrollViewCallbacks() {
        @Override
        public void onScrollChanged(int scrollY, boolean b, boolean b2) {
            scrollY += artistImageViewHeight + titleViewHeight;
            float flexibleRange = artistImageViewHeight - headerOffset;

            // Translate album cover
            artistImage.setTranslationY(Math.max(-artistImageViewHeight, -scrollY / 2));

            // Translate list background
            songListBackground.setTranslationY(Math.max(0, -scrollY + artistImageViewHeight));

            // Change alpha of overlay
            toolbarAlpha = Math.max(0, Math.min(1, (float) scrollY / flexibleRange));
            toolbar.setBackgroundColor(ColorUtil.getColorWithAlpha(toolbarAlpha, toolbarColor));
            setStatusBarColor(ColorUtil.getColorWithAlpha(cab != null && cab.isActive() ? 1 : toolbarAlpha, toolbarColor));

            // Translate name text
            int maxTitleTranslationY = artistImageViewHeight;
            int titleTranslationY = maxTitleTranslationY - scrollY;
            titleTranslationY = Math.max(headerOffset, titleTranslationY);

            artistName.setTranslationY(titleTranslationY);
        }
    };

    private void setUpObservableListViewParams() {
        artistImageViewHeight = getResources().getDimensionPixelSize(R.dimen.header_image_height);
        toolbarColor = DialogUtils.resolveColor(this, R.attr.default_bar_color);
        int toolbarHeight = Util.getActionBarSize(this);
        titleViewHeight = getResources().getDimensionPixelSize(R.dimen.title_view_height);
        headerOffset = toolbarHeight;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            headerOffset += getResources().getDimensionPixelSize(R.dimen.status_bar_padding);
        }
    }

    private void initViews() {
        songListHeader = LayoutInflater.from(this).inflate(R.layout.artist_detail_header, songListView, false);
        albumRecyclerView = ButterKnife.findById(songListHeader, R.id.recycler_view);
    }

    private void setUpViews() {
        artistName.setText(artist.name);
        setUpArtistImageAndApplyPalette(false);
        setUpSongListView();
        setUpAlbumRecyclerView();
        loadBiography();
    }

    private void setUpSongListView() {
        setUpSongListPadding();
        songListView.setScrollViewCallbacks(observableScrollViewCallbacks);
        songListView.addHeaderView(songListHeader);

        songAdapter = new ArtistSongAdapter(this, loadSongDataSet(), this);
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

    private void setUpSongListPadding() {
        songListView.setPadding(0, artistImageViewHeight + titleViewHeight, 0, 0);
    }

    private void setUpAlbumRecyclerView() {
        albumRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        albumAdapter = new HorizontalAlbumAdapter(this, loadAlbumDataSet(), this);
        albumRecyclerView.setAdapter(albumAdapter);
        albumAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (albumAdapter.getItemCount() == 0) finish();
            }
        });
    }

    private void reloadDataSets() {
        songAdapter.swapDataSet(loadSongDataSet());
        albumAdapter.swapDataSet(loadAlbumDataSet());
    }

    private ArrayList<Song> loadSongDataSet() {
        return ArtistSongLoader.getArtistSongList(this, artist.id);
    }

    private ArrayList<Album> loadAlbumDataSet() {
        return ArtistAlbumLoader.getArtistAlbumList(this, artist.id);
    }

    private void loadBiography() {
        lastFMRestClient.getApiService().getArtistInfo(artist.name, null).enqueue(new Callback<LastFmArtist>() {
            @Override
            public void onResponse(Response<LastFmArtist> response, Retrofit retrofit) {
                LastFmArtist lastFmArtist = response.body();
                if (lastFmArtist.getArtist() != null) {
                    String bio = lastFmArtist.getArtist().getBio().getContent();
                    if (bio != null && !bio.trim().equals("")) {
                        biography = Html.fromHtml(bio);
                        return;
                    }
                }
                biography = null;
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                biography = null;
            }
        });
    }

    private MaterialDialog getBiographyDialog() {
        return new MaterialDialog.Builder(ArtistDetailActivity.this)
                .title(artist.name)
                .content(biography != null ? biography : "")
                .positiveText(android.R.string.ok)
                .build();
    }

    private void setUpArtistImageAndApplyPalette(final boolean forceDownload) {
        // TODO Glide
//        ImageLoader.getInstance().displayImage(MusicUtil.getArtistImageLoaderString(artist, forceDownload),
//                artistImage,
//                new DisplayImageOptions.Builder()
//                        .cacheInMemory(true)
//                        .cacheOnDisk(true)
//                        .showImageOnFail(R.drawable.default_artist_image)
//                        .resetViewBeforeLoading(true)
//                        .postProcessor(new BitmapProcessor() {
//                            @Override
//                            public Bitmap process(Bitmap bitmap) {
//                                final int color = ColorUtil.generateColor(ArtistDetailActivity.this, bitmap);
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        setColors(color);
//                                    }
//                                });
//                                return bitmap;
//                            }
//                        })
//                        .build(),
//                new SimpleImageLoadingListener() {
//                    @Override
//                    public void onLoadingFailed(String imageUri, View view, @Nullable FailReason failReason) {
//                        setColors(ColorUtil.resolveColor(ArtistDetailActivity.this, R.attr.default_bar_color));
//
//                        toastUpdatedArtistImageIfDownloadWasForced();
//                    }
//
//                    @Override
//                    public void onLoadingComplete(String imageUri, View view, @Nullable Bitmap loadedImage) {
//                        if (loadedImage == null) {
//                            onLoadingFailed(imageUri, view, null);
//                            return;
//                        }
//
//                        toastUpdatedArtistImageIfDownloadWasForced();
//                    }
//
//                    private void toastUpdatedArtistImageIfDownloadWasForced() {
//                        if (forceDownload) {
//                            Toast.makeText(ArtistDetailActivity.this, getString(R.string.updated_artist_image), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }
//        );
    }

    @Override
    protected boolean overridesTaskColor() {
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            reloadDataSets();
        }
    }

    @Override
    public int getPaletteColor() {
        return toolbarColor;
    }

    private void setColors(int vibrantColor) {
        toolbarColor = vibrantColor;
        artistName.setBackgroundColor(vibrantColor);
        artistName.setTextColor(ColorUtil.getPrimaryTextColorForBackground(this, vibrantColor));

        if (shouldColorNavigationBar())
            setNavigationBarColor(vibrantColor);

        notifyTaskColorChange(vibrantColor);
    }

    private void getArtistFromIntentExtras() {
        Bundle intentExtras = getIntent().getExtras();
        final int artistId = intentExtras.getInt(EXTRA_ARTIST_ID);
        artist = ArtistLoader.getArtist(this, artistId);
        if (artist.id == -1) {
            finish();
        }
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_artist_detail, menu);
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
            case R.id.action_shuffle_artist:
                MusicPlayerRemote.openAndShuffleQueue(songAdapter.getDataSet(), true);
                return true;
            case android.R.id.home:
                super.onBackPressed();
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

    @Override
    public MaterialCab openCab(int menuRes, @NonNull final MaterialCab.Callback callback) {
        if (cab != null && cab.isActive()) cab.finish();
        cab = new MaterialCab(this, R.id.cab_stub)
                .setMenu(menuRes)
                .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
                .setBackgroundColor(ColorUtil.shiftBackgroundColorForLightText(getPaletteColor()))
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
            albumRecyclerView.stopScroll();
            super.onBackPressed();
        }
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        reloadDataSets();
    }
}