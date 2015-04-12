package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.AlbumAdapter;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.kabouzeid.gramophone.util.Util;

/**
 * Created by karim on 22.11.14.
 */
public class AlbumViewFragment extends AbsMainActivityRecyclerViewFragment {
    public static final String TAG = AlbumViewFragment.class.getSimpleName();

    private GridLayoutManager layoutManager;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_album_view;
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        int columns = Util.isInPortraitMode(getActivity()) ? PreferenceUtils.getInstance(getActivity()).getAlbumGridColumns() : PreferenceUtils.getInstance(getActivity()).getAlbumGridColumnsLand();
        layoutManager = new GridLayoutManager(getActivity(), columns);
        return layoutManager;
    }

    @Override
    protected RecyclerView.Adapter createAdapter() {
        return new AlbumAdapter(getActivity());
    }

    public void setColumns(int columns) {
        layoutManager.setSpanCount(columns);
        layoutManager.requestLayout();
    }
}
