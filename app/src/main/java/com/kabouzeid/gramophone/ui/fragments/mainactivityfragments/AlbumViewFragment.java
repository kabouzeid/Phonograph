package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.album.AlbumAdapter;
import com.kabouzeid.gramophone.loader.AlbumLoader;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.Util;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumViewFragment extends AbsMainActivityRecyclerViewFragment<AlbumAdapter, GridLayoutManager> {
    public static final String TAG = AlbumViewFragment.class.getSimpleName();

    @Override
    protected GridLayoutManager createLayoutManager() {
        int columns = Util.isInPortraitMode(getActivity()) ? PreferenceUtil.getInstance(getActivity()).getAlbumGridColumns() : PreferenceUtil.getInstance(getActivity()).getAlbumGridColumnsLand();
        return new GridLayoutManager(getActivity(), columns);
    }

    @NonNull
    @Override
    protected AlbumAdapter createAdapter() {
        return new AlbumAdapter(
                getMainActivity(),
                AlbumLoader.getAllAlbums(getActivity()),
                R.layout.item_grid,
                getMainActivity());
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_albums;
    }

    public void setColumns(int columns) {
        getLayoutManager().setSpanCount(columns);
        getLayoutManager().requestLayout();
        // required to animate the column size change
        getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onMediaStoreChanged() {
        getAdapter().swapDataSet(AlbumLoader.getAllAlbums(getActivity()));
    }
}
