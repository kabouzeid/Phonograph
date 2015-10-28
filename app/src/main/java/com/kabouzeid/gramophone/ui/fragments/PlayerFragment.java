package com.kabouzeid.gramophone.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.PlayingQueueDialog;
import com.kabouzeid.gramophone.dialogs.SleepTimerDialog;
import com.kabouzeid.gramophone.dialogs.SongDetailDialog;
import com.kabouzeid.gramophone.dialogs.SongShareDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.MusicProgressViewUpdateHelper;
import com.kabouzeid.gramophone.helper.PlayPauseButtonOnClickHandler;
import com.kabouzeid.gramophone.interfaces.MusicServiceEventListener;
import com.kabouzeid.gramophone.interfaces.PaletteColorHolder;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.misc.FloatingActionButtonProperties;
import com.kabouzeid.gramophone.misc.SimpleAnimatorListener;
import com.kabouzeid.gramophone.misc.SimpleOnSeekbarChangeListener;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.ui.activities.base.AbsMusicServiceActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.AbsTagEditorActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.SongTagEditorActivity;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.kabouzeid.gramophone.views.PlayPauseDrawable;
import com.kabouzeid.gramophone.views.SquareIfPlaceImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PlayerFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener, MusicServiceEventListener, Toolbar.OnMenuItemClickListener, PaletteColorHolder, MusicProgressViewUpdateHelper.Callback {
    public static final String TAG = PlayerFragment.class.getSimpleName();

    private static final int FAB_CIRCULAR_REVEAL_ANIMATION_TIME = 1000;

    @Bind(R.id.player_play_pause_fab)
    FloatingActionButton playPauseFab;

    @Bind(R.id.player_title)
    TextView songTitle;
    @Bind(R.id.player_text)
    TextView songText;
    @Bind(R.id.player_footer)
    LinearLayout footer;
    @Bind(R.id.player_playback_controller_card)
    CardView playbackControllerCard;
    @Bind(R.id.player_prev_button)
    ImageButton prevButton;
    @Bind(R.id.player_next_button)
    ImageButton nextButton;
    @Bind(R.id.player_repeat_button)
    ImageButton repeatButton;
    @Bind(R.id.player_shuffle_button)
    ImageButton shuffleButton;
    @Bind(R.id.player_media_controller_container)
    RelativeLayout mediaControllerContainer;
    @Bind(R.id.player_media_controller_container_background)
    View mediaControllerContainerBackground;
    @Bind(R.id.player_image)
    SquareIfPlaceImageView albumArt;
    @Bind(R.id.player_status_bar)
    View statusbar;
    @Bind(R.id.player_toolbar)
    Toolbar toolbar;
    @Bind(R.id.player_favorite_icon)
    ImageView favoriteIcon;
    @Bind(R.id.player_album_art_frame)
    FrameLayout albumArtFrame;

    TextView songCurrentProgress;
    TextView songTotalTime;
    SeekBar progressSlider;

    private int lastFooterColor;
    private int lastPlaybackControlsColor;
    private int lastTitleTextColor;
    private int lastCaptionTextColor;

    private MusicProgressViewUpdateHelper progressViewUpdateHelper;

    private boolean opaqueStatusBar;
    private boolean opaqueToolBar;
    private boolean forceSquareAlbumArt;
    private boolean largerTitleBox;
    private boolean alternativeProgressSlider;
    private boolean showPlaybackControllerCard;
    private boolean colorPlaybackControls;

    private Song song;

    private PlayPauseDrawable playerFabPlayPauseDrawable;

    private AnimatorSet colorTransitionAnimator;

    private AbsMusicServiceActivity activity;
    private Callbacks callbacks;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            activity = (AbsMusicServiceActivity) context;
            callbacks = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.getClass().getSimpleName() + " must be an instance of " + AbsMusicServiceActivity.class.getSimpleName() + " and implement " + Callbacks.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
        callbacks = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAppearanceVarsFromSharedPrefs();
        progressViewUpdateHelper = new MusicProgressViewUpdateHelper(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        resetShowControlsAnimation();
        setUpPlayPauseFab();
        adjustTitleBoxSize();
        setUpPlaybackControllerCard();
        setUpMusicControllers();
        setUpAlbumArtViews();
        setUpPlayerToolbar();
        setUpPlayerStatusBarElevation();

        PreferenceUtil.getInstance(getContext()).registerOnSharedPreferenceChangedListener(this);
        activity.addMusicServiceEventListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.removeMusicServiceEventListener(this);
        PreferenceUtil.getInstance(activity).unregisterOnSharedPreferenceChangedListener(this);
        ButterKnife.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        progressViewUpdateHelper.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        progressViewUpdateHelper.stop();
    }

    @Override
    public void onPlayingMetaChanged() {
        updateCurrentSong();
    }

    @Override
    public void onRepeatModeChanged() {
        updateRepeatState();
    }

    @Override
    public void onShuffleModeChanged() {
        updateShuffleState();
    }

    @Override
    public void onMediaStoreChanged() {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PreferenceUtil.OPAQUE_STATUSBAR_NOW_PLAYING:
                opaqueStatusBar = PreferenceUtil.getInstance(activity).opaqueStatusbarNowPlaying();
                // do not break here
            case PreferenceUtil.OPAQUE_TOOLBAR_NOW_PLAYING:
                opaqueToolBar = opaqueStatusBar && PreferenceUtil.getInstance(activity).opaqueToolbarNowPlaying();
                setUpPlayerStatusBarElevation();
                animateColorChange(lastFooterColor);
                if (opaqueStatusBar) {
                    if (opaqueToolBar) {
                        alignAlbumArtToToolbar();
                    } else {
                        alignAlbumArtToStatusBar();
                    }
                } else {
                    alignAlbumArtToTop();
                }
                break;
            case PreferenceUtil.FORCE_SQUARE_ALBUM_ART:
                forceSquareAlbumArt = PreferenceUtil.getInstance(activity).forceAlbumArtSquared();
                albumArt.forceSquare(forceSquareAlbumArt);
                break;
            case PreferenceUtil.LARGER_TITLE_BOX_NOW_PLAYING:
                largerTitleBox = PreferenceUtil.getInstance(activity).largerTitleBoxNowPlaying();
                adjustTitleBoxSize();
                break;
            case PreferenceUtil.ALTERNATIVE_PROGRESS_SLIDER_NOW_PLAYING:
                alternativeProgressSlider = PreferenceUtil.getInstance(activity).alternativeProgressSliderNowPlaying();
                setUpProgressSlider();
                break;
            case PreferenceUtil.PLAYBACK_CONTROLLER_CARD_NOW_PLAYING:
                showPlaybackControllerCard = PreferenceUtil.getInstance(activity).playbackControllerCardNowPlaying();
                setUpPlaybackControllerCard();
                break;
            case PreferenceUtil.COLOR_PLAYBACK_CONTROLS_NOW_PLAYING:
                colorPlaybackControls = PreferenceUtil.getInstance(activity).colorPlaybackControlsNowPlaying();
                updateRepeatState();
                updateShuffleState();
                setUpProgressSliderTint();
                setUpPlayerPlayPauseFabTint();
                break;
        }
    }

    private void setUpPlayPauseFab() {
        updatePlayPauseDrawableState(false);
        playPauseFab.setImageDrawable(playerFabPlayPauseDrawable);
        setUpPlayerPlayPauseFabTint();
        playPauseFab.setOnClickListener(new PlayPauseButtonOnClickHandler());
        playPauseFab.post(new Runnable() {
            @Override
            public void run() {
                playPauseFab.setPivotX(playPauseFab.getWidth() / 2);
                playPauseFab.setPivotY(playPauseFab.getHeight() / 2);
            }
        });
    }

    private void setUpPlayerPlayPauseFabTint() {
        int fabColor = colorPlaybackControls ? lastPlaybackControlsColor : activity.getThemeColorAccent();
        FloatingActionButtonProperties.COLOR.set(playPauseFab, fabColor);
    }

    protected void updatePlayPauseDrawableState(boolean animate) {
        if (playerFabPlayPauseDrawable == null) {
            playerFabPlayPauseDrawable = new PlayPauseDrawable(activity);
        }
        if (MusicPlayerRemote.isPlaying()) {
            playerFabPlayPauseDrawable.setPause(animate);
        } else {
            playerFabPlayPauseDrawable.setPlay(animate);
        }
    }

    @Override
    public void onPlayStateChanged() {
        updatePlayPauseDrawableState(true);
    }

    private void initAppearanceVarsFromSharedPrefs() {
        opaqueStatusBar = PreferenceUtil.getInstance(activity).opaqueStatusbarNowPlaying();
        opaqueToolBar = opaqueStatusBar && PreferenceUtil.getInstance(activity).opaqueToolbarNowPlaying();
        forceSquareAlbumArt = PreferenceUtil.getInstance(activity).forceAlbumArtSquared();
        largerTitleBox = PreferenceUtil.getInstance(activity).largerTitleBoxNowPlaying();
        alternativeProgressSlider = PreferenceUtil.getInstance(activity).alternativeProgressSliderNowPlaying();
        showPlaybackControllerCard = PreferenceUtil.getInstance(activity).playbackControllerCardNowPlaying();
        colorPlaybackControls = PreferenceUtil.getInstance(activity).colorPlaybackControlsNowPlaying();
    }

    private void initProgressSliderDependentViews() {
        if (getView() == null) return;
        if (alternativeProgressSlider) {
            getView().findViewById(R.id.player_default_progress_container).setVisibility(View.GONE);
            getView().findViewById(R.id.player_default_progress_slider).setVisibility(View.GONE);
            getView().findViewById(R.id.player_alternative_progress_container).setVisibility(View.VISIBLE);

            songCurrentProgress = (TextView) getView().findViewById(R.id.player_alternative_song_current_progress);
            songTotalTime = (TextView) getView().findViewById(R.id.player_alternative_song_total_time);
            progressSlider = (SeekBar) getView().findViewById(R.id.player_alternative_progress_slider);
        } else {
            getView().findViewById(R.id.player_default_progress_container).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.player_default_progress_slider).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.player_alternative_progress_container).setVisibility(View.GONE);

            songCurrentProgress = (TextView) getView().findViewById(R.id.player_default_song_current_progress);
            songTotalTime = (TextView) getView().findViewById(R.id.player_default_song_total_time);
            progressSlider = (SeekBar) getView().findViewById(R.id.player_default_progress_slider);
        }
    }

    private void moveProgressSliderIntoPlace() {
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
        songText.setPadding(songText.getPaddingLeft(), largerTitleBox ? getResources().getDimensionPixelSize(R.dimen.title_box_text_spacing_large) : getResources().getDimensionPixelSize(R.dimen.title_box_text_spacing_small), songText.getPaddingRight(), songText.getPaddingBottom());

        songTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, largerTitleBox ? getResources().getDimensionPixelSize(R.dimen.title_box_title_text_size_large) : getResources().getDimensionPixelSize(R.dimen.title_box_title_text_size_small));
        songText.setTextSize(TypedValue.COMPLEX_UNIT_PX, largerTitleBox ? getResources().getDimensionPixelSize(R.dimen.title_box_caption_text_size_large) : getResources().getDimensionPixelSize(R.dimen.title_box_caption_text_size_small));
    }

    private void setUpPlaybackControllerCard() {
        playbackControllerCard.setVisibility(showPlaybackControllerCard ? View.VISIBLE : View.GONE);
        mediaControllerContainerBackground.setVisibility(showPlaybackControllerCard ? View.GONE : View.VISIBLE);
    }

    private void setUpMusicControllers() {
        setUpPrevNext();
        setUpRepeatButton();
        setUpShuffleButton();
        setUpProgressSlider();
    }

    private void setUpProgressSliderTint() {
        int thumbColor;
        int progressColor;
        if (alternativeProgressSlider) {
            if (colorPlaybackControls) {
                thumbColor = lastPlaybackControlsColor;
            } else {
                thumbColor = ThemeSingleton.get().positiveColor.getDefaultColor();
            }
            progressColor = Color.TRANSPARENT;
        } else {
            if (colorPlaybackControls) {
                if (ColorUtil.useDarkTextColorOnBackground(lastPlaybackControlsColor)) {
                    thumbColor = ColorUtil.shiftColor(lastPlaybackControlsColor, 1.2f);
                } else {
                    thumbColor = ColorUtil.shiftColor(lastPlaybackControlsColor, 0.8f);
                }
            } else {
                thumbColor = activity.getThemeColorAccent();
            }
            progressColor = thumbColor;
        }
        setSeekBarTint(progressSlider, thumbColor, progressColor);
    }

    private static void setSeekBarTint(SeekBar seekBar, @ColorInt int thumbColor, @ColorInt int progressColor) {
        seekBar.getThumb().mutate().setColorFilter(thumbColor, PorterDuff.Mode.SRC_IN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // this will only tint the left part of the progress bar
            seekBar.setProgressTintList(ColorStateList.valueOf(progressColor));
        } else {
            seekBar.getProgressDrawable().mutate().setColorFilter(progressColor, PorterDuff.Mode.SRC_IN);
        }
    }

    private void setUpProgressSlider() {
        initProgressSliderDependentViews();
        moveProgressSliderIntoPlace();
        setUpProgressSliderTint();
        progressSlider.setOnSeekBarChangeListener(new SimpleOnSeekbarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    MusicPlayerRemote.seekTo(progress);
                    onUpdateProgressViews(MusicPlayerRemote.getSongProgressMillis(), MusicPlayerRemote.getSongDurationMillis());
                }
            }
        });
    }

    private void setUpPrevNext() {
        int themedDrawableColor = ColorUtil.resolveColor(activity, android.R.attr.textColorSecondary);
        nextButton.setImageDrawable(Util.getTintedDrawable(activity,
                R.drawable.ic_skip_next_white_36dp, themedDrawableColor));
        prevButton.setImageDrawable(Util.getTintedDrawable(activity,
                R.drawable.ic_skip_previous_white_36dp, themedDrawableColor));
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
                shuffleButton.setImageDrawable(Util.getTintedDrawable(activity, R.drawable.ic_shuffle_white_36dp,
                        getActivatedIconColor()));
                break;
            default:
                shuffleButton.setImageDrawable(Util.getTintedDrawable(activity, R.drawable.ic_shuffle_white_36dp,
                        getDeactivatedIconColor()));
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
            case MusicService.REPEAT_MODE_ALL:
                repeatButton.setImageDrawable(Util.getTintedDrawable(activity, R.drawable.ic_repeat_white_36dp,
                        getActivatedIconColor()));
                break;
            case MusicService.REPEAT_MODE_THIS:
                repeatButton.setImageDrawable(Util.getTintedDrawable(activity, R.drawable.ic_repeat_one_white_36dp,
                        getActivatedIconColor()));
                break;
            default:
                repeatButton.setImageDrawable(Util.getTintedDrawable(activity, R.drawable.ic_repeat_white_36dp,
                        getDeactivatedIconColor()));
                break;
        }
    }

    private int getActivatedIconColor() {
        if (colorPlaybackControls) {
            return ensureActivatedColorVisibleIfNecessary(lastPlaybackControlsColor);
        } else {
            return ThemeSingleton.get().positiveColor.getDefaultColor();
        }
    }

    private int getDeactivatedIconColor() {
        return ColorUtil.resolveColor(activity, android.R.attr.textColorSecondary);
    }

    /**
     * @return If the activated color wont have enough difference to the deactivated color Color.WHITE / Color.BLACK (depending on the theme),
     * else the unmodified accentColor.
     */
    private int ensureActivatedColorVisibleIfNecessary(int activatedColor) {
        // Not optimal, but much easier then computing the opaque deactivated color on the background color every time.
        int preBlendedDeactivatedIconColor = ThemeSingleton.get().darkTheme ? Color.argb(255, 188, 188, 188) : Color.argb(255, 115, 115, 115);
        if (ColorUtil.getColorDifference(activatedColor, preBlendedDeactivatedIconColor) <= 30d) {
            return ThemeSingleton.get().darkTheme ? Color.WHITE : Color.BLACK;
        }
        return activatedColor;
    }

    private void setUpAlbumArtViews() {
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

    private void alignAlbumArtToTop() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) albumArtFrame.getLayoutParams();
        if (Build.VERSION.SDK_INT > 16) {
            params.removeRule(RelativeLayout.BELOW);
        } else {
            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.ABOVE, R.id.player_footer_frame);
        }
    }

    private void alignAlbumArtToToolbar() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) albumArtFrame.getLayoutParams();
        params.addRule(RelativeLayout.BELOW, R.id.player_toolbar);
    }

    private void alignAlbumArtToStatusBar() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) albumArtFrame.getLayoutParams();
        params.addRule(RelativeLayout.BELOW, R.id.player_status_bar);
    }

    private void setUpPlayerToolbar() {
        toolbar.inflateMenu(R.menu.menu_player);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onBackPressed();
            }
        });
        toolbar.setOnMenuItemClickListener(this);
    }

    private void setUpPlayerStatusBarElevation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (opaqueToolBar) {
                statusbar.setElevation(getResources().getDimensionPixelSize(R.dimen.toolbar_elevation));
            } else {
                statusbar.setElevation(0);
            }
        }
    }

    private void updatePlayerMenu() {
        boolean isFavorite = MusicUtil.isFavorite(activity, song);
        Drawable favoriteIcon = Util.getTintedDrawable(activity, isFavorite ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_outline_white_24dp, ViewUtil.getToolbarIconColor(activity, opaqueToolBar && ColorUtil.useDarkTextColorOnBackground(lastFooterColor)));
        toolbar.getMenu().findItem(R.id.action_toggle_favorite)
                .setIcon(favoriteIcon)
                .setTitle(isFavorite ? getString(R.string.action_remove_from_favorites) : getString(R.string.action_add_to_favorites));
    }

    private void updateCurrentSong() {
        getCurrentSong();
        updateMetaTexts();
        setUpAlbumArtAndApplyPalette();
        updatePlayerMenu();
    }

    private void getCurrentSong() {
        song = MusicPlayerRemote.getCurrentSong();
    }

    private void updateMetaTexts() {
        songTitle.setText(song.title);
        songText.setText(song.artistName);
    }

    @Override
    @ColorInt
    public int getPaletteColor() {
        return lastFooterColor;
    }

    @Override
    public void onUpdateProgressViews(int progress, int total) {
        progressSlider.setMax(total);
        progressSlider.setProgress(progress);
        songTotalTime.setText(MusicUtil.getReadableDurationString(total));
        songCurrentProgress.setText(MusicUtil.getReadableDurationString(progress));
    }

    private static class ColorHolder {
        @ColorInt
        public int color;
    }

    private void setUpAlbumArtAndApplyPalette() {
        final ColorHolder colorHolder = new ColorHolder();
        ImageLoader.getInstance().displayImage(
                MusicUtil.getSongImageLoaderString(song),
                albumArt,
                new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .showImageOnFail(R.drawable.default_album_art)
                        .postProcessor(new BitmapProcessor() {
                            @Override
                            public Bitmap process(Bitmap bitmap) {
                                colorHolder.color = ColorUtil.generateColor(activity, bitmap);
                                return bitmap;
                            }
                        })
                        .displayer(new FadeInBitmapDisplayer(ViewUtil.DEFAULT_COLOR_ANIMATION_DURATION) {
                            @Override
                            public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
                                super.display(bitmap, imageAware, loadedFrom);
                                setColors(colorHolder.color);
                            }
                        })
                        .build(),
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingFailed(String imageUri, View view, @Nullable FailReason failReason) {
                        FadeInBitmapDisplayer.animate(view, ViewUtil.DEFAULT_COLOR_ANIMATION_DURATION);
                        setColors(ColorUtil.resolveColor(activity, R.attr.default_bar_color));
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, @Nullable Bitmap loadedImage) {
                        if (loadedImage == null) {
                            onLoadingFailed(imageUri, view, null);
                        }
                    }
                }
        );
    }

    private void setColors(int color) {
        animateColorChange(color);
        callbacks.onPaletteColorChanged();
    }

    private void animateColorChange(final int newColor) {
        if (colorTransitionAnimator != null && colorTransitionAnimator.isStarted()) {
            colorTransitionAnimator.cancel();
        }
        colorTransitionAnimator = new AnimatorSet();
        AnimatorSet.Builder animatorSetBuilder = colorTransitionAnimator.play(ViewUtil.createBackgroundColorTransition(footer, lastFooterColor, newColor));

        if (opaqueToolBar) {
            animatorSetBuilder.with(ViewUtil.createBackgroundColorTransition(toolbar, lastFooterColor, newColor));
            ViewUtil.setToolbarContentColorForBackground(activity, toolbar, newColor);
        } else {
            toolbar.setBackgroundColor(Color.TRANSPARENT);
            ViewUtil.setToolbarContentDark(activity, toolbar, false);
        }

        if (opaqueStatusBar) {
            int newStatusbarColor = newColor;
            int oldStatusbarColor = lastFooterColor;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                newStatusbarColor = ColorUtil.shiftColorDown(newStatusbarColor);
                oldStatusbarColor = ColorUtil.shiftColorDown(oldStatusbarColor);
            }
            animatorSetBuilder.with(ViewUtil.createBackgroundColorTransition(statusbar, oldStatusbarColor, newStatusbarColor));
        } else {
            statusbar.setBackgroundColor(Color.TRANSPARENT);
        }

        int titleTextColor = ColorUtil.getPrimaryTextColorForBackground(activity, newColor);
        int captionTextColor = ColorUtil.getSecondaryTextColorForBackground(activity, newColor);

        animatorSetBuilder.with(ViewUtil.createTextColorTransition(songTitle, lastTitleTextColor, titleTextColor));
        animatorSetBuilder.with(ViewUtil.createTextColorTransition(songText, lastCaptionTextColor, captionTextColor));

        colorTransitionAnimator.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (newColor == ColorUtil.resolveColor(activity, R.attr.default_bar_color) && ThemeSingleton.get().darkTheme) {
                    lastPlaybackControlsColor = Color.WHITE;
                } else {
                    lastPlaybackControlsColor = newColor;
                }
                updateRepeatState();
                updateShuffleState();
                setUpProgressSliderTint();
                setUpPlayerPlayPauseFabTint();
            }
        });

        colorTransitionAnimator.start();

        lastFooterColor = newColor;
        lastTitleTextColor = titleTextColor;
        lastCaptionTextColor = captionTextColor;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sleep_timer:
                new SleepTimerDialog().show(getFragmentManager(), "SET_SLEEP_TIMER");
                return true;
            case R.id.action_toggle_favorite:
                MusicUtil.toggleFavorite(activity, song);
                if (MusicUtil.isFavorite(activity, song)) {
                    animateSetFavorite();
                }
                updatePlayerMenu();
                return true;
            case R.id.action_share:
                SongShareDialog.create(song).show(getFragmentManager(), "SHARE_SONG");
                return true;
            case R.id.action_equalizer:
                NavigationUtil.openEqualizer(activity);
                return true;
            case R.id.action_shuffle_all:
                MusicPlayerRemote.openAndShuffleQueue(SongLoader.getAllSongs(activity), true);
                return true;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(song).show(getFragmentManager(), "ADD_PLAYLIST");
                return true;
            case R.id.action_playing_queue:
                PlayingQueueDialog.create().show(getFragmentManager(), "PLAY_QUEUE");
                return true;
            case R.id.action_tag_editor:
                Intent intent = new Intent(activity, SongTagEditorActivity.class);
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, song.id);
                startActivity(intent);
                return true;
            case R.id.action_details:
                SongDetailDialog.create(song).show(getFragmentManager(), "SONG_DETAIL");
                return true;
            case R.id.action_go_to_album:
                NavigationUtil.goToAlbum(activity, song.albumId);
                return true;
            case R.id.action_go_to_artist:
                NavigationUtil.goToArtist(activity, song.artistId);
                return true;
        }
        return false;
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
                .setListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        favoriteIcon.setVisibility(View.INVISIBLE);
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

    public void showControls() {
        playPauseFab.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotation(360f)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mediaControllerContainer.getVisibility() == View.INVISIBLE) {
                int cx = (playPauseFab.getLeft() + playPauseFab.getRight()) / 2;
                int cy = (playPauseFab.getTop() + playPauseFab.getBottom()) / 2;
                int finalRadius = Math.max(mediaControllerContainer.getWidth(), mediaControllerContainer.getHeight());

                final Animator animator = ViewAnimationUtils.createCircularReveal(mediaControllerContainer, cx, cy, 0, finalRadius);
                animator.setInterpolator(new DecelerateInterpolator());
                animator.setDuration(FAB_CIRCULAR_REVEAL_ANIMATION_TIME);
                animator.start();
                mediaControllerContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    public void resetShowControlsAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaControllerContainer.setVisibility(View.INVISIBLE);
        }
        playPauseFab.setScaleX(0f);
        playPauseFab.setScaleY(0f);
        playPauseFab.setRotation(0f);
    }

    public interface Callbacks {
        void onPaletteColorChanged();
    }
}
