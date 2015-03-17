package com.kabouzeid.gramophone.ui.activities;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.songadapter.PlaylistSongAdapter;
import com.kabouzeid.gramophone.loader.PlaylistLoader;
import com.kabouzeid.gramophone.loader.PlaylistSongLoader;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.PlaylistSong;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.NavigationUtil;

import java.util.List;

public class PlaylistDetailActivity extends AbsFabActivity {
    public static final String TAG = PlaylistDetailActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private Playlist playlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUpTranslucence(false, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        getIntentExtras();
        setUpToolBar();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        List<PlaylistSong> songs = PlaylistSongLoader.getPlaylistSongList(this, playlist.id);
        PlaylistSongAdapter adapter = new PlaylistSongAdapter(this, songs);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(adapter);
    }

    private void getIntentExtras() {
        Bundle intentExtras = getIntent().getExtras();
        final int playlistId = intentExtras.getInt(AppKeys.E_PLAYLIST);
        playlist = PlaylistLoader.getPlaylist(this, playlistId);
        if (playlist == null) {
            finish();
        }
    }

    private void setUpToolBar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(playlist.playlistName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_playlist_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_settings:
                return true;
            case R.id.action_current_playing:
                NavigationUtil.openCurrentPlayingIfPossible(this, getSharedViewsWithFab(null));
                return true;
            case R.id.action_playing_queue:
                NavigationUtil.openPlayingQueueDialog(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
