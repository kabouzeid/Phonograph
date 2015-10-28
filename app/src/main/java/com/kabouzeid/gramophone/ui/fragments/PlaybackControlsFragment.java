package com.kabouzeid.gramophone.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.PlayPauseButtonOnClickHandler;
import com.kabouzeid.gramophone.interfaces.MusicServiceEventListener;
import com.kabouzeid.gramophone.misc.FloatingActionButtonProperties;
import com.kabouzeid.gramophone.misc.SimpleAnimatorListener;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.ui.activities.base.AbsMusicServiceActivity;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.kabouzeid.gramophone.views.PlayPauseDrawable;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaybackControlsFragment extends Fragment implements MusicServiceEventListener, SharedPreferences.OnSharedPreferenceChangeListener {
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

    private int lastFooterColor;
    private int lastPlaybackControlsColor;
    private int lastTitleTextColor;
    private int lastCaptionTextColor;

    private PlayPauseDrawable playerFabPlayPauseDrawable;
    private AnimatorSet colorTransitionAnimator;

    private AbsMusicServiceActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            activity = (AbsMusicServiceActivity) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.getClass().getSimpleName() + " must be an instance of " + AbsMusicServiceActivity.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playback_controls, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        PreferenceUtil.getInstance(getContext()).registerOnSharedPreferenceChangedListener(this);
        activity.addMusicServiceEventListener(this);

        setUpMusicControllers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.removeMusicServiceEventListener(this);
        PreferenceUtil.getInstance(activity).unregisterOnSharedPreferenceChangedListener(this);
        ButterKnife.unbind(this);
    }

    @Override
    public void onPlayingMetaChanged() {
        updateMetaTexts();
    }

    @Override
    public void onPlayStateChanged() {
        updatePlayPauseDrawableState(true);
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
            case PreferenceUtil.PLAYBACK_CONTROLLER_CARD_NOW_PLAYING:
                updatePlaybackControllerCardVisibility();
                break;
            case PreferenceUtil.COLOR_PLAYBACK_CONTROLS_NOW_PLAYING:
                updateRepeatState();
                updateShuffleState();
                updatePlayPauseFabTint();
                break;
            case PreferenceUtil.LARGER_TITLE_BOX_NOW_PLAYING:
                updateTitleBoxSize();
                break;
        }
    }

    public void setColor(int color) {
        animateColorChange(color);
    }

    private void setUpPlayPauseFab() {
        updatePlayPauseDrawableState(false);
        playPauseFab.setImageDrawable(playerFabPlayPauseDrawable);
        updatePlayPauseFabTint();
        playPauseFab.setOnClickListener(new PlayPauseButtonOnClickHandler());
        playPauseFab.post(new Runnable() {
            @Override
            public void run() {
                playPauseFab.setPivotX(playPauseFab.getWidth() / 2);
                playPauseFab.setPivotY(playPauseFab.getHeight() / 2);
            }
        });
    }

    private void updatePlayPauseFabTint() {
        int fabColor = PreferenceUtil.getInstance(activity).colorPlaybackControlsNowPlaying() ? lastPlaybackControlsColor : activity.getThemeColorAccent();
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

    private void updateTitleBoxSize() {
        boolean largerTitleBox = PreferenceUtil.getInstance(activity).largerTitleBoxNowPlaying();
        int paddingTopBottom = largerTitleBox ? getResources().getDimensionPixelSize(R.dimen.title_box_padding_large) : getResources().getDimensionPixelSize(R.dimen.title_box_padding_small);
        footer.setPadding(footer.getPaddingLeft(), paddingTopBottom, footer.getPaddingRight(), paddingTopBottom);

        songTitle.setPadding(songTitle.getPaddingLeft(), songTitle.getPaddingTop(), songTitle.getPaddingRight(), largerTitleBox ? getResources().getDimensionPixelSize(R.dimen.title_box_text_spacing_large) : getResources().getDimensionPixelSize(R.dimen.title_box_text_spacing_small));
        songText.setPadding(songText.getPaddingLeft(), largerTitleBox ? getResources().getDimensionPixelSize(R.dimen.title_box_text_spacing_large) : getResources().getDimensionPixelSize(R.dimen.title_box_text_spacing_small), songText.getPaddingRight(), songText.getPaddingBottom());

        songTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, largerTitleBox ? getResources().getDimensionPixelSize(R.dimen.title_box_title_text_size_large) : getResources().getDimensionPixelSize(R.dimen.title_box_title_text_size_small));
        songText.setTextSize(TypedValue.COMPLEX_UNIT_PX, largerTitleBox ? getResources().getDimensionPixelSize(R.dimen.title_box_caption_text_size_large) : getResources().getDimensionPixelSize(R.dimen.title_box_caption_text_size_small));
    }

    private void updatePlaybackControllerCardVisibility() {
        boolean showPlaybackControllerCard = PreferenceUtil.getInstance(activity).playbackControllerCardNowPlaying();
        playbackControllerCard.setVisibility(showPlaybackControllerCard ? View.VISIBLE : View.GONE);
        mediaControllerContainerBackground.setVisibility(showPlaybackControllerCard ? View.GONE : View.VISIBLE);
    }

    private void setUpMusicControllers() {
        setUpPlayPauseFab();
        setUpPrevNext();
        setUpRepeatButton();
        setUpShuffleButton();
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
        if (PreferenceUtil.getInstance(activity).colorPlaybackControlsNowPlaying()) {
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

    private void updateMetaTexts() {
        final Song song = MusicPlayerRemote.getCurrentSong();
        songTitle.setText(song.title);
        songText.setText(song.artistName);
    }

    private void animateColorChange(final int newColor) {
        if (colorTransitionAnimator != null && colorTransitionAnimator.isStarted()) {
            colorTransitionAnimator.cancel();
        }
        colorTransitionAnimator = new AnimatorSet();
        AnimatorSet.Builder animatorSetBuilder = colorTransitionAnimator.play(ViewUtil.createBackgroundColorTransition(footer, lastFooterColor, newColor));

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
                updatePlayPauseFabTint();
            }
        });

        colorTransitionAnimator.start();

        lastFooterColor = newColor;
        lastTitleTextColor = titleTextColor;
        lastCaptionTextColor = captionTextColor;
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
}
