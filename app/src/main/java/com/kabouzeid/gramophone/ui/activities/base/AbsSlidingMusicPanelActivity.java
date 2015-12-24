package com.kabouzeid.gramophone.ui.activities.base;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.ui.fragments.player.AbsPlayerFragment;
import com.kabouzeid.gramophone.ui.fragments.player.MiniPlayerFragment;
import com.kabouzeid.gramophone.ui.fragments.player.PlayerFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Karim Abou Zeid (kabouzeid)
 *         <p/>
 *         Do not use {@link #setContentView(int)} but wrap your layout with
 *         {@link #wrapSlidingMusicPanel(int)} first and then return it in {@link #createContentView()}
 */
public abstract class AbsSlidingMusicPanelActivity extends AbsMusicServiceActivity implements SlidingUpPanelLayout.PanelSlideListener, PlayerFragment.Callbacks {
    public static final String TAG = AbsSlidingMusicPanelActivity.class.getSimpleName();

    @Bind(R.id.sliding_layout)
    SlidingUpPanelLayout slidingUpPanelLayout;

    private int navigationBarColor;
    private int taskColor;

    private AbsPlayerFragment playerFragment;
    private MiniPlayerFragment miniPlayerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(createContentView());
        ButterKnife.bind(this);

        playerFragment = (AbsPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.player_fragment);
        miniPlayerFragment = (MiniPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.mini_player_fragment);

        if (miniPlayerFragment.getView() != null) {
            miniPlayerFragment.getView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleSlidingPanel();
                }
            });
        }

        slidingUpPanelLayout.setPanelSlideListener(this);
        playerFragment.onHide();

        slidingUpPanelLayout.post(new Runnable() {
            @Override
            public void run() {
                if (!isPanelCollapsed()) {
                    onPanelSlide(slidingUpPanelLayout, 1);
                    onPanelExpanded(slidingUpPanelLayout);
                } else if (isPanelCollapsed()) {
                    onPanelCollapsed(slidingUpPanelLayout);
                }
            }
        });
    }

    public void setAntiDragView(View antiDragView) {
        slidingUpPanelLayout.setAntiDragView(antiDragView);
    }

    protected abstract View createContentView();

    @Override
    public void onQueueChanged() {
        super.onQueueChanged();
        hideBottomBar(MusicPlayerRemote.getPlayingQueue().isEmpty());
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        hideBottomBar(MusicPlayerRemote.getPlayingQueue().isEmpty());
    }

    @Override
    public void onPanelSlide(View view, @FloatRange(from = 0, to = 1) float slideOffset) {
        setMiniPlayerAlphaProgress(slideOffset);
    }

    @Override
    public void onPanelCollapsed(View view) {
        super.notifyTaskColorChange(taskColor);
        if (shouldColorNavigationBar()) {
            super.setNavigationBarColor(navigationBarColor);
        }
        playerFragment.setMenuVisibility(false);
        playerFragment.setUserVisibleHint(false);
        playerFragment.onHide();
    }

    @Override
    public void onPanelExpanded(View view) {
        int playerFragmentColor = playerFragment.getPaletteColor();
        super.notifyTaskColorChange(playerFragmentColor);
        if (shouldColorNavigationBar()) {
            super.setNavigationBarColor(playerFragmentColor);
        }
        playerFragment.setMenuVisibility(true);
        playerFragment.setUserVisibleHint(true);
        playerFragment.onShow();
    }

    @Override
    public void onPanelAnchored(View view) {

    }

    @Override
    public void onPanelHidden(View view) {

    }

    private void setMiniPlayerAlphaProgress(@FloatRange(from = 0, to = 1) float progress) {
        if (miniPlayerFragment.getView() == null) return;
        float alpha = 1 - progress;
        miniPlayerFragment.getView().setAlpha(alpha);
        // necessary to make the views below clickable
        miniPlayerFragment.getView().setVisibility(alpha == 0 ? View.GONE : View.VISIBLE);
    }

    public void toggleSlidingPanel() {
        if (isPanelCollapsed()) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        } else {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    public boolean isPanelCollapsed() {
        return slidingUpPanelLayout == null || slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED;
    }

    public void hideBottomBar(final boolean hide) {
        slidingUpPanelLayout.post(new Runnable() {
            @Override
            public void run() {
                if (hide) {
                    slidingUpPanelLayout.setPanelHeight(0);
                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                } else {
                    slidingUpPanelLayout.setPanelHeight(getResources().getDimensionPixelSize(R.dimen.mini_player_height));
                }
            }
        });
    }

    protected View wrapSlidingMusicPanel(@LayoutRes int resId) {
        @SuppressLint("InflateParams")
        View slidingMusicPanelLayout = getLayoutInflater().inflate(R.layout.sliding_music_panel_layout, null);
        ViewGroup contentContainer = ButterKnife.findById(slidingMusicPanelLayout, R.id.content_container);
        getLayoutInflater().inflate(resId, contentContainer);
        return slidingMusicPanelLayout;
    }

    @Override
    public void onBackPressed() {
        if (playerFragment.onBackPressed()) return;
        if (!isPanelCollapsed()) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void setNavigationBarColor(@ColorInt int color) {
        this.navigationBarColor = color;
        if (isPanelCollapsed()) {
            super.setNavigationBarColor(color);
        }
    }

    @Override
    protected void notifyTaskColorChange(@ColorInt int color) {
        this.taskColor = color;
        if (isPanelCollapsed()) {
            super.notifyTaskColorChange(color);
        }
    }

    @Override
    public void onPaletteColorChanged() {
        if (!isPanelCollapsed()) {
            int playerFragmentColor = playerFragment.getPaletteColor();
            super.notifyTaskColorChange(playerFragmentColor);
            if (shouldColorNavigationBar()) {
                super.setNavigationBarColor(playerFragmentColor);
            }
        }
    }
}
