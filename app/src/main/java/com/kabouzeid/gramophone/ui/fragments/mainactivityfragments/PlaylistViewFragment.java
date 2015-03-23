package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;


import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.PlaylistAdapter;

public class PlaylistViewFragment extends AbsMainActivityFragment {
    public static final String TAG = PlaylistViewFragment.class.getSimpleName();

    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        recyclerView.setPadding(0, getTopPadding(), 0, getBottomPadding());
        setUpAdapter();
    }

    private void setUpAdapter() {
        if (recyclerView != null) {
            PlaylistAdapter adapter = new PlaylistAdapter(getActivity());
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void enableViews() {
        super.enableViews();
        recyclerView.setEnabled(true);
    }

    @Override
    public void disableViews() {
        super.disableViews();
        recyclerView.setEnabled(false);
    }
}
