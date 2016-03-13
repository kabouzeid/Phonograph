package com.kabouzeid.gramophone.ui.fragments.libraryfragments;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.song.ShuffleButtonSongAdapter;
import com.kabouzeid.gramophone.adapter.song.SongAdapter;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongsPagerFragment extends AbsLibraryRecyclerViewCustomGridSizePagerFragment<SongAdapter, GridLayoutManager> {

    public static final String TAG = SongsPagerFragment.class.getSimpleName();

    @NonNull
    @Override
    protected GridLayoutManager createLayoutManager() {
        return new GridLayoutManager(getActivity(), getGridSize());
    }

    @NonNull
    @Override
    protected SongAdapter createAdapter() {
        ArrayList<Song> songs = SongLoader.getAllSongs(getActivity());
        int itemLayoutRes = getItemLayoutRes();
        applyRecyclerViewPaddingForLayoutRes(itemLayoutRes);
        boolean usePalette = loadUsePalette();

        if (getGridSize() <= getMaxGridSizeForList()) {
            return new ShuffleButtonSongAdapter(
                    getLibraryFragment().getMainActivity(),
                    songs,
                    itemLayoutRes,
                    usePalette,
                    getLibraryFragment());
        }
        return new SongAdapter(
                getLibraryFragment().getMainActivity(),
                songs,
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
        getAdapter().swapDataSet(SongLoader.getAllSongs(getActivity()));
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
}
