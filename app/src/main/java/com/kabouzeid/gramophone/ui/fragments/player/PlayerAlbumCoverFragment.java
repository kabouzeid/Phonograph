package com.kabouzeid.gramophone.ui.fragments.player;

import android.animation.Animator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.AlbumCoverPagerAdapter;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.MusicProgressViewUpdateHelper;
import com.kabouzeid.gramophone.misc.SimpleAnimatorListener;
import com.kabouzeid.gramophone.model.lyrics.AbsSynchronizedLyrics;
import com.kabouzeid.gramophone.model.lyrics.Lyrics;
import com.kabouzeid.gramophone.ui.fragments.AbsMusicServiceFragment;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.ViewUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlayerAlbumCoverFragment extends AbsMusicServiceFragment implements ViewPager.OnPageChangeListener, MusicProgressViewUpdateHelper.Callback {

    public static final int VISIBILITY_ANIM_DURATION = 300;

    private Unbinder unbinder;

    @BindView(R.id.player_album_cover_viewpager)
    ViewPager viewPager;
    @BindView(R.id.player_favorite_icon)
    ImageView favoriteIcon;

    @BindView(R.id.player_lyrics)
    FrameLayout lyricsLayout;
    @BindView(R.id.player_lyrics_line1)
    TextView lyricsLine1;
    @BindView(R.id.player_lyrics_line2)
    TextView lyricsLine2;

    private Callbacks callbacks;
    private int currentPosition;

    private Lyrics lyrics;
    private MusicProgressViewUpdateHelper progressViewUpdateHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_album_cover, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager.addOnPageChangeListener(this);
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    if (callbacks != null) {
                        callbacks.onToolbarToggled();
                        return true;
                    }
                    return super.onSingleTapConfirmed(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        progressViewUpdateHelper = new MusicProgressViewUpdateHelper(this, 500, 1000);
        progressViewUpdateHelper.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewPager.removeOnPageChangeListener(this);
        progressViewUpdateHelper.stop();
        unbinder.unbind();
    }

    @Override
    public void onServiceConnected() {
        updatePlayingQueue();
    }

    @Override
    public void onPlayingMetaChanged() {
        viewPager.setCurrentItem(MusicPlayerRemote.getPosition());
    }

    @Override
    public void onQueueChanged() {
        updatePlayingQueue();
    }

    private void updatePlayingQueue() {
        viewPager.setAdapter(new AlbumCoverPagerAdapter(getFragmentManager(), MusicPlayerRemote.getPlayingQueue()));
        viewPager.setCurrentItem(MusicPlayerRemote.getPosition());
        onPageSelected(MusicPlayerRemote.getPosition());
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        currentPosition = position;
        ((AlbumCoverPagerAdapter) viewPager.getAdapter()).receiveColor(colorReceiver, position);
        if (position != MusicPlayerRemote.getPosition()) {
            MusicPlayerRemote.playSongAt(position);
        }
    }

    private AlbumCoverPagerAdapter.AlbumCoverFragment.ColorReceiver colorReceiver = new AlbumCoverPagerAdapter.AlbumCoverFragment.ColorReceiver() {
        @Override
        public void onColorReady(int color, int requestCode) {
            if (currentPosition == requestCode) {
                notifyColorChange(color);
            }
        }
    };

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public void showHeartAnimation() {
        favoriteIcon.clearAnimation();

        favoriteIcon.setAlpha(0f);
        favoriteIcon.setScaleX(0f);
        favoriteIcon.setScaleY(0f);
        favoriteIcon.setVisibility(View.VISIBLE);
        favoriteIcon.setPivotX(favoriteIcon.getWidth() / 2);
        favoriteIcon.setPivotY(favoriteIcon.getHeight() / 2);

        favoriteIcon.animate()
                .setDuration(ViewUtil.PHONOGRAPH_ANIM_TIME / 2)
                .setInterpolator(new DecelerateInterpolator())
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        favoriteIcon.setVisibility(View.INVISIBLE);
                    }
                })
                .withEndAction(() -> favoriteIcon.animate()
                        .setDuration(ViewUtil.PHONOGRAPH_ANIM_TIME / 2)
                        .setInterpolator(new AccelerateInterpolator())
                        .scaleX(0f)
                        .scaleY(0f)
                        .alpha(0f)
                        .start())
                .start();
    }

    private boolean isLyricsLayoutVisible() {
        return lyrics != null && lyrics.isSynchronized() && lyrics.isValid() && PreferenceUtil.getInstance(getActivity()).synchronizedLyricsShow();
    }

    private boolean isLyricsLayoutBound() {
        return lyricsLayout != null && lyricsLine1 != null && lyricsLine2 != null;
    }

    private void hideLyricsLayout() {
        lyricsLayout.animate().alpha(0f).setDuration(PlayerAlbumCoverFragment.VISIBILITY_ANIM_DURATION).withEndAction(() -> {
            if (!isLyricsLayoutBound()) return;
            lyricsLayout.setVisibility(View.GONE);
            lyricsLine1.setText(null);
            lyricsLine2.setText(null);
        });
    }

    public void setLyrics(Lyrics l) {
        lyrics = l;

        if (!isLyricsLayoutBound()) return;

        if (!isLyricsLayoutVisible()) {
            hideLyricsLayout();
            return;
        }

        lyricsLine1.setText(null);
        lyricsLine2.setText(null);

        lyricsLayout.setVisibility(View.VISIBLE);
        lyricsLayout.animate().alpha(1f).setDuration(PlayerAlbumCoverFragment.VISIBILITY_ANIM_DURATION);
    }

    private void notifyColorChange(int color) {
        if (callbacks != null) callbacks.onColorChanged(color);
    }

    public void setCallbacks(Callbacks listener) {
        callbacks = listener;
    }

    @Override
    public void onUpdateProgressViews(int progress, int total) {
        if (!isLyricsLayoutBound()) return;

        if (!isLyricsLayoutVisible()) {
            hideLyricsLayout();
            return;
        }

        if (!(lyrics instanceof AbsSynchronizedLyrics)) return;
        AbsSynchronizedLyrics synchronizedLyrics = (AbsSynchronizedLyrics) lyrics;

        lyricsLayout.setVisibility(View.VISIBLE);
        lyricsLayout.setAlpha(1f);

        String oldLine = lyricsLine2.getText().toString();
        String line = synchronizedLyrics.getLine(progress);

        if (!oldLine.equals(line) || oldLine.isEmpty()) {
            lyricsLine1.setText(oldLine);
            lyricsLine2.setText(line);

            lyricsLine1.setVisibility(View.VISIBLE);
            lyricsLine2.setVisibility(View.VISIBLE);

            lyricsLine2.measure(View.MeasureSpec.makeMeasureSpec(lyricsLine2.getMeasuredWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.UNSPECIFIED);
            int h = lyricsLine2.getMeasuredHeight();

            lyricsLine1.setAlpha(1f);
            lyricsLine1.setTranslationY(0f);
            lyricsLine1.animate().alpha(0f).translationY(-h).setDuration(PlayerAlbumCoverFragment.VISIBILITY_ANIM_DURATION);

            lyricsLine2.setAlpha(0f);
            lyricsLine2.setTranslationY(h);
            lyricsLine2.animate().alpha(1f).translationY(0f).setDuration(PlayerAlbumCoverFragment.VISIBILITY_ANIM_DURATION);
        }
    }

    public interface Callbacks {
        void onColorChanged(int color);

        void onFavoriteToggled();

        void onToolbarToggled();
    }
}
