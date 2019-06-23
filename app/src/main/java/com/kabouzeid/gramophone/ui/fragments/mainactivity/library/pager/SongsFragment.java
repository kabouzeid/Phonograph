package com.kabouzeid.gramophone.ui.fragments.mainactivity.library.pager;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.song.ShuffleButtonSongAdapter;
import com.kabouzeid.gramophone.adapter.song.SongAdapter;
import com.kabouzeid.gramophone.interfaces.LoaderIds;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.misc.WrappedAsyncTaskLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongsFragment extends AbsLibraryPagerRecyclerViewCustomGridSizeFragment<SongAdapter, GridLayoutManager> implements LoaderManager.LoaderCallbacks<List<Song>> {

    private static final int LOADER_ID = LoaderIds.SONGS_FRAGMENT;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @NonNull
    @Override
    protected GridLayoutManager createLayoutManager() {
        return new GridLayoutManager(getActivity(), getGridSize());
    }

    @NonNull
    @Override
    protected SongAdapter createAdapter() {
        int itemLayoutRes = getItemLayoutRes();
        notifyLayoutResChanged(itemLayoutRes);
        boolean usePalette = loadUsePalette();
        List<Song> dataSet = getAdapter() == null ? new ArrayList<>() : getAdapter().getDataSet();

        if (getGridSize() <= getMaxGridSizeForList()) {
            return new ShuffleButtonSongAdapter(
                    getLibraryFragment().getMainActivity(),
                    dataSet,
                    itemLayoutRes,
                    usePalette,
                    getLibraryFragment());
        }
        return new SongAdapter(
                getLibraryFragment().getMainActivity(),
                dataSet,
                itemLayoutRes,
                usePalette,
                getLibraryFragment());
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_songs;
    }

    @Override
    public void onMediaStoreChanged() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    protected String loadSortOrder() {
        return PreferenceUtil.getInstance(getActivity()).getSongSortOrder();
    }

    @Override
    protected void saveSortOrder(String sortOrder) {
        PreferenceUtil.getInstance(getActivity()).setSongSortOrder(sortOrder);
    }

    @Override
    protected void setSortOrder(String sortOrder) {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    protected int loadGridSize() {
        return PreferenceUtil.getInstance(getActivity()).getSongGridSize(getActivity());
    }

    @Override
    protected void saveGridSize(int gridSize) {
        PreferenceUtil.getInstance(getActivity()).setSongGridSize(gridSize);
    }

    @Override
    protected int loadGridSizeLand() {
        return PreferenceUtil.getInstance(getActivity()).getSongGridSizeLand(getActivity());
    }

    @Override
    protected void saveGridSizeLand(int gridSize) {
        PreferenceUtil.getInstance(getActivity()).setSongGridSizeLand(gridSize);
    }

    @Override
    public void saveUsePalette(boolean usePalette) {
        PreferenceUtil.getInstance(getActivity()).setSongColoredFooters(usePalette);
    }

    @Override
    public boolean loadUsePalette() {
        return PreferenceUtil.getInstance(getActivity()).songColoredFooters();
    }

    @Override
    public void setUsePalette(boolean usePalette) {
        getAdapter().usePalette(usePalette);
    }

    @Override
    protected void setGridSize(int gridSize) {
        getLayoutManager().setSpanCount(gridSize);
        getAdapter().notifyDataSetChanged();
    }

    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        return new AsyncSongLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        getAdapter().swapDataSet(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
        getAdapter().swapDataSet(new ArrayList<>());
    }

    private static class AsyncSongLoader extends WrappedAsyncTaskLoader<List<Song>> {
        public AsyncSongLoader(Context context) {
            super(context);
        }

        @Override
        public List<Song> loadInBackground() {
            return SongLoader.getAllSongs(getContext());
        }
    }
}
