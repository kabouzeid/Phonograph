package com.kabouzeid.gramophone.ui.fragments.player;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.base.MediaEntryViewHolder;
import com.kabouzeid.gramophone.adapter.song.PlayingQueueAdapter;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.misc.DragSortRecycler;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.kabouzeid.gramophone.views.SquareLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.solovyev.android.views.llm.LinearLayoutManager;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PlayerFragment extends AbsPlayerFragment implements PlayerAlbumCoverFragment.OnColorChangedListener, SlidingUpPanelLayout.PanelSlideListener {
    public static final String TAG = PlayerFragment.class.getSimpleName();

    @Bind(R.id.player_toolbar)
    Toolbar toolbar;
    @Bind(R.id.player_sliding_layout)
    SlidingUpPanelLayout slidingUpPanelLayout;
    @Bind(R.id.player_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.playing_queue_card)
    CardView playingQueueCard;
    @Bind(R.id.album_cover_container)
    SquareLayout albumCoverContainer;
    @Bind(R.id.player_content)
    RelativeLayout playerContent;
    @Bind(R.id.color_background)
    View colorBackground;

    @Bind(R.id.player_queue_subheader)
    TextView playerQueueSubheader;

    MediaEntryViewHolder currentSongViewHolder;

    private int lastColor;

    private PlaybackControlsFragment playbackControlsFragment;
    private PlayerAlbumCoverFragment playerAlbumCoverFragment;

    private LinearLayoutManager layoutManager;

    private PlayingQueueAdapter playingQueueAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpPlayerToolbar();
        setUpSubFragments();

        playingQueueAdapter = new PlayingQueueAdapter(
                ((AppCompatActivity) getActivity()),
                MusicPlayerRemote.getPlayingQueue(),
                R.layout.item_list,
                false,
                null);
        recyclerView.setAdapter(playingQueueAdapter);

        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setChildSize((int) (getResources().getDisplayMetrics().density * 72));
        recyclerView.setLayoutManager(layoutManager);

        setUpDragSort();

        //slidingUpPanelLayout.setParallaxOffset(Util.resolveDimensionPixelSize(activity, R.attr.actionBarSize) + getResources().getDimensionPixelSize(R.dimen.status_bar_padding));
        slidingUpPanelLayout.setPanelSlideListener(this);
        slidingUpPanelLayout.setAntiDragView(view.findViewById(R.id.draggable_area));

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setUpPanelAndAlbumCoverHeight();
            }
        });

        setUpCurrentSongView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onPlayingMetaChanged() {
        updateIsFavorite();
        updateCurrentSong();
        updateQueuePosition();
    }

    @Override
    public void onQueueChanged() {
        updateQueue();
    }

    private void updateQueue() {
        playingQueueAdapter.swapDataSet(MusicPlayerRemote.getPlayingQueue(), MusicPlayerRemote.getPosition());
        if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            resetToCurrentPosition();
        }
    }

    private void updateQueuePosition() {
        playingQueueAdapter.setCurrent(MusicPlayerRemote.getPosition());
        if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            resetToCurrentPosition();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void updateCurrentSong() {
        Song song = MusicPlayerRemote.getCurrentSong();
        currentSongViewHolder.title.setText(song.title);
        currentSongViewHolder.text.setText(song.artistName);
    }

    private void setUpPanelAndAlbumCoverHeight() {
        int topMargin = getResources().getDimensionPixelSize(R.dimen.status_bar_padding);

        final int availablePanelHeight = slidingUpPanelLayout.getHeight() - playerContent.getHeight() + topMargin;
        final int minPanelHeight = (int) getResources().getDisplayMetrics().density * (72 + 24) + topMargin;
        if (availablePanelHeight < minPanelHeight) {
            albumCoverContainer.getLayoutParams().height = albumCoverContainer.getHeight() - (minPanelHeight - availablePanelHeight);
            albumCoverContainer.forceSquare(false);
        }
        slidingUpPanelLayout.setPanelHeight(Math.max(minPanelHeight, availablePanelHeight));
        ((AbsSlidingMusicPanelActivity) getActivity()).setAntiDragView(slidingUpPanelLayout.findViewById(R.id.player_panel));
    }

    private void setUpSubFragments() {
        playbackControlsFragment = (PlaybackControlsFragment) getChildFragmentManager().findFragmentById(R.id.playback_controls_fragment);
        playerAlbumCoverFragment = (PlayerAlbumCoverFragment) getChildFragmentManager().findFragmentById(R.id.player_album_cover_fragment);

        playerAlbumCoverFragment.setOnColorChangedListener(this);
    }

    private void setUpPlayerToolbar() {
        toolbar.inflateMenu(R.menu.menu_player);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        toolbar.setOnMenuItemClickListener(this);
    }

    @SuppressWarnings("ConstantConditions")
    private void setUpCurrentSongView() {
        currentSongViewHolder = new MediaEntryViewHolder(getView().findViewById(R.id.current_song));

        currentSongViewHolder.separator.setVisibility(View.VISIBLE);
        currentSongViewHolder.shortSeparator.setVisibility(View.GONE);
        currentSongViewHolder.image.setScaleType(ImageView.ScaleType.CENTER);
        currentSongViewHolder.image.setImageDrawable(Util.getTintedDrawable(getActivity(), R.drawable.ic_volume_up_white_24dp, ColorUtil.resolveColor(getActivity(), R.attr.icon_color)));
    }

    private void updateIsFavorite() {
        boolean isFavorite = MusicUtil.isFavorite(getActivity(), MusicPlayerRemote.getCurrentSong());
        Drawable favoriteIcon = Util.getTintedDrawable(getActivity(), isFavorite ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_outline_white_24dp, ViewUtil.getToolbarIconColor(getActivity(), false));
        toolbar.getMenu().findItem(R.id.action_toggle_favorite)
                .setIcon(favoriteIcon)
                .setTitle(isFavorite ? getString(R.string.action_remove_from_favorites) : getString(R.string.action_add_to_favorites));
    }

    private void setUpDragSort() {
        DragSortRecycler dragSortRecycler = new DragSortRecycler();
        dragSortRecycler.setViewHandleId(R.id.image);
        dragSortRecycler.setOnItemMovedListener(new DragSortRecycler.OnItemMovedListener() {
            @Override
            public void onItemMoved(int from, int to) {
                if (from == to) return;
                MusicPlayerRemote.moveSong(from, to);
            }
        });

        recyclerView.addItemDecoration(dragSortRecycler);
        recyclerView.addOnItemTouchListener(dragSortRecycler);
        recyclerView.addOnScrollListener(dragSortRecycler.getScrollListener());
        recyclerView.setItemAnimator(null);
    }

    @Override
    @ColorInt
    public int getPaletteColor() {
        return lastColor;
    }

    @SuppressWarnings("ConstantConditions")
    private void animateColorChange(final int newColor) {
        slidingUpPanelLayout.setBackgroundColor(lastColor);
        Animator backgroundAnimator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int topMargin = getResources().getDimensionPixelSize(R.dimen.status_bar_padding);
            int x = (int) (playbackControlsFragment.playPauseFab.getX() + playbackControlsFragment.playPauseFab.getWidth() / 2 + playbackControlsFragment.getView().getX());
            int y = (int) (topMargin + playbackControlsFragment.playPauseFab.getY() + playbackControlsFragment.playPauseFab.getHeight() / 2 + playbackControlsFragment.getView().getY());
            float startRadius = Math.max(playbackControlsFragment.playPauseFab.getWidth() / 2, playbackControlsFragment.playPauseFab.getHeight() / 2);
            float endRadius = Math.max(colorBackground.getWidth(), colorBackground.getHeight());
            colorBackground.setBackgroundColor(newColor);
            backgroundAnimator = ViewAnimationUtils.createCircularReveal(colorBackground, x, y, startRadius, endRadius);
        } else {
            backgroundAnimator = ViewUtil.createBackgroundColorTransition(colorBackground, lastColor, newColor);
        }

        Animator subHeaderAnimator = ViewUtil.createTextColorTransition(playerQueueSubheader, lastColor, newColor);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(backgroundAnimator, subHeaderAnimator);
        animatorSet.setDuration(ViewUtil.PHONOGRAPH_ANIM_TIME).start();

        lastColor = newColor;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        final Song song = MusicPlayerRemote.getCurrentSong();
        switch (item.getItemId()) {
            case R.id.action_toggle_favorite:
                super.onMenuItemClick(item);
                if (MusicUtil.isFavorite(getActivity(), song)) {
                    playerAlbumCoverFragment.showHeartAnimation();
                }
                updateIsFavorite();
                return true;
        }
        return super.onMenuItemClick(item);
    }

    @Override
    public void onHide() {
        playbackControlsFragment.hide();
    }

    @Override
    public void onShow() {
        playbackControlsFragment.show();
    }

    @Override
    public boolean onBackPressed() {
        if (slidingUpPanelLayout.getPanelState() != SlidingUpPanelLayout.PanelState.COLLAPSED) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return true;
        }
        return false;
    }

    @Override
    public void onColorChanged(int color) {
        animateColorChange(color);
        playbackControlsFragment.setColor(color);
        getCallbacks().onPaletteColorChanged();
    }

    @Override
    public void onPanelSlide(View view, float slide) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float density = getResources().getDisplayMetrics().density;
            playingQueueCard.setCardElevation(density * 6 * slide + 2 * density);
        }
    }

    @Override
    public void onPanelCollapsed(View view) {
        resetToCurrentPosition();
    }

    private void resetToCurrentPosition() {
        recyclerView.stopScroll();
        layoutManager.scrollToPositionWithOffset(MusicPlayerRemote.getPosition() + 1, 0);
    }

    @Override
    public void onPanelExpanded(View view) {

    }

    @Override
    public void onPanelAnchored(View view) {

    }

    @Override
    public void onPanelHidden(View view) {

    }
}
