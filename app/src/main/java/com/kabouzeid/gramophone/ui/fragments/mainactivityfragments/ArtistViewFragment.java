package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;


import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.ArtistAdapter;
import com.kabouzeid.gramophone.loader.ArtistLoader;
import com.kabouzeid.gramophone.model.Artist;

import java.util.List;

public class ArtistViewFragment extends AbsMainActivityFragment {
    public static final String TAG = ArtistViewFragment.class.getSimpleName();

    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        List<Artist> artists = ArtistLoader.getAllArtists(getActivity());
        ArtistAdapter artistAdapter = new ArtistAdapter(getActivity(), artists);

        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        recyclerView.setAdapter(artistAdapter);
        recyclerView.setPadding(0, getTopPadding(), 0, getBottomPadding());
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
