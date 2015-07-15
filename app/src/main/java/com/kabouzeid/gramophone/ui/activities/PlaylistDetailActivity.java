package com.kabouzeid.gramophone.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.song.PlaylistSongAdapter;
import com.kabouzeid.gramophone.adapter.song.SmartPlaylistSongAdapter;
import com.kabouzeid.gramophone.adapter.song.SongAdapter;
import com.kabouzeid.gramophone.dialogs.SleepTimerDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.loader.PlaylistSongLoader;
import com.kabouzeid.gramophone.misc.DragSortRecycler;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.PlaylistSong;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.model.smartplaylist.AbsSmartPlaylist;
import com.kabouzeid.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PlaylistsUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PlaylistDetailActivity extends AbsSlidingMusicPanelActivity implements CabHolder {

    public static final String TAG = PlaylistDetailActivity.class.getSimpleName();

    @NonNull
    public static String EXTRA_PLAYLIST = "extra_playlist";

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(android.R.id.empty)
    TextView empty;

    private Playlist playlist;
    private MaterialCab cab;
    private SongAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);
        ButterKnife.bind(this);

        getIntentExtras();

        setUpRecyclerView();

        checkIsEmpty();

        setUpToolBar();

        if (PreferenceUtil.getInstance(this).coloredNavigationBarPlaylist())
            setNavigationBarThemeColor();
        setStatusBarThemeColor();
    }

    private void setUpRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        if (playlist instanceof AbsSmartPlaylist) {
            adapter = new SmartPlaylistSongAdapter(this, loadSmartPlaylistDataSet(), R.layout.item_list, this);
        } else {
            adapter = new PlaylistSongAdapter(this, loadPlaylistDataSet(), R.layout.item_list, this);

            DragSortRecycler dragSortRecycler = new DragSortRecycler();
            dragSortRecycler.setViewHandleId(R.id.image);
            dragSortRecycler.setOnItemMovedListener(new DragSortRecycler.OnItemMovedListener() {
                @Override
                public void onItemMoved(int from, int to) {
                    if (from == to) return;

                    if (PlaylistsUtil.moveItem(PlaylistDetailActivity.this, playlist.id, from, to)) {
                        Song song = adapter.getDataSet().remove(from);
                        adapter.getDataSet().add(to, song);
                        adapter.notifyItemMoved(from, to);
                    }
                }
            });

            recyclerView.addItemDecoration(dragSortRecycler);
            recyclerView.addOnItemTouchListener(dragSortRecycler);
            recyclerView.addOnScrollListener(dragSortRecycler.getScrollListener());
            recyclerView.setItemAnimator(null);
        }
        recyclerView.setAdapter(adapter);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkIsEmpty();
            }
        });
    }

    private void reloadDataSet() {
        if (playlist instanceof AbsSmartPlaylist) {
            adapter.swapDataSet(loadSmartPlaylistDataSet());
        } else {
            //noinspection unchecked
            adapter.swapDataSet((ArrayList<Song>) (List) loadPlaylistDataSet());
        }
    }

    private ArrayList<PlaylistSong> loadPlaylistDataSet() {
        return PlaylistSongLoader.getPlaylistSongList(this, playlist.id);
    }

    private ArrayList<Song> loadSmartPlaylistDataSet() {
        return ((AbsSmartPlaylist) playlist).getSongs(this);
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
            playlist = new Playlist();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_playlist_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_sleep_timer:
                new SleepTimerDialog().show(getSupportFragmentManager(), "SET_SLEEP_TIMER");
                return true;
            case R.id.action_shuffle_playlist:
                MusicPlayerRemote.openAndShuffleQueue(adapter.getDataSet(), true);
                return true;
            case R.id.action_equalizer:
                NavigationUtil.openEqualizer(this);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_now_playing:
                NavigationUtil.openCurrentPlayingIfPossible(this, getSharedViewsWithPlayPauseFab(null));
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
                .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
                .setBackgroundColor(getThemeColorPrimary())
                .start(callback);
        return cab;
    }

    @Override
    public void onBackPressed() {
        if (cab != null && cab.isActive()) cab.finish();
        else {
            recyclerView.stopScroll();
            super.onBackPressed();
        }
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        reloadDataSet();
    }

    private void checkIsEmpty() {
        empty.setVisibility(
                adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE
        );
    }
}
