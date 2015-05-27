package com.kabouzeid.gramophone.ui.activities;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.ThemeSingleton;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.ColorChooserDialog;
import com.kabouzeid.gramophone.dialogs.SongDetailDialog;
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
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;

public class MusicControllerActivity extends AbsFabActivity {

    public static final String TAG = MusicControllerActivity.class.getSimpleName();
    private static final int DEFAULT_DELAY = 350;
    private static final int DEFAULT_ANIMATION_TIME = 1000;

    private Song song;
    private ImageView albumArt;
    private ImageView albumArtBackground;
    private TextView songTitle;
    private TextView songArtist;
    private TextView currentSongProgress;
    private TextView totalSongDuration;
    private View footer;
    private View progressContainer;
    private SeekBar progressSlider;
    private ImageButton nextButton;
    private ImageButton prevButton;
    private ImageButton repeatButton;
    private ImageButton shuffleButton;
    private View mediaControllerContainer;
    private Toolbar toolbar;
    private int lastFooterColor = -1;
    private boolean killThreads = false;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_music_controller);

        initViews();
        albumArtBackground.setAlpha(0.7f);

        moveSeekBarIntoPlace();

        setUpMusicControllers();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Util.isAtLeastLollipop()) {
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

                    int i = footer.getHeight();

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

    @Override
    protected boolean shouldColorStatusBar() {
        return false; // let other code handle this below
    }

    @Override
    protected boolean shouldColorNavBar() {
        return false; // let other code handle this below
    }

    @Override
    protected boolean shouldSetStatusBarTranslucent() {
        return true;
    }

    private void moveSeekBarIntoPlace() {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) progressSlider.getLayoutParams();
        progressSlider.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        final int seekBarMarginLeftRight = getResources().getDimensionPixelSize(R.dimen.seek_bar_margin_left_right);
        lp.setMargins(seekBarMarginLeftRight, getResources().getDimensionPixelSize(R.dimen.progress_container_height) - (progressSlider.getMeasuredHeight() / 2), seekBarMarginLeftRight, 0);
        progressSlider.setLayoutParams(lp);
    }

    private void initViews() {
        nextButton = (ImageButton) findViewById(R.id.next_button);
        prevButton = (ImageButton) findViewById(R.id.prev_button);
        repeatButton = (ImageButton) findViewById(R.id.repeat_button);
        shuffleButton = (ImageButton) findViewById(R.id.shuffle_button);
        albumArt = (ImageView) findViewById(R.id.album_art);
        albumArtBackground = (ImageView) findViewById(R.id.album_art_background);
        songTitle = (TextView) findViewById(R.id.song_title);
        songArtist = (TextView) findViewById(R.id.song_artist);
        currentSongProgress = (TextView) findViewById(R.id.song_current_progress);
        totalSongDuration = (TextView) findViewById(R.id.song_total_time);
        footer = findViewById(R.id.footer);
        progressSlider = (SeekBar) findViewById(R.id.progress_slider);
        mediaControllerContainer = findViewById(R.id.media_controller_container);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        progressContainer = findViewById(R.id.progress_container);
    }

    private void setUpMusicControllers() {
        setUpPrevNext();
        setUpRepeatButton();
        setUpShuffleButton();
        setUpProgressSlider();
    }

    private static void setTint(SeekBar seekBar, int color) {
        ColorStateList s1 = ColorStateList.valueOf(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            seekBar.setThumbTintList(s1);
            seekBar.setProgressTintList(s1);
        } else {
            seekBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);

        }
    }

    private void setUpProgressSlider() {
        setTint(progressSlider, ThemeSingleton.get().positiveColor);
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
                        ThemeSingleton.get().positiveColor));
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
                        ThemeSingleton.get().positiveColor));
                break;
            default:
                repeatButton.setImageDrawable(Util.getTintedDrawable(this, R.drawable.ic_repeat_one_white_48dp,
                        ThemeSingleton.get().positiveColor));
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
        startMusicControllerStateUpdateThread();
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
                                songTitle.setTextColor(vibrantSwatch.getTitleTextColor());
                                songArtist.setTextColor(vibrantSwatch.getBodyTextColor());
                                currentSongProgress.setTextColor(vibrantSwatch.getTitleTextColor());
                                totalSongDuration.setTextColor(vibrantSwatch.getTitleTextColor());
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
        final int songTitleTextColor = DialogUtils.resolveColor(this, R.attr.title_text_color);
        final int artistNameTextColor = DialogUtils.resolveColor(this, R.attr.caption_text_color);
        final int defaultBarColor = DialogUtils.resolveColor(this, R.attr.default_bar_color);

        animateColorChange(defaultBarColor);

        songTitle.setTextColor(songTitleTextColor);
        songArtist.setTextColor(artistNameTextColor);
        currentSongProgress.setTextColor(artistNameTextColor);
        totalSongDuration.setTextColor(artistNameTextColor);

        notifyTaskColorChange(defaultBarColor);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animateColorChange(final int newColor) {
        if (lastFooterColor != -1 && lastFooterColor != newColor) {
            ViewUtil.animateViewColor(footer, lastFooterColor, newColor, 300);
            ViewUtil.animateViewColor(progressContainer, ColorChooserDialog.shiftColorDown(lastFooterColor), ColorChooserDialog.shiftColorDown(newColor), 300);
            ViewUtil.animateViewColor(toolbar, lastFooterColor, newColor, 300);
        } else {
            footer.setBackgroundColor(newColor);
            progressContainer.setBackgroundColor(ColorChooserDialog.shiftColorDown(newColor));
            toolbar.setBackgroundColor(newColor);
        }
        setStatusBarColor(newColor);
        if (Util.isAtLeastLollipop() && PreferenceUtils.getInstance(this).coloredNavigationBarCurrentPlayingEnabled())
            setNavigationBarColor(newColor);
        lastFooterColor = newColor;
    }

    private void getCurrentSong() {
        song = MusicPlayerRemote.getCurrentSong();
        if (song.id == -1) {
            finish();
        }
    }

    private void startMusicControllerStateUpdateThread() {
        killThreads = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                int currentPosition = 0;
                int total = 0;
                while (!killThreads) {
                    try {
                        total = MusicPlayerRemote.getSongDurationMillis();
                        currentPosition = MusicPlayerRemote.getSongProgressMillis();
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        return;
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    final int finalTotal = total;
                    final int finalCurrentPosition = currentPosition;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressSlider.setMax(finalTotal);
                            progressSlider.setProgress(finalCurrentPosition);
                            currentSongProgress.setText(MusicUtil.getReadableDurationString(finalCurrentPosition));
                        }
                    });
                }
            }
        }).start();
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
        killThreads = true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            animateActivityOpened(DEFAULT_DELAY);
        }
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

    private void animateActivityOpened(int startDelay) {
        ViewPropertyAnimator.animate(footer)
                .scaleX(1)
                .scaleY(1)
                .setInterpolator(new DecelerateInterpolator(4))
                .setDuration(DEFAULT_ANIMATION_TIME)
                .setStartDelay(startDelay)
                .start();
    }
}