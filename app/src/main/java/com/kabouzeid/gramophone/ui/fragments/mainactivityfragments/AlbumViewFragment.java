package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.album.AlbumAdapter;
import com.kabouzeid.gramophone.loader.AlbumLoader;
import com.kabouzeid.gramophone.util.PreferenceUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumViewFragment extends AbsMainActivityRecyclerViewLayoutModeFragment<AlbumAdapter, GridLayoutManager> {
    public static final String TAG = AlbumViewFragment.class.getSimpleName();

    @Override
    protected GridLayoutManager createLayoutManager() {
        return new GridLayoutManager(getActivity(), getColumnCount());
    }

    @NonNull
    @Override
    protected AlbumAdapter createAdapter() {
        return new AlbumAdapter(
                getMainActivity(),
                AlbumLoader.getAllAlbums(getActivity()),
                getItemLayoutRes(),
                loadUsePalette(),
                getMainActivity());
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_albums;
    }

    @Override
    public void setUsePaletteAndSaveValue(boolean usePalette) {
        getAdapter().usePalette(usePalette);
        PreferenceUtil.getInstance(getActivity()).setAlbumColoredFooters(usePalette);
    }

    @Override
    public boolean loadUsePalette() {
        return PreferenceUtil.getInstance(getActivity()).albumColoredFooters();
    }

    @Override
    protected int loadLayoutMode() {
        return PreferenceUtil.getInstance(getActivity()).getAlbumLayoutMode();
    }

    @Override
    protected void saveLayoutMode(int layoutMode) {
        PreferenceUtil.getInstance(getActivity()).setAlbumLayoutMode(layoutMode);
    }

    @Override
    public void onMediaStoreChanged() {
        getAdapter().swapDataSet(AlbumLoader.getAllAlbums(getActivity()));
    }
}
