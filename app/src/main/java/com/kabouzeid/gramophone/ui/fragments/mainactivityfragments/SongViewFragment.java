package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.song.SongAdapter;
import com.kabouzeid.gramophone.loader.SongLoader;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongViewFragment extends AbsMainActivityRecyclerViewFragment<SongAdapter, GridLayoutManager> {

    public static final String TAG = SongViewFragment.class.getSimpleName();

    @NonNull
    @Override
    protected GridLayoutManager createLayoutManager() {
        return new GridLayoutManager(getActivity(), 1);
    }

    @NonNull
    @Override
    protected SongAdapter createAdapter() {
        return new SongAdapter(getMainActivity(), SongLoader.getAllSongs(getActivity()), R.layout.item_list, getMainActivity());
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_songs;
    }

    @Override
    public void onMediaStoreChanged() {
        getAdapter().swapDataSet(SongLoader.getAllSongs(getActivity()));
    }
}
