package com.kabouzeid.gramophone.ui.activities.base;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.ThemeSingleton;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.misc.SmallOnGestureListener;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.views.PlayPauseDrawable;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsSlidingMusicPanelActivity extends AbsMusicStateActivity {
    public static final String TAG = AbsSlidingMusicPanelActivity.class.getSimpleName();

    FloatingActionButton playPauseFab;

    private PlayPauseDrawable playPauseDrawable;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setUpPlayPauseButton();
    }

    private void setUpPlayPauseButton() {
        if (playPauseDrawable == null) {
            playPauseDrawable = new PlayPauseDrawable(this);
        }

        getPlayPauseFab().setImageDrawable(playPauseDrawable);
        final int accentColor = ThemeSingleton.get().positiveColor;
        getPlayPauseFab().setBackgroundTintList(ColorUtil.getEmptyColorStateList(accentColor));
        if (accentColor == Color.WHITE) {
            getPlayPauseFab().getDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        } else {
            getPlayPauseFab().getDrawable().clearColorFilter();
        }

        updateFabState(false);
        final GestureDetector gestureDetector = new GestureDetector(this, new SmallOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                NavigationUtil.openCurrentPlayingIfPossible(AbsSlidingMusicPanelActivity.this, getSharedViewsWithPlayPauseFab(null));
                return true;
            }
        });

        getPlayPauseFab().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicPlayerRemote.getPosition() != -1) {
                    if (MusicPlayerRemote.isPlaying()) {
                        MusicPlayerRemote.pauseSong();
                    } else {
                        MusicPlayerRemote.resumePlaying();
                    }
                } else {
                    Toast.makeText(AbsSlidingMusicPanelActivity.this, getResources().getString(R.string.playing_queue_empty), Toast.LENGTH_SHORT).show();
                }
            }
        });

        getPlayPauseFab().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, @NonNull MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });

        getPlayPauseFab().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final Song song = MusicPlayerRemote.getCurrentSong();
                if (song.id != -1) {
                    Toast.makeText(AbsSlidingMusicPanelActivity.this, song.title + " - " + song.artistName, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AbsSlidingMusicPanelActivity.this, getResources().getString(R.string.nothing_playing), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    protected void updateFabState(boolean animate) {
        if (MusicPlayerRemote.isPlaying()) {
            playPauseDrawable.setPause(animate);
        } else {
            playPauseDrawable.setPlay(animate);
        }
    }

    @NonNull
    protected FloatingActionButton getPlayPauseFab() {
        if (playPauseFab == null) {
            playPauseFab = (FloatingActionButton) findViewById(R.id.play_pause_fab);
            if (playPauseFab == null) {
                playPauseFab = new FloatingActionButton(this);
                Log.e(TAG, "PlayPauseFAB not found, created default FAB.");
            }
        }
        return playPauseFab;
    }

    public Pair[] getSharedViewsWithPlayPauseFab(@Nullable Pair[] sharedViews) {
        Pair[] sharedViewsWithFab;
        if (sharedViews != null) {
            sharedViewsWithFab = new Pair[sharedViews.length + 1];
            System.arraycopy(sharedViews, 0, sharedViewsWithFab, 0, sharedViews.length);
        } else {
            sharedViewsWithFab = new Pair[1];
        }
        sharedViewsWithFab[sharedViewsWithFab.length - 1] = Pair.create((View) getPlayPauseFab(), getString(R.string.transition_fab));
        return sharedViewsWithFab;
    }

    @Override
    public void onPlayStateChanged() {
        super.onPlayStateChanged();
        updateFabState(true);
    }
}
