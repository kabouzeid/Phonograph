package com.kabouzeid.gramophone.ui.fragments.player;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
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
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.PlayingQueueDialog;
import com.kabouzeid.gramophone.dialogs.SleepTimerDialog;
import com.kabouzeid.gramophone.dialogs.SongDetailDialog;
import com.kabouzeid.gramophone.dialogs.SongShareDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.interfaces.MusicServiceEventListener;
import com.kabouzeid.gramophone.interfaces.PaletteColorHolder;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsMusicServiceActivity;
import com.kabouzeid.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.AbsTagEditorActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.SongTagEditorActivity;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.kabouzeid.gramophone.views.SquareLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.solovyev.android.views.llm.LinearLayoutManager;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PlayerFragment extends Fragment implements MusicServiceEventListener, Toolbar.OnMenuItemClickListener, PaletteColorHolder, PlayerAlbumCoverFragment.OnColorChangedListener, SlidingUpPanelLayout.PanelSlideListener {
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

    @Bind(R.id.current_song)
    View currentSong;
    MediaEntryViewHolder mediaEntryViewHolder;

    private int lastColor;

    private AbsMusicServiceActivity activity;
    private Callbacks callbacks;

    private PlaybackControlsFragment playbackControlsFragment;
    private PlayerAlbumCoverFragment playerAlbumCoverFragment;

    private LinearLayoutManager layoutManager;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        setUpPlayerToolbar();
        setUpSubFragments();

        recyclerView.setAdapter(new PlayingQueueAdapter(
                ((AppCompatActivity) getActivity()),
                SongLoader.getAllSongs(getActivity()),
                R.layout.item_list,
                false,
                ((CabHolder) getActivity())));

        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setChildSize((int) (getResources().getDisplayMetrics().density * 72));
        recyclerView.setLayoutManager(layoutManager);

        //slidingUpPanelLayout.setParallaxOffset(Util.resolveDimensionPixelSize(activity, R.attr.actionBarSize) + getResources().getDimensionPixelSize(R.dimen.status_bar_padding));
        slidingUpPanelLayout.setPanelSlideListener(this);

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setUpPanelAndAlbumCoverHeight();
            }
        });

        activity.addMusicServiceEventListener(this);

        mediaEntryViewHolder = new MediaEntryViewHolder(currentSong) {
        };

        mediaEntryViewHolder.separator.setVisibility(View.VISIBLE);
        mediaEntryViewHolder.shortSeparator.setVisibility(View.GONE);
        mediaEntryViewHolder.title.setText("When I'm Gone");
        mediaEntryViewHolder.text.setText("Eminem");
        mediaEntryViewHolder.image.setScaleType(ImageView.ScaleType.CENTER);
        mediaEntryViewHolder.image.setImageDrawable(Util.getTintedDrawable(activity, R.drawable.ic_volume_up_white_24dp, ColorUtil.resolveColor(activity, R.attr.icon_color)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.removeMusicServiceEventListener(this);
        ButterKnife.unbind(this);
    }

    @Override
    public void onPlayingMetaChanged() {
        updatePlayerMenu();
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

    private void setUpPanelAndAlbumCoverHeight() {
        int topMargin = getResources().getDimensionPixelSize(R.dimen.status_bar_padding);

        final int availablePanelHeight = slidingUpPanelLayout.getHeight() - playerContent.getHeight() + topMargin;
        final int minPanelHeight = (int) getResources().getDisplayMetrics().density * (72 + 32) + topMargin;
        if (availablePanelHeight < minPanelHeight) {
            albumCoverContainer.getLayoutParams().height = albumCoverContainer.getHeight() - (minPanelHeight - availablePanelHeight);
            albumCoverContainer.forceSquare(false);
        }
        slidingUpPanelLayout.setPanelHeight(Math.max(minPanelHeight, availablePanelHeight));
        ((AbsSlidingMusicPanelActivity) activity).setAntiDragView(slidingUpPanelLayout.findViewById(R.id.player_panel));
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
                activity.onBackPressed();
            }
        });
        toolbar.setOnMenuItemClickListener(this);
    }

    private void updatePlayerMenu() {
        boolean isFavorite = MusicUtil.isFavorite(activity, MusicPlayerRemote.getCurrentSong());
        Drawable favoriteIcon = Util.getTintedDrawable(activity, isFavorite ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_outline_white_24dp, ViewUtil.getToolbarIconColor(activity, false));
        toolbar.getMenu().findItem(R.id.action_toggle_favorite)
                .setIcon(favoriteIcon)
                .setTitle(isFavorite ? getString(R.string.action_remove_from_favorites) : getString(R.string.action_add_to_favorites));
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
            int x = (int) (playbackControlsFragment.playPauseFab.getX() + playbackControlsFragment.playPauseFab.getWidth() / 2 + playbackControlsFragment.getView().getX());
            int y = (int) (playbackControlsFragment.playPauseFab.getY() + playbackControlsFragment.playPauseFab.getHeight() / 2 + playbackControlsFragment.getView().getY());
            float startRadius = 0;
            float endRadius = Math.max(colorBackground.getWidth(), colorBackground.getHeight());
            colorBackground.setBackgroundColor(newColor);
            backgroundAnimator = ViewAnimationUtils.createCircularReveal(colorBackground, x, y, startRadius, endRadius);
        } else {
            backgroundAnimator = ViewUtil.createBackgroundColorTransition(colorBackground, lastColor, newColor);
        }

        Animator subHeaderAnimator = ViewUtil.createTextColorTransition(playerQueueSubheader, lastColor, newColor);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(backgroundAnimator, subHeaderAnimator);
        animatorSet.setDuration(1000).start();

        lastColor = newColor;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        final Song song = MusicPlayerRemote.getCurrentSong();
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

    public boolean onBackPressed() {
        if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return true;
        }
        return false;
    }

    @Override
    public void onColorChanged(int color) {
        animateColorChange(color);
        playbackControlsFragment.setColor(color);
        callbacks.onPaletteColorChanged();
    }

    @Override
    public void onPanelSlide(View view, float slide) {
        float density = getResources().getDisplayMetrics().density;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            playingQueueCard.setCardElevation(density * 6 * slide + 2 * density);
        }
    }

    @Override
    public void onPanelCollapsed(View view) {
        if (layoutManager.findLastVisibleItemPosition() < 50) {
            recyclerView.smoothScrollToPosition(0);
        } else {
            recyclerView.scrollToPosition(0);
        }
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

    public interface Callbacks {
        void onPaletteColorChanged();
    }
}
