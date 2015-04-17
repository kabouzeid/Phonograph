package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;


import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

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
        PlaylistAdapter adapter = new PlaylistAdapter((ActionBarActivity) getActivity());
        View v = getView();
        if (v != null) {
            v.findViewById(android.R.id.empty).setVisibility(
                    adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
        return adapter;
    }
}