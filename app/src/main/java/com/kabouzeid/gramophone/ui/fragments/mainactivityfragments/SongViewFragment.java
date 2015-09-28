package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.song.ShuffleButtonSongAdapter;
import com.kabouzeid.gramophone.adapter.song.SongAdapter;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.MainActivity;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongViewFragment extends AbsMainActivityRecyclerViewLayoutModeFragment<SongAdapter, GridLayoutManager> {

    public static final String TAG = SongViewFragment.class.getSimpleName();

    @NonNull
    @Override
    protected GridLayoutManager createLayoutManager() {
        return new GridLayoutManager(getActivity(), getColumnCount());
    }

    @NonNull
    @Override
    protected SongAdapter createAdapter() {
        MainActivity mainActivity = getMainActivity();
        ArrayList<Song> songs = SongLoader.getAllSongs(getActivity());
        int itemLayoutRes = getItemLayoutRes();
        boolean usePalette = loadUsePalette();

        if (getLayoutMode() == PreferenceUtil.LAYOUT_MODE_LIST) {
            return new ShuffleButtonSongAdapter(
                    mainActivity,
                    songs,
                    itemLayoutRes,
                    usePalette,
                    mainActivity);
        }
        return new SongAdapter(
                mainActivity,
                songs,
                itemLayoutRes,
                usePalette,
                mainActivity);
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
    protected int loadLayoutMode() {
        return PreferenceUtil.getInstance(getActivity()).getSongLayoutMode();
    }

    @Override
    protected void saveLayoutMode(int layoutMode) {
        PreferenceUtil.getInstance(getActivity()).setSongLayoutMode(layoutMode);
    }

    @Override
    public void setUsePaletteAndSaveValue(boolean usePalette) {
        getAdapter().usePalette(usePalette);
        PreferenceUtil.getInstance(getActivity()).setSongColoredFooters(usePalette);
    }

    @Override
    public boolean loadUsePalette() {
        return PreferenceUtil.getInstance(getActivity()).songColoredFooters();
    }
}
