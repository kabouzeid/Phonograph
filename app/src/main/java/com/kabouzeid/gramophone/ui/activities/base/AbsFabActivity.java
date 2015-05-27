package com.kabouzeid.gramophone.ui.activities.base;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.ThemeSingleton;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.ColorChooserDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.misc.SmallOnGestureListener;
import com.kabouzeid.gramophone.model.MusicRemoteEvent;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.views.PlayPauseDrawable;
import com.melnykov.fab.FloatingActionButton;
import com.squareup.otto.Subscribe;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsFabActivity extends AbsBaseActivity {
    public static final String TAG = AbsFabActivity.class.getSimpleName();

    private FloatingActionButton fab;
    private PlayPauseDrawable playPauseDrawable;
    private final Object busEventListener = new Object() {
        @Subscribe
        public void onBusEvent(MusicRemoteEvent event) {
            onMusicRemoteEvent(event);
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        try {
            App.bus.register(busEventListener);
        } catch (Exception ignored) {
        }
        setUpFab();
    }

    private void setUpFab() {
        if (playPauseDrawable == null) {
            playPauseDrawable = new PlayPauseDrawable(this);
        }

        getFab().setImageDrawable(playPauseDrawable);
        final int accentColor = ThemeSingleton.get().positiveColor;
        if (accentColor == Color.WHITE) {
            getFab().setColorNormal(accentColor);
            getFab().setColorPressed(ColorChooserDialog.shiftColorDown(accentColor));
            getFab().setColorRipple(ColorChooserDialog.shiftColorUp(accentColor));
            getFab().getDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        } else {
            getFab().setColorNormal(accentColor);
            getFab().setColorPressed(ColorChooserDialog.shiftColorUp(accentColor));
            getFab().setColorRipple(ColorChooserDialog.shiftColorDown(accentColor));
            getFab().getDrawable().clearColorFilter();
        }

        updateFabState();
        final GestureDetector gestureDetector = new GestureDetector(this, new SmallOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                NavigationUtil.openCurrentPlayingIfPossible(AbsFabActivity.this, getSharedViewsWithFab(null));
                return true;
            }
        });

        getFab().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicPlayerRemote.getPosition() != -1) {
                    if (MusicPlayerRemote.isPlaying()) {
                        MusicPlayerRemote.pauseSong();
                    } else {
                        MusicPlayerRemote.resumePlaying();
                    }
                } else {
                    Toast.makeText(AbsFabActivity.this, getResources().getString(R.string.nothing_playing), Toast.LENGTH_SHORT).show();
                }
            }
        });

        getFab().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });

        getFab().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final Song song = MusicPlayerRemote.getCurrentSong();
                if (song.id != -1) {
                    Toast.makeText(AbsFabActivity.this, song.title + " - " + song.artistName, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AbsFabActivity.this, getResources().getString(R.string.nothing_playing), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    protected void updateFabState() {
        if (MusicPlayerRemote.isPlaying()) {
            playPauseDrawable.setPause();
        } else {
            playPauseDrawable.setPlay();
        }
    }

    protected FloatingActionButton getFab() {
        if (fab == null) {
            fab = (FloatingActionButton) findViewById(R.id.fab);
            if (fab == null) {
                fab = new FloatingActionButton(this);
                Log.e(getTag(), "No FAB found created default FAB.");
            }
        }
        return fab;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFabState();
    }

    public Pair[] getSharedViewsWithFab(Pair[] sharedViews) {
        Pair[] sharedViewsWithFab;
        if (sharedViews != null) {
            sharedViewsWithFab = new Pair[sharedViews.length + 1];
            System.arraycopy(sharedViews, 0, sharedViewsWithFab, 0, sharedViews.length);
        } else {
            sharedViewsWithFab = new Pair[1];
        }
        sharedViewsWithFab[sharedViewsWithFab.length - 1] = Pair.create((View) getFab(), getString(R.string.transition_fab));
        return sharedViewsWithFab;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            App.bus.unregister(busEventListener);
        } catch (Exception ignored) {
        }
    }

    protected void onMusicRemoteEvent(MusicRemoteEvent event) {
        switch (event.getAction()) {
            case MusicRemoteEvent.PLAY:
                setFabPause();
                break;
            case MusicRemoteEvent.PAUSE:
                setFabPlay();
                break;
            case MusicRemoteEvent.RESUME:
                setFabPause();
                break;
            case MusicRemoteEvent.STOP:
                setFabPlay();
                break;
            case MusicRemoteEvent.QUEUE_COMPLETED:
                setFabPlay();
                break;
        }
    }

    private void setFabPlay() {
        playPauseDrawable.animatedPlay();
    }

    private void setFabPause() {
        playPauseDrawable.animatedPause();
    }

    private void setFabColor() {

    }
}
