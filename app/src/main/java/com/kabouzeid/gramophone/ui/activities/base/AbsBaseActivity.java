package com.kabouzeid.gramophone.ui.activities.base;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.songadapter.SongAdapter;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.ui.activities.AlbumDetailActivity;
import com.kabouzeid.gramophone.ui.activities.ArtistDetailActivity;
import com.kabouzeid.gramophone.ui.activities.MusicControllerActivity;
import com.kabouzeid.gramophone.util.Util;

/**
 * Created by karim on 20.01.15.
 */
public abstract class AbsBaseActivity extends ActionBarActivity implements KabViewsDisableAble, SongAdapter.GoToAble {
    private App app;
    private boolean areViewsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Crashlytics.setString(AppKeys.CL_CURRENT_ACTIVITY, getTag());
        setTheme(getApp().getAppTheme());
        super.onCreate(savedInstanceState);
    }

    protected App getApp() {
        if (app == null) {
            app = (App) getApplicationContext();
        }
        return app;
    }

    public abstract String getTag();

    @Override
    protected void onResume() {
        super.onResume();
        enableViews();
    }

    @Override
    public void enableViews() {
        areViewsEnabled = true;
    }

    @Override
    public void disableViews() {
        areViewsEnabled = false;
    }

    @Override
    public boolean areViewsEnabled() {
        return areViewsEnabled;
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
        if (MusicPlayerRemote.getPosition() != -1) {
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
        goToAlbum(albumId, null);
    }

    @Override
    public void goToArtist(int artistId) {
        goToArtist(artistId, null);
    }

    public void goToArtist(int artistId, Pair[] sharedViews) {
        if(areViewsEnabled()) {
            disableViews();
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
    }

    public void goToAlbum(int albumId, Pair[] sharedViews) {
        if(areViewsEnabled()) {
            disableViews();
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
    }
}
