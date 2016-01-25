package com.kabouzeid.gramophone.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.song.PlaylistSongAdapter;
import com.kabouzeid.gramophone.adapter.song.SmartPlaylistSongAdapter;
import com.kabouzeid.gramophone.adapter.song.SongAdapter;
import com.kabouzeid.gramophone.dialogs.SleepTimerDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.loader.PlaylistSongLoader;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.PlaylistSong;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.model.smartplaylist.AbsSmartPlaylist;
import com.kabouzeid.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PlaylistsUtil;

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

    private RecyclerView.Adapter wrappedAdapter;
    private RecyclerViewDragDropManager recyclerViewDragDropManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDrawUnderStatusbar(true);
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        getIntentExtras();

        setUpRecyclerView();

        checkIsEmpty();

        setUpToolBar();
    }

    @Override
    protected View createContentView() {
        return wrapSlidingMusicPanel(R.layout.activity_playlist_detail);
    }

    private void setUpRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (playlist instanceof AbsSmartPlaylist) {
            adapter = new SmartPlaylistSongAdapter(this, loadSmartPlaylistDataSet(), R.layout.item_list, false, this);
            recyclerView.setAdapter(adapter);
        } else {
            recyclerViewDragDropManager = new RecyclerViewDragDropManager();
            final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
            adapter = new PlaylistSongAdapter(this, loadPlaylistDataSet(), R.layout.item_list, false, this, new PlaylistSongAdapter.OnMoveItemListener() {
                @Override
                public void onMoveItem(int fromPosition, int toPosition) {
                    if (PlaylistsUtil.moveItem(PlaylistDetailActivity.this, playlist.id, fromPosition, toPosition)) {
                        Song song = adapter.getDataSet().remove(fromPosition);
                        adapter.getDataSet().add(toPosition, song);
                        adapter.notifyItemMoved(fromPosition, toPosition);
                    }
                }
            });
            wrappedAdapter = recyclerViewDragDropManager.createWrappedAdapter(adapter);

            recyclerView.setAdapter(wrappedAdapter);
            recyclerView.setItemAnimator(animator);

            recyclerViewDragDropManager.attachRecyclerView(recyclerView);
        }

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
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(playlist.name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void getIntentExtras() {
        Bundle intentExtras = getIntent().getExtras();
        try {
            playlist = intentExtras.getParcelable(EXTRA_PLAYLIST);
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
        return super.onCreateOptionsMenu(menu);
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public MaterialCab openCab(final int menu, final MaterialCab.Callback callback) {
        if (cab != null && cab.isActive()) cab.finish();
        cab = new MaterialCab(this, R.id.cab_stub)
                .setMenu(menu)
                .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
                .setBackgroundColor(ColorUtil.shiftBackgroundColorForLightText(ThemeStore.primaryColor(this)))
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

    @Override
    public void onPause() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager.cancelDrag();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager.release();
            recyclerViewDragDropManager = null;
        }

        if (recyclerView != null) {
            recyclerView.setItemAnimator(null);
            recyclerView.setAdapter(null);
            recyclerView = null;
        }

        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter);
            wrappedAdapter = null;
        }
        adapter = null;

        super.onDestroy();
    }
}
