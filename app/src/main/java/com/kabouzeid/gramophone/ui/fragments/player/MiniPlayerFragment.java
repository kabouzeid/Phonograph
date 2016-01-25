package com.kabouzeid.gramophone.ui.fragments.player;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.MusicProgressViewUpdateHelper;
import com.kabouzeid.gramophone.helper.PlayPauseButtonOnClickHandler;
import com.kabouzeid.gramophone.interfaces.MusicServiceEventListener;
import com.kabouzeid.gramophone.ui.activities.base.AbsMusicServiceActivity;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.views.PlayPauseDrawable;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class MiniPlayerFragment extends Fragment implements MusicServiceEventListener, MusicProgressViewUpdateHelper.Callback {
    @Bind(R.id.mini_player_title)
    TextView miniPlayerTitle;
    @Bind(R.id.mini_player_image)
    ImageView miniPlayerImage;
    @Bind(R.id.mini_player_play_pause_button)
    ImageView miniPlayerPlayPauseButton;
    @Bind(R.id.progress_bar)
    MaterialProgressBar progressBar;

    private PlayPauseDrawable miniPlayerPlayPauseDrawable;

    private MusicProgressViewUpdateHelper progressViewUpdateHelper;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressViewUpdateHelper = new MusicProgressViewUpdateHelper(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mini_player, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        view.setOnTouchListener(new FlingPlayBackController(activity));
        setUpMiniPlayer();

        activity.addMusicServiceEventListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.removeMusicServiceEventListener(this);
        ButterKnife.unbind(this);
    }

    private void setUpMiniPlayer() {
        setMiniPlayerColor(ColorUtil.resolveColor(activity, R.attr.cardBackgroundColor));
        miniPlayerImage.setImageDrawable(Util.getTintedDrawable(activity, R.drawable.ic_keyboard_arrow_up_white_24dp,
                ColorUtil.resolveColor(activity, android.R.attr.textColorSecondary)));
        setUpPlayPauseButton();

        progressBar.setProgressTintList(ColorStateList.valueOf(ThemeStore.accentColor(activity)));
    }

    public void setMiniPlayerColor(int color) {
        if (getView() == null) return;
        getView().setBackgroundColor(color);
        miniPlayerTitle.setTextColor(ColorUtil.getPrimaryTextColorForBackground(activity, color));
    }

    private void setUpPlayPauseButton() {
        updatePlayPauseDrawableState(false);
        miniPlayerPlayPauseButton.setImageDrawable(miniPlayerPlayPauseDrawable);
        miniPlayerPlayPauseButton.setColorFilter(ColorUtil.resolveColor(activity, android.R.attr.textColorSecondary), PorterDuff.Mode.SRC_IN);
        miniPlayerPlayPauseButton.setOnClickListener(new PlayPauseButtonOnClickHandler());
    }

    @Override
    public void onPlayingMetaChanged() {
        miniPlayerTitle.setText(MusicPlayerRemote.getCurrentSong().title);
    }

    @Override
    public void onQueueChanged() {

    }

    @Override
    public void onPlayStateChanged() {
        updatePlayPauseDrawableState(true);
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
    public void onUpdateProgressViews(int progress, int total) {
        progressBar.setMax(total);
        progressBar.setProgress(progress);
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

    private static class FlingPlayBackController implements View.OnTouchListener {

        GestureDetector flingPlayBackController;

        public FlingPlayBackController(Context context) {
            flingPlayBackController = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    if (Math.abs(velocityX) > Math.abs(velocityY)) {
                        if (velocityX < 0) {
                            MusicPlayerRemote.playNextSong();
                            return true;
                        } else if (velocityX > 0) {
                            MusicPlayerRemote.playPreviousSong();
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return flingPlayBackController.onTouchEvent(event);
        }
    }

    protected void updatePlayPauseDrawableState(boolean animate) {
        if (miniPlayerPlayPauseDrawable == null) {
            miniPlayerPlayPauseDrawable = new PlayPauseDrawable(activity);
        }
        if (MusicPlayerRemote.isPlaying()) {
            miniPlayerPlayPauseDrawable.setPause(animate);
        } else {
            miniPlayerPlayPauseDrawable.setPlay(animate);
        }
    }
}
