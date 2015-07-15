package com.kabouzeid.gramophone.ui.activities;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.ThemeSingleton;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.SleepTimerDialog;
import com.kabouzeid.gramophone.dialogs.SongDetailDialog;
import com.kabouzeid.gramophone.dialogs.SongShareDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.imageloader.BlurProcessor;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.misc.SmallTransitionListener;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.AbsTagEditorActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.SongTagEditorActivity;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.kabouzeid.gramophone.views.SquareIfPlaceImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;

public class MusicControllerActivity extends AbsSlidingMusicPanelActivity {

    public static final String TAG = MusicControllerActivity.class.getSimpleName();
    private static final int FAB_CIRCULAR_REVEAL_ANIMATION_TIME = 1000;
    private static final int PROGRESS_VIEW_UPDATE_INTERVAL = 100;

    private static final int CMD_UPDATE_PROGRESS_VIEWS = 1;

    @Bind(R.id.title)
    TextView songTitle;
    @Bind(R.id.text)
    TextView songArtist;
    @Bind(R.id.footer)
    LinearLayout footer;
    @Bind(R.id.playback_controller_card)
    CardView playbackControllerCard;
    @Bind(R.id.prev_button)
    ImageButton prevButton;
    @Bind(R.id.next_button)
    ImageButton nextButton;
    @Bind(R.id.repeat_button)
    ImageButton repeatButton;
    @Bind(R.id.shuffle_button)
    ImageButton shuffleButton;
    @Bind(R.id.media_controller_container)
    RelativeLayout mediaControllerContainer;
    @Bind(R.id.album_art_background)
    ImageView albumArtBackground;
    @Bind(R.id.image)
    SquareIfPlaceImageView albumArt;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.favorite_icon)
    ImageView favoriteIcon;

    TextView songCurrentProgress;
    TextView songTotalTime;
    SeekBar progressSlider;

    private int lastFooterColor = -1;
    private int lastTextColor = -2;

    private Handler progressViewsUpdateHandler;
    private HandlerThread handlerThread;

    private boolean opaqueStatusBar;
    private boolean opaqueToolBar;
    private boolean forceSquareAlbumArt;
    private boolean largerTitleBox;
    private boolean alternativeProgressSlider;
    private boolean showPlaybackControllerCard;

    private Song song;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setStatusBarTransparent();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_controller);
        ButterKnife.bind(this);

        initAppearanceVarsFromSharedPrefs();
        initProgressSliderDependentViews();

        moveSeekBarIntoPlace();
        adjustTitleBoxSize();
        setUpPlaybackControllerCard();
        setUpMusicControllers();
        setUpAlbumArtViews();
        setUpToolbar();
        animateFabCircularRevealOnEnterTransitionEnd();

        updateCurrentSong();
    }

    private void initProgressSliderDependentViews() {
        if (alternativeProgressSlider) {
            findViewById(R.id.default_progress_container).setVisibility(View.GONE);
            findViewById(R.id.default_progress_slider).setVisibility(View.GONE);
            findViewById(R.id.alternative_progress_container).setVisibility(View.VISIBLE);

            songCurrentProgress = (TextView) findViewById(R.id.alternative_song_current_progress);
            songTotalTime = (TextView) findViewById(R.id.alternative_song_total_time);
            progressSlider = (SeekBar) findViewById(R.id.alternative_progress_slider);
        } else {
            songCurrentProgress = (TextView) findViewById(R.id.default_song_current_progress);
            songTotalTime = (TextView) findViewById(R.id.default_song_total_time);
            progressSlider = (SeekBar) findViewById(R.id.default_progress_slider);
        }
    }

    private void setUpAlbumArtViews() {
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
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
    }

    private void animateFabCircularRevealOnEnterTransitionEnd() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getEnterTransition().addListener(new SmallTransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                    mediaControllerContainer.setVisibility(View.INVISIBLE);
                }

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onTransitionEnd(Transition transition) {
                    int cx = (getPlayPauseFab().getLeft() + getPlayPauseFab().getRight()) / 2;
                    int cy = (getPlayPauseFab().getTop() + getPlayPauseFab().getBottom()) / 2;
                    int finalRadius = Math.max(mediaControllerContainer.getWidth(), mediaControllerContainer.getHeight());

                    Animator animator = ViewAnimationUtils.createCircularReveal(mediaControllerContainer, cx, cy, getPlayPauseFab().getWidth() / 2, finalRadius);
                    animator.setInterpolator(new DecelerateInterpolator());
                    animator.setDuration(FAB_CIRCULAR_REVEAL_ANIMATION_TIME);
                    animator.start();

                    mediaControllerContainer.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void startHandler() {
        handlerThread = new HandlerThread("MusicProgressViewUpdateHandler",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        progressViewsUpdateHandler = new MusicProgressViewsUpdateHandler(this, handlerThread.getLooper());
    }

    private void stopHandler() {
        progressViewsUpdateHandler.removeCallbacksAndMessages(null);
        if (Build.VERSION.SDK_INT >= 18) {
            handlerThread.quitSafely();
        } else {
            handlerThread.quit();
        }
    }

    private void startUpdatingProgressViews() {
        startHandler();
        progressViewsUpdateHandler.sendEmptyMessage(CMD_UPDATE_PROGRESS_VIEWS);
    }

    private void stopUpdatingProgressViews() {
        progressViewsUpdateHandler.removeMessages(CMD_UPDATE_PROGRESS_VIEWS);
        stopHandler();
    }

    private void initAppearanceVarsFromSharedPrefs() {
        opaqueStatusBar = PreferenceUtil.getInstance(this).opaqueStatusbarNowPlaying();
        opaqueToolBar = opaqueStatusBar && PreferenceUtil.getInstance(this).opaqueToolbarNowPlaying();
        forceSquareAlbumArt = PreferenceUtil.getInstance(this).forceAlbumArtSquared();
        largerTitleBox = PreferenceUtil.getInstance(this).largerTitleBoxNowPlaying();
        alternativeProgressSlider = PreferenceUtil.getInstance(this).alternativeProgressSliderNowPlaying();
        showPlaybackControllerCard = PreferenceUtil.getInstance(this).playbackControllerCardNowPlaying();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getPlayPauseFab().setOnLongClickListener(null);
    }


    private void moveSeekBarIntoPlace() {
        if (!alternativeProgressSlider) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) progressSlider.getLayoutParams();
            progressSlider.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            final int seekBarMarginLeftRight = getResources().getDimensionPixelSize(R.dimen.seek_bar_margin_left_right);
            lp.setMargins(seekBarMarginLeftRight, 0, seekBarMarginLeftRight, -(progressSlider.getMeasuredHeight() / 2));
            progressSlider.setLayoutParams(lp);
        }
    }

    private void adjustTitleBoxSize() {
        int paddingTopBottom = largerTitleBox ? getResources().getDimensionPixelSize(R.dimen.title_box_padding_large) : getResources().getDimensionPixelSize(R.dimen.title_box_padding_small);
        footer.setPadding(footer.getPaddingLeft(), paddingTopBottom, footer.getPaddingRight(), paddingTopBottom);

        songTitle.setPadding(songTitle.getPaddingLeft(), songTitle.getPaddingTop(), songTitle.getPaddingRight(), largerTitleBox ? getResources().getDimensionPixelSize(R.dimen.title_box_text_spacing_large) : getResources().getDimensionPixelSize(R.dimen.title_box_text_spacing_small));
        songArtist.setPadding(songArtist.getPaddingLeft(), largerTitleBox ? getResources().getDimensionPixelSize(R.dimen.title_box_text_spacing_large) : getResources().getDimensionPixelSize(R.dimen.title_box_text_spacing_small), songArtist.getPaddingRight(), songArtist.getPaddingBottom());

        songTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, largerTitleBox ? getResources().getDimensionPixelSize(R.dimen.title_box_title_text_size_large) : getResources().getDimensionPixelSize(R.dimen.title_box_title_text_size_small));
        songArtist.setTextSize(TypedValue.COMPLEX_UNIT_PX, largerTitleBox ? getResources().getDimensionPixelSize(R.dimen.title_box_caption_text_size_large) : getResources().getDimensionPixelSize(R.dimen.title_box_caption_text_size_small));
    }

    private void setUpPlaybackControllerCard() {
        playbackControllerCard.setVisibility(showPlaybackControllerCard ? View.VISIBLE : View.GONE);
        mediaControllerContainer.setBackgroundColor(showPlaybackControllerCard ? Color.TRANSPARENT : ColorUtil.resolveColor(this, R.attr.music_controller_container_color));
    }

    private void setUpMusicControllers() {
        setUpPrevNext();
        setUpRepeatButton();
        setUpShuffleButton();
        setUpProgressSlider();
    }

    private void setTint(@NonNull SeekBar seekBar, int color) {
        ColorStateList s1 = ColorStateList.valueOf(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            seekBar.setThumbTintList(s1);
            if (!alternativeProgressSlider) seekBar.setProgressTintList(s1);
        } else {
            seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            if (!alternativeProgressSlider)
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
                R.drawable.ic_skip_next_white_36dp, DialogUtils.resolveColor(this, R.attr.themed_drawable_color)));
        prevButton.setImageDrawable(Util.getTintedDrawable(this,
                R.drawable.ic_skip_previous_white_36dp, DialogUtils.resolveColor(this, R.attr.themed_drawable_color)));
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
                shuffleButton.setImageDrawable(Util.getTintedDrawable(this, R.drawable.ic_shuffle_white_36dp,
                        getThemeColorAccent() == Color.WHITE ? Color.BLACK : getThemeColorAccent()));
                break;
            default:
                shuffleButton.setImageDrawable(Util.getTintedDrawable(this, R.drawable.ic_shuffle_white_36dp,
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
                repeatButton.setImageDrawable(Util.getTintedDrawable(this, R.drawable.ic_repeat_white_36dp,
                        DialogUtils.resolveColor(this, R.attr.themed_drawable_color)));
                break;
            case MusicService.REPEAT_MODE_ALL:
                repeatButton.setImageDrawable(Util.getTintedDrawable(this, R.drawable.ic_repeat_white_36dp,
                        getThemeColorAccent() == Color.WHITE ? Color.BLACK : getThemeColorAccent()));
                break;
            default:
                repeatButton.setImageDrawable(Util.getTintedDrawable(this, R.drawable.ic_repeat_one_white_36dp,
                        getThemeColorAccent() == Color.WHITE ? Color.BLACK : getThemeColorAccent()));
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startUpdatingProgressViews();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopUpdatingProgressViews();
    }

    private void updateCurrentSong() {
        getCurrentSong();
        setHeadersText();
        setUpAlbumArtAndApplyPalette();
        songTotalTime.setText(MusicUtil.getReadableDurationString(song.duration));
        songCurrentProgress.setText(MusicUtil.getReadableDurationString(0));
        invalidateOptionsMenu();
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
                MusicUtil.getSongImageLoaderString(song),
                albumArt,
                new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .showImageOnFail(R.drawable.default_album_art)
                        .build(),
                new SimpleImageLoadingListener() {
                    @DebugLog
                    @Override
                    public void onLoadingFailed(String imageUri, View view, @Nullable FailReason failReason) {
                        applyPalette(null);

                        ImageLoader.getInstance().displayImage(
                                "drawable://" + R.drawable.default_album_art,
                                albumArtBackground,
                                new DisplayImageOptions.Builder().postProcessor(new BlurProcessor(10)).build()
                        );
                    }

                    @DebugLog
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
        animateColorChange(vibrantColor);
        animateTextColorChange(ColorUtil.getTextColorForBackground(vibrantColor));
        notifyTaskColorChange(vibrantColor);
    }


    private void animateColorChange(final int newColor) {
        if (lastFooterColor != -1 && lastFooterColor != newColor) {
            ViewUtil.animateViewColor(footer, lastFooterColor, newColor);

            if (opaqueToolBar)
                ViewUtil.animateViewColor(toolbar, lastFooterColor, newColor);
            else toolbar.setBackgroundColor(Color.TRANSPARENT);
        } else {
            footer.setBackgroundColor(newColor);

            if (opaqueToolBar) toolbar.setBackgroundColor(newColor);
            else toolbar.setBackgroundColor(Color.TRANSPARENT);
        }

        setTint(progressSlider, !ThemeSingleton.get().darkTheme && getThemeColorAccent() == Color.WHITE ? Color.BLACK : getThemeColorAccent());
        if (opaqueStatusBar) setStatusBarColor(newColor);
        else setStatusBarColor(Color.TRANSPARENT);

        if (shouldColorNavigationBar())
            setNavigationBarColor(newColor);
        lastFooterColor = newColor;
    }

    private void animateTextColorChange(final int newColor) {
        if (lastTextColor != -2 && lastTextColor != newColor) {
            ViewUtil.animateTextColor(songTitle, lastTextColor, newColor);
            ViewUtil.animateTextColor(songArtist, lastTextColor, newColor);
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

    private void updateProgressViews() {
        final int totalMillis = MusicPlayerRemote.getSongDurationMillis();
        final int progressMillis = MusicPlayerRemote.getSongProgressMillis();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressSlider.setMax(totalMillis);
                progressSlider.setProgress(progressMillis);
                songCurrentProgress.setText(MusicUtil.getReadableDurationString(progressMillis));
                songTotalTime.setText(MusicUtil.getReadableDurationString(totalMillis));
            }
        });
    }

    private void animateSetFavorite() {
        favoriteIcon.clearAnimation();

        favoriteIcon.setAlpha(0f);
        favoriteIcon.setScaleX(0f);
        favoriteIcon.setScaleY(0f);
        favoriteIcon.setVisibility(View.VISIBLE);
        favoriteIcon.setPivotX(favoriteIcon.getWidth() / 2);
        favoriteIcon.setPivotY(favoriteIcon.getHeight() / 2);

        favoriteIcon.animate()
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator())
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        favoriteIcon.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        favoriteIcon.animate()
                                .setDuration(300)
                                .setInterpolator(new DecelerateInterpolator())
                                .alpha(0f)
                                .start();
                    }
                })
                .start();
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        updateCurrentSong();
    }

    @Override
    public void onRepeatModeChanged() {
        super.onRepeatModeChanged();
        updateRepeatState();
    }

    @Override
    public void onShuffleModeChanged() {
        super.onShuffleModeChanged();
        updateShuffleState();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_music_playing, menu);
        boolean isFavorite = MusicUtil.isFavorite(this, song);
        menu.findItem(R.id.action_toggle_favorite)
                .setIcon(isFavorite ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_outline_white_24dp)
                .setTitle(isFavorite ? getString(R.string.action_remove_from_favorites) : getString(R.string.action_add_to_favorites));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_sleep_timer:
                new SleepTimerDialog().show(getSupportFragmentManager(), "SET_SLEEP_TIMER");
                return true;
            case R.id.action_toggle_favorite:
                MusicUtil.toggleFavorite(this, song);
                if (MusicUtil.isFavorite(this, song)) {
                    animateSetFavorite();
                }
                invalidateOptionsMenu();
                return true;
            case R.id.action_share:
                SongShareDialog.create(song).show(getSupportFragmentManager(), "SHARE_SONG");
                return true;
            case R.id.action_equalizer:
                NavigationUtil.openEqualizer(this);
                return true;
            case R.id.action_shuffle_all:
                MusicPlayerRemote.openAndShuffleQueue(SongLoader.getAllSongs(this), true);
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
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, song.id);
                startActivity(intent);
                return true;
            case R.id.action_details:
                File songFile = new File(song.data);
                SongDetailDialog.create(songFile).show(getSupportFragmentManager(), "SONG_DETAIL");
                return true;
            case R.id.action_go_to_album:
                NavigationUtil.goToAlbum(this, song.albumId, getSharedViewsWithPlayPauseFab(null));
                return true;
            case R.id.action_go_to_artist:
                NavigationUtil.goToArtist(this, song.artistId, getSharedViewsWithPlayPauseFab(null));
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

    private static class MusicProgressViewsUpdateHandler extends Handler {
        private WeakReference<MusicControllerActivity> activityReference;

        public MusicProgressViewsUpdateHandler(final MusicControllerActivity activity, @NonNull final Looper looper) {
            super(looper);
            activityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == CMD_UPDATE_PROGRESS_VIEWS) {
                activityReference.get().updateProgressViews();
                sendEmptyMessageDelayed(CMD_UPDATE_PROGRESS_VIEWS, PROGRESS_VIEW_UPDATE_INTERVAL);
            }
        }
    }
}