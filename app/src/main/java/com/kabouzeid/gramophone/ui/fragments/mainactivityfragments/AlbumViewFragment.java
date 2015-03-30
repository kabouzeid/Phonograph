package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.AlbumAdapter;

/**
 * Created by karim on 22.11.14.
 */
public class AlbumViewFragment extends AbsMainActivityRecyclerViewFragment {
    public static final String TAG = AlbumViewFragment.class.getSimpleName();

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_album_view;
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new GridLayoutManager(getActivity(), 2);
    }

    @Override
    protected RecyclerView.Adapter getAdapter() {
        return new AlbumAdapter(getActivity());
    }
}
