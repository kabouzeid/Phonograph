package com.kabouzeid.gramophone.ui.activities;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.songadapter.PlaylistSongAdapter;
import com.kabouzeid.gramophone.loader.PlaylistLoader;
import com.kabouzeid.gramophone.loader.PlaylistSongLoader;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.misc.DragSortRecycler;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.PlaylistSong;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PlaylistsUtil;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.kabouzeid.gramophone.util.Util;

import java.util.ArrayList;

public class PlaylistDetailActivity extends AbsFabActivity {

    public static final String TAG = PlaylistDetailActivity.class.getSimpleName();
    private Playlist playlist;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setStatusBarTranslucent(!Util.hasLollipopSDK());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        getIntentExtras();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        final ArrayList<PlaylistSong> songs = PlaylistSongLoader.getPlaylistSongList(this, playlist.id);
        final PlaylistSongAdapter adapter = new PlaylistSongAdapter(this, songs);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(adapter);

        findViewById(android.R.id.empty).setVisibility(
                songs.size() == 0 ? View.VISIBLE : View.GONE
        );

        DragSortRecycler dragSortRecycler = new DragSortRecycler();
        dragSortRecycler.setViewHandleId(R.id.album_art);

        dragSortRecycler.setOnItemMovedListener(new DragSortRecycler.OnItemMovedListener() {
            @Override
            public void onItemMoved(int from, int to) {
                PlaylistSong song = songs.remove(from);
                songs.add(to, song);
                adapter.notifyDataSetChanged();
                PlaylistsUtil.moveItem(PlaylistDetailActivity.this, playlist.id, from, to);
            }
        });

        recyclerView.addItemDecoration(dragSortRecycler);
        recyclerView.addOnItemTouchListener(dragSortRecycler);
        recyclerView.setOnScrollListener(dragSortRecycler.getScrollListener());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(PreferenceUtils.getInstance(this).getThemeColorPrimary());
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(playlist.name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected boolean shouldColorStatusBar() {
        return true;
    }

    @Override
    protected boolean shouldColorNavBar() {
        return PreferenceUtils.getInstance(this).coloredNavigationBarPlaylistEnabled();
    }

    private void getIntentExtras() {
        Bundle intentExtras = getIntent().getExtras();
        final int playlistId = intentExtras.getInt(AppKeys.E_PLAYLIST);
        playlist = PlaylistLoader.getPlaylist(this, playlistId);
        if (playlist == null) {
            finish();
        }
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
            case R.id.action_equalizer:
                NavigationUtil.openEqualizer(this);
                return true;
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
