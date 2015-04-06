package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;


import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.PlaylistAdapter;

public class PlaylistViewFragment extends AbsMainActivityRecyclerViewFragment {
    public static final String TAG = PlaylistViewFragment.class.getSimpleName();

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_playlist_view;
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new GridLayoutManager(getActivity(), 1);
    }

    @Override
    protected RecyclerView.Adapter createAdapter() {
        return new PlaylistAdapter(getActivity());
    }
}
