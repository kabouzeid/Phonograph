package com.kabouzeid.gramophone.ui.fragments;

import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.PlayingQueueDialog;
import com.kabouzeid.gramophone.dialogs.SleepTimerDialog;
import com.kabouzeid.gramophone.dialogs.SongDetailDialog;
import com.kabouzeid.gramophone.dialogs.SongShareDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.MusicProgressViewUpdateHelper;
import com.kabouzeid.gramophone.interfaces.MusicServiceEventListener;
import com.kabouzeid.gramophone.interfaces.PaletteColorHolder;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsMusicServiceActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.AbsTagEditorActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.SongTagEditorActivity;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PlayerFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener, MusicServiceEventListener, Toolbar.OnMenuItemClickListener, PaletteColorHolder, MusicProgressViewUpdateHelper.Callback, PlayerAlbumCoverFragment.OnColorChangedListener {
    public static final String TAG = PlayerFragment.class.getSimpleName();

    @Bind(R.id.player_toolbar)
    Toolbar toolbar;

    private AnimatorSet colorTransitionAnimator;
    private int lastColor;
    private int lastPlaybackControlsColor;

    private Song song;

    private MusicProgressViewUpdateHelper progressViewUpdateHelper;

    private AbsMusicServiceActivity activity;
    private Callbacks callbacks;

    private PlaybackControlsFragment playbackControlsFragment;
    private PlayingInfoFragment playingInfoFragment;
    private PlayerAlbumCoverFragment playerAlbumCoverFragment;

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

        playbackControlsFragment = (PlaybackControlsFragment) getChildFragmentManager().findFragmentById(R.id.playback_controls_fragment);
        playingInfoFragment = (PlayingInfoFragment) getChildFragmentManager().findFragmentById(R.id.playing_info_fragment);
        playerAlbumCoverFragment = (PlayerAlbumCoverFragment) getChildFragmentManager().findFragmentById(R.id.player_album_cover_fragment);
        playerAlbumCoverFragment.setOnColorChangedListener(this);

//        alignAlbumArt();
        setUpPlayerToolbar();
//        setUpPlayerStatusBarElevation();

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
    public void onPlayStateChanged() {

    }

    @Override
    public void onRepeatModeChanged() {

    }

    @Override
    public void onShuffleModeChanged() {

    }

    @Override
    public void onMediaStoreChanged() {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
//            case PreferenceUtil.OPAQUE_STATUSBAR_NOW_PLAYING:
//                opaqueStatusBar = PreferenceUtil.getInstance(activity).opaqueStatusbarNowPlaying();
//                // do not break here
//            case PreferenceUtil.OPAQUE_TOOLBAR_NOW_PLAYING:
//                opaqueToolBar = opaqueStatusBar && PreferenceUtil.getInstance(activity).opaqueToolbarNowPlaying();
//                setUpPlayerStatusBarElevation();
//                animateColorChange(lastColor);
//                alignAlbumArt();
//                break;
//            case PreferenceUtil.ALTERNATIVE_PROGRESS_SLIDER_NOW_PLAYING:
//                alternativeProgressSlider = PreferenceUtil.getInstance(activity).alternativeProgressSliderNowPlaying();
//                setUpProgressSlider();
//                break;
        }
    }

//    private void initProgressSliderDependentViews() {
//        if (getView() == null) return;
//        if (alternativeProgressSlider) {
//            getView().findViewById(R.id.player_default_progress_container).setVisibility(View.GONE);
//            getView().findViewById(R.id.player_default_progress_slider).setVisibility(View.GONE);
//            getView().findViewById(R.id.player_alternative_progress_container).setVisibility(View.VISIBLE);
//
//            songCurrentProgress = (TextView) getView().findViewById(R.id.player_alternative_song_current_progress);
//            songTotalTime = (TextView) getView().findViewById(R.id.player_alternative_song_total_time);
//            progressSlider = (SeekBar) getView().findViewById(R.id.player_alternative_progress_slider);
//        } else {
//            getView().findViewById(R.id.player_default_progress_container).setVisibility(View.VISIBLE);
//            getView().findViewById(R.id.player_default_progress_slider).setVisibility(View.VISIBLE);
//            getView().findViewById(R.id.player_alternative_progress_container).setVisibility(View.GONE);
//
//            songCurrentProgress = (TextView) getView().findViewById(R.id.player_default_song_current_progress);
//            songTotalTime = (TextView) getView().findViewById(R.id.player_default_song_total_time);
//            progressSlider = (SeekBar) getView().findViewById(R.id.player_default_progress_slider);
//        }
//    }
//
//    private void moveProgressSliderIntoPlace() {
//        if (!alternativeProgressSlider) {
//            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) progressSlider.getLayoutParams();
//            progressSlider.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//            final int seekBarMarginLeftRight = getResources().getDimensionPixelSize(R.dimen.seek_bar_margin_left_right);
//            lp.setMargins(seekBarMarginLeftRight, 0, seekBarMarginLeftRight, -(progressSlider.getMeasuredHeight() / 2));
//            progressSlider.setLayoutParams(lp);
//        }
//    }

//    private void updateProgressSliderTint() {
//        int thumbColor;
//        int progressColor;
//        if (alternativeProgressSlider) {
//            if (colorPlaybackControls) {
//                thumbColor = lastPlaybackControlsColor;
//            } else {
//                thumbColor = ThemeSingleton.get().positiveColor.getDefaultColor();
//            }
//            progressColor = Color.TRANSPARENT;
//        } else {
//            if (colorPlaybackControls) {
//                if (ColorUtil.useDarkTextColorOnBackground(lastPlaybackControlsColor)) {
//                    thumbColor = ColorUtil.shiftColor(lastPlaybackControlsColor, 1.2f);
//                } else {
//                    thumbColor = ColorUtil.shiftColor(lastPlaybackControlsColor, 0.8f);
//                }
//            } else {
//                thumbColor = activity.getThemeColorAccent();
//            }
//            progressColor = thumbColor;
//        }
//        setSeekBarTint(progressSlider, thumbColor, progressColor);
//    }
//
//    private static void setSeekBarTint(SeekBar seekBar, @ColorInt int thumbColor, @ColorInt int progressColor) {
//        seekBar.getThumb().mutate().setColorFilter(thumbColor, PorterDuff.Mode.SRC_IN);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            // this will only tint the left part of the progress bar
//            seekBar.setProgressTintList(ColorStateList.valueOf(progressColor));
//        } else {
//            seekBar.getProgressDrawable().mutate().setColorFilter(progressColor, PorterDuff.Mode.SRC_IN);
//        }
//    }

//    private void setUpProgressSlider() {
//        initProgressSliderDependentViews();
//        moveProgressSliderIntoPlace();
//        updateProgressSliderTint();
//        progressSlider.setOnSeekBarChangeListener(new SimpleOnSeekbarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (fromUser) {
//                    MusicPlayerRemote.seekTo(progress);
//                    onUpdateProgressViews(MusicPlayerRemote.getSongProgressMillis(), MusicPlayerRemote.getSongDurationMillis());
//                }
//            }
//        });
//    }

//    private void alignAlbumArt() {
//        if (opaqueStatusBar) {
//            if (opaqueToolBar) {
//                alignAlbumArtToToolbar();
//            } else {
//                alignAlbumArtToStatusBar();
//            }
//        } else {
//            alignAlbumArtToTop();
//        }
//    }
//
//    private void alignAlbumArtToTop() {
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) albumArtFrame.getLayoutParams();
//        if (Build.VERSION.SDK_INT > 16) {
//            params.removeRule(RelativeLayout.BELOW);
//        } else {
//            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT);
//            params.addRule(RelativeLayout.ABOVE, R.id.player_footer_frame);
//        }
//    }
//
//    private void alignAlbumArtToToolbar() {
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) albumArtFrame.getLayoutParams();
//        params.addRule(RelativeLayout.BELOW, R.id.player_toolbar);
//    }
//
//    private void alignAlbumArtToStatusBar() {
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) albumArtFrame.getLayoutParams();
//        params.addRule(RelativeLayout.BELOW, R.id.player_status_bar);
//    }

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

//    private void setUpPlayerStatusBarElevation() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (opaqueToolBar) {
//                statusbar.setElevation(getResources().getDimensionPixelSize(R.dimen.toolbar_elevation));
//            } else {
//                statusbar.setElevation(0);
//            }
//        }
//    }

    private void updatePlayerMenu() {
        boolean isFavorite = MusicUtil.isFavorite(activity, song);
        Drawable favoriteIcon = Util.getTintedDrawable(activity, isFavorite ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_outline_white_24dp, ViewUtil.getToolbarIconColor(activity, PreferenceUtil.getInstance(activity).opaqueToolbarNowPlaying() && ColorUtil.useDarkTextColorOnBackground(lastColor)));
        toolbar.getMenu().findItem(R.id.action_toggle_favorite)
                .setIcon(favoriteIcon)
                .setTitle(isFavorite ? getString(R.string.action_remove_from_favorites) : getString(R.string.action_add_to_favorites));
    }

    private void updateCurrentSong() {
        getCurrentSong();
        updatePlayerMenu();
    }

    private void getCurrentSong() {
        song = MusicPlayerRemote.getCurrentSong();
    }

    @Override
    @ColorInt
    public int getPaletteColor() {
        return lastColor;
    }

    @Override
    public void onUpdateProgressViews(int progress, int total) {
//        progressSlider.setMax(total);
//        progressSlider.setProgress(progress);
//        songTotalTime.setText(MusicUtil.getReadableDurationString(total));
//        songCurrentProgress.setText(MusicUtil.getReadableDurationString(progress));
    }

    private void animateColorChange(final int newColor) {
        getView().setBackgroundColor(newColor);
        lastColor = newColor;
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
                    playerAlbumCoverFragment.showHeart();
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

    public void showControls() {
        playbackControlsFragment.showControls();
    }

    public void resetShowControlsAnimation() {
        playbackControlsFragment.resetShowControlsAnimation();
    }

    @Override
    public void onColorChanged(int color) {
        animateColorChange(color);
        playbackControlsFragment.setColor(color);
        callbacks.onPaletteColorChanged();
    }

    public interface Callbacks {
        void onPaletteColorChanged();
    }
}
