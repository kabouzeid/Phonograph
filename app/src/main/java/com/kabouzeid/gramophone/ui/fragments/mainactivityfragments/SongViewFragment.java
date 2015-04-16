package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.songadapter.SongAdapter;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongViewFragment extends AbsMainActivityRecyclerViewFragment {

    public static final String TAG = SongViewFragment.class.getSimpleName();

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_songview;
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new GridLayoutManager(getActivity(), 1);
    }

    @Override
    protected RecyclerView.Adapter createAdapter() {
        return new SongAdapter((ActionBarActivity) getActivity());
    }
}
