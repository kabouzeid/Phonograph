package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;


import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.PlaylistAdapter;
import com.kabouzeid.gramophone.model.DataBaseChangedEvent;
import com.squareup.otto.Subscribe;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.bus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.bus.unregister(this);
    }

    @Override
    protected RecyclerView.Adapter createAdapter() {
        PlaylistAdapter adapter = new PlaylistAdapter(getMainActivity(), getMainActivity());
        View v = getView();
        if (v != null) {
            v.findViewById(android.R.id.empty).setVisibility(
                    adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
        return adapter;
    }

    @Subscribe
    public void onDataBaseEvent(DataBaseChangedEvent event) {
        switch (event.getAction()) {
            case DataBaseChangedEvent.PLAYLISTS_CHANGED:
            case DataBaseChangedEvent.DATABASE_CHANGED:
                PlaylistAdapter adapter = (PlaylistAdapter) getAdapter();
                adapter.loadDataSet();
                adapter.notifyDataSetChanged();
                View v = getView();
                if (v != null) {
                    v.findViewById(android.R.id.empty).setVisibility(
                            adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                }
                break;
        }
    }
}