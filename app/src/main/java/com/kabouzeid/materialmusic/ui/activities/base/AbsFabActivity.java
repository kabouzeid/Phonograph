package com.kabouzeid.materialmusic.ui.activities.base;

import android.os.Bundle;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.interfaces.OnMusicRemoteEventListener;
import com.kabouzeid.materialmusic.misc.SmallOnGestureListener;
import com.kabouzeid.materialmusic.model.MusicRemoteEvent;
import com.melnykov.fab.FloatingActionButton;

/**
 * Created by karim on 22.01.15.
 */
public abstract class AbsFabActivity extends AbsBaseActivity implements OnMusicRemoteEventListener {
    private FloatingActionButton fab;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setUpFab();
    }

    private void setUpFab() {
        updateFabState();
        final GestureDetector gestureDetector = new GestureDetector(this, new SmallOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                openCurrentPlayingIfPossible(null);
                return true;
            }
        });

        getFab().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getApp().getMusicPlayerRemote().getPosition() != -1) {
                    if (getApp().getMusicPlayerRemote().isPlaying()) {
                        getApp().getMusicPlayerRemote().pauseSong();
                    } else {
                        getApp().getMusicPlayerRemote().resumePlaying();
                    }
                } else {
                    Toast.makeText(AbsFabActivity.this, getResources().getString(R.string.nothing_playing), Toast.LENGTH_SHORT).show();
                }
            }
        });

        getFab().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(AbsFabActivity.this, getResources().getString(R.string.hint_fling_to_open), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        getFab().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });
    }

    private void updateFabState() {
        if (getApp().getMusicPlayerRemote().isPlaying()) {
            getFab().setImageResource(R.drawable.ic_pause_white_48dp);
        } else {
            getFab().setImageResource(R.drawable.ic_play_arrow_white_48dp);
        }
    }

    protected FloatingActionButton getFab() {
        if (fab == null) {
            fab = (FloatingActionButton) findViewById(R.id.fab);
            if(fab == null){
                fab = new FloatingActionButton(this);
                Log.e(getTag(), "No FAB found created default FAB.");
            }
        }
        return fab;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateControllerState();
        getApp().getMusicPlayerRemote().addOnMusicRemoteEventListener(this);
    }

    @Override
    public void enableViews() {
        super.enableViews();
        getFab().setEnabled(true);
    }

    @Override
    public void disableViews() {
        super.disableViews();
        getFab().setEnabled(false);
    }

    @Override
    protected boolean openCurrentPlayingIfPossible(Pair[] sharedViews) {
        return super.openCurrentPlayingIfPossible(getSharedViewsWithFab(sharedViews));
    }

    @Override
    public void goToArtistDetailsActivity(int artistId, Pair[] sharedViews) {
        super.goToArtistDetailsActivity(artistId, getSharedViewsWithFab(sharedViews));
    }

    @Override
    public void goToAlbumDetailsActivity(int albumId, Pair[] sharedViews) {
        super.goToAlbumDetailsActivity(albumId, getSharedViewsWithFab(sharedViews));
    }

    private Pair[] getSharedViewsWithFab(Pair[] sharedViews) {
        Pair[] sharedViewsWithFab;
        if (sharedViews != null) {
            sharedViewsWithFab = new Pair[sharedViews.length + 1];
            for (int i = 0; i < sharedViews.length; i++) {
                sharedViewsWithFab[i] = sharedViews[i];
            }
        } else {
            sharedViewsWithFab = new Pair[1];
        }
        sharedViewsWithFab[sharedViewsWithFab.length - 1] = Pair.create((View) getFab(), getString(R.string.transition_fab));
        return sharedViewsWithFab;
    }

    protected void updateControllerState() {
        updateFabState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getApp().getMusicPlayerRemote().removeOnMusicRemoteEventListener(this);
    }

    @Override
    public void onMusicRemoteEvent(MusicRemoteEvent event) {
        switch (event.getAction()) {
            case MusicRemoteEvent.PLAY:
                getFab().setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_white_48dp));
                break;
            case MusicRemoteEvent.PAUSE:
                getFab().setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_white_48dp));
                break;
            case MusicRemoteEvent.RESUME:
                getFab().setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_white_48dp));
                break;
            case MusicRemoteEvent.STOP:
                getFab().setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_white_48dp));
                break;
            case MusicRemoteEvent.QUEUE_COMPLETED:
                getFab().setImageResource(R.drawable.ic_play_arrow_white_48dp);
                break;
        }
    }
}
