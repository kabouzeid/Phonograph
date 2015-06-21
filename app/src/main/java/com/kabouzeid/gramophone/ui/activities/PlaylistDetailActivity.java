package com.kabouzeid.gramophone.ui.activities;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.songadapter.AbsPlaylistSongAdapter;
import com.kabouzeid.gramophone.adapter.songadapter.PlaylistSongAdapter;
import com.kabouzeid.gramophone.adapter.songadapter.smartplaylist.SmartPlaylistSongAdapter;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.loader.PlaylistSongLoader;
import com.kabouzeid.gramophone.misc.DragSortRecycler;
import com.kabouzeid.gramophone.model.DataBaseChangedEvent;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.PlaylistSong;
import com.kabouzeid.gramophone.model.smartplaylist.SmartPlaylist;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PlaylistsUtil;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PlaylistDetailActivity extends AbsFabActivity implements CabHolder {

    public static final String TAG = PlaylistDetailActivity.class.getSimpleName();

    public static String EXTRA_PLAYLIST = "extra_playlist";

    @InjectView(R.id.recycler_view)
    RecyclerView recyclerView;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(android.R.id.empty)
    TextView empty;

    private Playlist playlist;
    private MaterialCab cab;
    private AbsPlaylistSongAdapter adapter;
    private ArrayList<PlaylistSong> songs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);
        ButterKnife.inject(this);

        getIntentExtras();

        setUpRecyclerView();

        checkIsEmpty();

        setUpToolBar();

        if (PreferenceUtils.getInstance(this).coloredNavigationBarPlaylist())
            setNavigationBarThemeColor();
        setStatusBarThemeColor();

        App.bus.register(this);
    }

    private void setUpRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        if (playlist instanceof SmartPlaylist) {
            adapter = ((SmartPlaylist) playlist).createAdapter(this, this);
        } else {
            songs = PlaylistSongLoader.getPlaylistSongList(this, playlist.id);
            adapter = new PlaylistSongAdapter(this, songs, this);

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
        }
        recyclerView.setAdapter(adapter);
    }

    private void setUpToolBar() {
        toolbar.setBackgroundColor(getThemeColorPrimary());
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(playlist.name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void getIntentExtras() {
        Bundle intentExtras = getIntent().getExtras();
        try {
            playlist = (Playlist) intentExtras.getSerializable(EXTRA_PLAYLIST);
        } catch (ClassCastException ignored) {
        }
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
                onBackPressed();
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

    @Override
    public MaterialCab openCab(final int menu, final MaterialCab.Callback callback) {
        if (cab != null && cab.isActive()) cab.finish();
        cab = new MaterialCab(this, R.id.cab_stub)
                .setMenu(menu)
                .setBackgroundColor(PreferenceUtils.getInstance(this).getThemeColorPrimary())
                .start(callback);
        return cab;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.bus.unregister(this);
    }

    @Subscribe
    public void onDataBaseEvent(DataBaseChangedEvent event) {
        switch (event.getAction()) {
            case DataBaseChangedEvent.PLAYLISTS_CHANGED:
            case DataBaseChangedEvent.DATABASE_CHANGED:
                if (adapter instanceof SmartPlaylistSongAdapter) {
                    ((SmartPlaylistSongAdapter) adapter).updateDataSet();
                } else {
                    songs = PlaylistSongLoader.getPlaylistSongList(this, playlist.id);
                    //noinspection unchecked
                    adapter.updateDataSet(songs);
                }
                checkIsEmpty();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (cab != null && cab.isActive()) cab.finish();
        else {
            recyclerView.stopScroll();
            super.onBackPressed();
        }
    }

    private void checkIsEmpty() {
        empty.setVisibility(
                adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE
        );
    }
}
