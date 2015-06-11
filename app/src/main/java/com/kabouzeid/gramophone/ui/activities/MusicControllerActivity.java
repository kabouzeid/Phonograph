package com.kabouzeid.gramophone.ui.activities;

import android.animation.Animator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.ThemeSingleton;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.SongDetailDialog;
import com.kabouzeid.gramophone.dialogs.SongShareDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.bitmapblur.StackBlurManager;
import com.kabouzeid.gramophone.loader.SongFilePathLoader;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.misc.SmallTransitionListener;
import com.kabouzeid.gramophone.model.MusicRemoteEvent;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.SongTagEditorActivity;
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

import java.io.File;

public class MusicControllerActivity extends AbsFabActivity {

    public static final String TAG = MusicControllerActivity.class.getSimpleName();
    private static final int COLOR_TRANSITION_TIME = 400;

    private Song song;
    private SquareIfPlaceImageView albumArt;
    private ImageView albumArtBackground;
    private TextView songTitle;
    private TextView songArtist;
    private TextView currentSongProgress;
    private TextView totalSongDuration;
    private View footer;
    private SeekBar progressSlider;
    private ImageButton nextButton;
    private ImageButton prevButton;
    private ImageButton repeatButton;
    private ImageButton shuffleButton;
    private View mediaControllerContainer;
    private CardView playbackControllerCard;
    private Toolbar toolbar;
    private int lastFooterColor = -1;
    private int lastTextColor = -2;
    private Thread progressViewsUpdateThread;

    private final boolean opaqueStatusBar = PreferenceUtils.getInstance(this).opaqueStatusbarNowPlaying();
    private final boolean opaqueToolBar = opaqueStatusBar && PreferenceUtils.getInstance(this).opaqueToolbarNowPlaying();
    private final boolean forceSquareAlbumArt = PreferenceUtils.getInstance(this).forceAlbumArtSquared();
    private final boolean smallerTitleBox = PreferenceUtils.getInstance(this).smallerTitileBoxNowPlaying();
    private final boolean traditionalProgressSlider = PreferenceUtils.getInstance(this).traditionalProgressSliderNowPlaying();
    private final boolean showPlaybackControllerCard = PreferenceUtils.getInstance(this).playbackControllerCardNowPlaying();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setStatusBarTransparent();
        super.onCreate(savedInstanceState);

        setContentView(traditionalProgressSlider ? R.layout.activity_music_controller_traditional_progress_slider : R.layout.activity_music_controller);

        initViews();
        moveSeekBarIntoPlace();
        adjustTitleBoxSize();
        setUpPlaybackControllerCard();
        setUpMusicControllers();

        albumArtBackground.setAlpha(0.7f);
        albumArt.forceSquare(forceSquareAlbumArt);

        if (opaqueStatusBar) {
            if (opaqueToolBar) {
                alignAlbumArtToToolbar();
            } else {
                alignAlbumArtToStatusBar();
            }
        } else {
            alignAlbumArtToTop();
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getEnterTransition().addListener(new SmallTransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                    mediaControllerContainer.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    int cx = (getFab().getLeft() + getFab().getRight()) / 2;
                    int cy = (getFab().getTop() + getFab().getBottom()) / 2;
                    int finalRadius = Math.max(mediaControllerContainer.getWidth(), mediaControllerContainer.getHeight());

                    Animator animator = ViewAnimationUtils.createCircularReveal(mediaControllerContainer, cx, cy, getFab().getWidth() / 2, finalRadius);
                    animator.setInterpolator(new DecelerateInterpolator());
                    animator.setDuration(1000);
                    animator.start();

                    mediaControllerContainer.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getFab().setOnLongClickListener(null);
    }


    private void moveSeekBarIntoPlace() {
        if (traditionalProgressSlider) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) progressSlider.getLayoutParams();
            progressSlider.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            final int seekBarMarginLeftRight = getResources().getDimensionPixelSize(R.dimen.seek_bar_margin_left_right);
            lp.setMargins(seekBarMarginLeftRight, 0, seekBarMarginLeftRight, -(progressSlider.getMeasuredHeight() / 2));
            progressSlider.setLayoutParams(lp);
        }
    }

    private void adjustTitleBoxSize() {
        int paddingTopBottom = smallerTitleBox ? getResources().getDimensionPixelSize(R.dimen.title_box_padding_small) : getResources().getDimensionPixelSize(R.dimen.title_box_padding_large);
        footer.setPadding(footer.getPaddingLeft(), paddingTopBottom, footer.getPaddingRight(), paddingTopBottom);

        songTitle.setPadding(songTitle.getPaddingLeft(), songTitle.getPaddingTop(), songTitle.getPaddingRight(), smallerTitleBox ? getResources().getDimensionPixelSize(R.dimen.title_box_text_spacing_small) : getResources().getDimensionPixelSize(R.dimen.title_box_text_spacing_large));
        songArtist.setPadding(songArtist.getPaddingLeft(), smallerTitleBox ? getResources().getDimensionPixelSize(R.dimen.title_box_text_spacing_small) : getResources().getDimensionPixelSize(R.dimen.title_box_text_spacing_large), songArtist.getPaddingRight(), songArtist.getPaddingBottom());

        songTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallerTitleBox ? getResources().getDimensionPixelSize(R.dimen.title_box_title_text_size_small) : getResources().getDimensionPixelSize(R.dimen.title_box_title_text_size_large));
        songArtist.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallerTitleBox ? getResources().getDimensionPixelSize(R.dimen.title_box_caption_text_size_small) : getResources().getDimensionPixelSize(R.dimen.title_box_caption_text_size_large));
    }

    private void setUpPlaybackControllerCard() {
        playbackControllerCard.setVisibility(showPlaybackControllerCard ? View.VISIBLE : View.GONE);
        mediaControllerContainer.setBackgroundColor(showPlaybackControllerCard ? Color.TRANSPARENT : Util.resolveColor(this, R.attr.music_controller_container_color));
    }

    private void initViews() {
        nextButton = (ImageButton) findViewById(R.id.next_button);
        prevButton = (ImageButton) findViewById(R.id.prev_button);
        repeatButton = (ImageButton) findViewById(R.id.repeat_button);
        shuffleButton = (ImageButton) findViewById(R.id.shuffle_button);
        albumArt = (SquareIfPlaceImageView) findViewById(R.id.album_art);
        albumArtBackground = (ImageView) findViewById(R.id.album_art_background);
        songTitle = (TextView) findViewById(R.id.song_title);
        songArtist = (TextView) findViewById(R.id.song_artist);
        currentSongProgress = (TextView) findViewById(R.id.song_current_progress);
        totalSongDuration = (TextView) findViewById(R.id.song_total_time);
        footer = findViewById(R.id.footer);
        progressSlider = (SeekBar) findViewById(R.id.progress_slider);
        mediaControllerContainer = findViewById(R.id.media_controller_container);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        playbackControllerCard = (CardView) findViewById(R.id.playback_controller_card);
    }

    private void setUpMusicControllers() {
        setUpPrevNext();
        setUpRepeatButton();
        setUpShuffleButton();
        setUpProgressSlider();
    }

    private void setTint(SeekBar seekBar, int color) {
        ColorStateList s1 = ColorStateList.valueOf(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            seekBar.setThumbTintList(s1);
            if (traditionalProgressSlider) seekBar.setProgressTintList(s1);
        } else {
            seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            if (traditionalProgressSlider)
                seekBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    private void setUpProgressSlider() {
        progressSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    MusicPlayerRemote.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setUpPrevNext() {
        nextButton.setImageDrawable(Util.getTintedDrawable(this,
                R.drawable.ic_skip_next_white_48dp, DialogUtils.resolveColor(this, R.attr.themed_drawable_color)));
        prevButton.setImageDrawable(Util.getTintedDrawable(this,
                R.drawable.ic_skip_previous_white_48dp, DialogUtils.resolveColor(this, R.attr.themed_drawable_color)));
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayerRemote.playNextSong();
            }
        });
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayerRemote.back();
            }
        });
    }

    private void setUpShuffleButton() {
        updateShuffleState();
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayerRemote.toggleShuffleMode();
            }
        });
    }

    private void updateShuffleState() {
        switch (MusicPlayerRemote.getShuffleMode()) {
            case MusicService.SHUFFLE_MODE_SHUFFLE:
                shuffleButton.setImageDrawable(Util.getTintedDrawable(this, R.drawable.ic_shuffle_white_48dp,
                        getThemeColorAccent() == Color.WHITE ? Color.BLACK : getThemeColorAccent()));
                break;
            default:
                shuffleButton.setImageDrawable(Util.getTintedDrawable(this, R.drawable.ic_shuffle_white_48dp,
                        DialogUtils.resolveColor(this, R.attr.themed_drawable_color)));
                break;
        }
    }

    private void setUpRepeatButton() {
        updateRepeatState();
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayerRemote.cycleRepeatMode();
            }
        });
    }

    private void updateRepeatState() {
        switch (MusicPlayerRemote.getRepeatMode()) {
            case MusicService.REPEAT_MODE_NONE:
                repeatButton.setImageDrawable(Util.getTintedDrawable(this, R.drawable.ic_repeat_white_48dp,
                        DialogUtils.resolveColor(this, R.attr.themed_drawable_color)));
                break;
            case MusicService.REPEAT_MODE_ALL:
                repeatButton.setImageDrawable(Util.getTintedDrawable(this, R.drawable.ic_repeat_white_48dp,
                        getThemeColorAccent() == Color.WHITE ? Color.BLACK : getThemeColorAccent()));
                break;
            default:
                repeatButton.setImageDrawable(Util.getTintedDrawable(this, R.drawable.ic_repeat_one_white_48dp,
                        getThemeColorAccent() == Color.WHITE ? Color.BLACK : getThemeColorAccent()));
                break;
        }
    }


    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateControllerState();
        startProgressViewsUpdateThread();
        updateCurrentSong();
    }

    private void updateCurrentSong() {
        getCurrentSong();
        setHeadersText();
        setUpAlbumArtAndApplyPalette();
        totalSongDuration.setText(MusicUtil.getReadableDurationString(song.duration));
        currentSongProgress.setText(MusicUtil.getReadableDurationString(-1));
    }

    private void setHeadersText() {
        songTitle.setText(song.title);
        songArtist.setText(song.artistName);
    }

    @Override
    protected boolean overridesTaskColor() {
        return true;
    }

    private void setUpAlbumArtAndApplyPalette() {
        ImageLoader.getInstance().displayImage(
                MusicUtil.getAlbumArtUri(song.albumId).toString(),
                albumArt,
                new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .showImageOnFail(R.drawable.default_album_art)
                        .build(),
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        applyPalette(null);
                        albumArtBackground.setImageBitmap(new StackBlurManager(BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art)).process(10));
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        applyPalette(loadedImage);
                        albumArtBackground.setImageBitmap(new StackBlurManager(loadedImage).process(10));
                    }
                }
        );
    }

    private void applyPalette(Bitmap bitmap) {
        if (bitmap != null) {
            Palette.from(bitmap)
                    .generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            final Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                            if (vibrantSwatch != null) {
                                final int swatchRgb = vibrantSwatch.getRgb();
                                animateColorChange(swatchRgb);
                                animateTextColorChange(Util.getOpaqueColor(vibrantSwatch.getTitleTextColor()));
                                notifyTaskColorChange(swatchRgb);
                            } else {
                                resetColors();
                            }
                        }
                    });
        } else {
            resetColors();
        }
    }

    private void resetColors() {
        final int textColor = Util.getOpaqueColor(DialogUtils.resolveColor(this, R.attr.title_text_color));
        final int defaultBarColor = DialogUtils.resolveColor(this, R.attr.default_bar_color);

        animateColorChange(defaultBarColor);
        animateTextColorChange(textColor);

        notifyTaskColorChange(defaultBarColor);
    }


    private void animateColorChange(final int newColor) {
        if (lastFooterColor != -1 && lastFooterColor != newColor) {
            ViewUtil.animateViewColor(footer, lastFooterColor, newColor, COLOR_TRANSITION_TIME);

            if (opaqueToolBar)
                ViewUtil.animateViewColor(toolbar, lastFooterColor, newColor, COLOR_TRANSITION_TIME);
            else toolbar.setBackgroundColor(Color.TRANSPARENT);
        } else {
            footer.setBackgroundColor(newColor);

            if (opaqueToolBar) toolbar.setBackgroundColor(newColor);
            else toolbar.setBackgroundColor(Color.TRANSPARENT);
        }

        setTint(progressSlider, !ThemeSingleton.get().darkTheme && getThemeColorAccent() == Color.WHITE ? Color.BLACK : getThemeColorAccent());
        if (opaqueStatusBar) setStatusBarColor(newColor);
        else setStatusBarColor(Color.TRANSPARENT);

        if (Util.isAtLeastLollipop() && PreferenceUtils.getInstance(this).coloredNavigationBarCurrentPlayingEnabled())
            setNavigationBarColor(newColor);
        lastFooterColor = newColor;
    }

    private void animateTextColorChange(final int newColor) {
        if (lastTextColor != -2 && lastTextColor != newColor) {
            ViewUtil.animateTextColor(songTitle, lastTextColor, newColor, COLOR_TRANSITION_TIME);
            ViewUtil.animateTextColor(songArtist, lastTextColor, newColor, COLOR_TRANSITION_TIME);
        } else {
            songTitle.setTextColor(newColor);
            songArtist.setTextColor(newColor);
        }
        lastTextColor = newColor;
    }

    private void getCurrentSong() {
        song = MusicPlayerRemote.getCurrentSong();
        if (song.id == -1) {
            finish();
        }
    }

    private void startProgressViewsUpdateThread() {
        if (progressViewsUpdateThread != null) progressViewsUpdateThread.interrupt();
        progressViewsUpdateThread = new Thread(new Runnable() {
            int totalMillis = 0;
            int progressMillis = 0;

            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        totalMillis = MusicPlayerRemote.getSongDurationMillis();
                        progressMillis = MusicPlayerRemote.getSongProgressMillis();

                        runOnUiThread(updateProgressViews);

                        Thread.sleep(100);
                    }
                } catch (InterruptedException ignored) {
                }
            }

            private Runnable updateProgressViews = new Runnable() {
                @Override
                public void run() {
                    progressSlider.setMax(totalMillis);
                    progressSlider.setProgress(progressMillis);
                    currentSongProgress.setText(MusicUtil.getReadableDurationString(progressMillis));
                }
            };
        });
        progressViewsUpdateThread.start();
    }

    protected void updateControllerState() {
        updateFabState();
        updateRepeatState();
        updateShuffleState();
    }

    @Override
    public void onMusicRemoteEvent(MusicRemoteEvent event) {
        super.onMusicRemoteEvent(event);
        switch (event.getAction()) {
            case MusicRemoteEvent.TRACK_CHANGED:
                updateCurrentSong();
                break;
            case MusicRemoteEvent.REPEAT_MODE_CHANGED:
                updateRepeatState();
                break;
            case MusicRemoteEvent.SHUFFLE_MODE_CHANGED:
                updateShuffleState();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(progressViewsUpdateThread != null) progressViewsUpdateThread.interrupt();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_music_playing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_share:
                SongShareDialog.create(song.id).show(getSupportFragmentManager(), "SHARE_SONG");
                return true;
            case R.id.action_equalizer:
                NavigationUtil.openEqualizer(this);
                return true;
            case R.id.action_shuffle_all:
                MusicPlayerRemote.shuffleAllSongs(this);
                return true;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(song).show(getSupportFragmentManager(), "ADD_PLAYLIST");
                return true;
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_playing_queue:
                NavigationUtil.openPlayingQueueDialog(this);
                return true;
            case R.id.action_tag_editor:
                Intent intent = new Intent(this, SongTagEditorActivity.class);
                intent.putExtra(AppKeys.E_ID, song.id);
                startActivity(intent);
                return true;
            case R.id.action_details:
                String songFilePath = SongFilePathLoader.getSongFilePath(this, song.id);
                File songFile = new File(songFilePath);
                SongDetailDialog.create(songFile).show(getSupportFragmentManager(), "SONG_DETAIL");
                return true;
            case R.id.action_go_to_album:
                NavigationUtil.goToAlbum(this, song.albumId, getSharedViewsWithFab(null));
                return true;
            case R.id.action_go_to_artist:
                NavigationUtil.goToArtist(this, song.artistId, getSharedViewsWithFab(null));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void alignAlbumArtToTop() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) findViewById(R.id.album_art_frame).getLayoutParams();
        if (Build.VERSION.SDK_INT > 16) {
            params.removeRule(RelativeLayout.BELOW);
        } else {
            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.ABOVE, R.id.footer_frame);
        }
    }

    private void alignAlbumArtToToolbar() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) findViewById(R.id.album_art_frame).getLayoutParams();
        params.addRule(RelativeLayout.BELOW, R.id.toolbar);
    }

    private void alignAlbumArtToStatusBar() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) findViewById(R.id.album_art_frame).getLayoutParams();
        params.addRule(RelativeLayout.BELOW, R.id.status_bar);
    }
}