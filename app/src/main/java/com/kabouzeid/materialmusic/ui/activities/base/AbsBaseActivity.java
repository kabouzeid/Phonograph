package com.kabouzeid.materialmusic.ui.activities.base;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.kabouzeid.materialmusic.App;
import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.adapter.songadapter.SongAdapter;
import com.kabouzeid.materialmusic.interfaces.KabViewsDisableAble;
import com.kabouzeid.materialmusic.misc.AppKeys;
import com.kabouzeid.materialmusic.ui.activities.AlbumDetailActivity;
import com.kabouzeid.materialmusic.ui.activities.ArtistDetailActivity;
import com.kabouzeid.materialmusic.ui.activities.MusicControllerActivity;
import com.kabouzeid.materialmusic.util.Util;

/**
 * Created by karim on 20.01.15.
 */
public abstract class AbsBaseActivity extends ActionBarActivity implements KabViewsDisableAble, SongAdapter.GoToAble {
    private App app;
    private boolean areViewsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getApp().getAppTheme());
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableViews();
    }

    protected void setUpTranslucence(boolean statusBarTranslucent, boolean navigationBarTranslucent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Util.setStatusBarTranslucent(getWindow(), statusBarTranslucent);
            if (getApp().isInPortraitMode() || getApp().isTablet()) {
                Util.setNavBarTranslucent(getWindow(), navigationBarTranslucent);
            }
        }
    }

    protected boolean openCurrentPlayingIfPossible(Pair[] sharedViews) {
        if (getApp().getMusicPlayerRemote().getPosition() != -1) {
            if (areViewsEnabled()) {
                disableViews();
                Intent intent = new Intent(this, MusicControllerActivity.class);
                if (sharedViews != null) {
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                            sharedViews
                    );
                    ActivityCompat.startActivity(this, intent, optionsCompat.toBundle());
                } else {
                    startActivity(intent);
                }
                return true;
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.nothing_playing), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void goToAlbum(int albumId) {
        goToAlbumDetailsActivity(albumId, null);
    }

    @Override
    public void goToArtist(int artistId) {
        goToArtistDetailsActivity(artistId, null);
    }

    public void goToAlbumDetailsActivity(int albumId, Pair[] sharedViews) {
        final Intent intent = new Intent(this, AlbumDetailActivity.class);
        intent.putExtra(AppKeys.E_ALBUM, albumId);
        if (sharedViews != null) {
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                    sharedViews
            );
            ActivityCompat.startActivity(this, intent, optionsCompat.toBundle());
        } else {
            startActivity(intent);
        }
    }

    public void goToArtistDetailsActivity(int artistId, Pair[] sharedViews) {
        final Intent intent = new Intent(this, ArtistDetailActivity.class);
        intent.putExtra(AppKeys.E_ARTIST, artistId);
        if (sharedViews != null) {
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                    sharedViews
            );
            ActivityCompat.startActivity(this, intent, optionsCompat.toBundle());
        } else {
            startActivity(intent);
        }
    }

    @Override
    public boolean areViewsEnabled() {
        return areViewsEnabled;
    }

    @Override
    public void enableViews() {
        areViewsEnabled = true;
    }

    @Override
    public void disableViews() {
        areViewsEnabled = false;
    }

    protected App getApp() {
        if (app == null) {
            app = (App) getApplicationContext();
        }
        return app;
    }
}
