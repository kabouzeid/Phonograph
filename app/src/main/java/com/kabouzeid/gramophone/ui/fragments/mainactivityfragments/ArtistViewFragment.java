package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.ArtistViewListAdapter;
import com.kabouzeid.gramophone.loader.ArtistLoader;
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.util.NavigationUtil;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistViewFragment extends AbsMainActivityFragment {
    public static final String TAG = ArtistViewFragment.class.getSimpleName();

    private AbsListView absListView;
    private View fragmentRootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        fragmentRootView = view;
        super.onViewCreated(view, savedInstanceState);
        initViews();
        setUpViews();
    }


    @Override
    public void enableViews() {
        super.enableViews();
        absListView.setEnabled(true);
    }

    @Override
    public void disableViews() {
        super.disableViews();
        absListView.setEnabled(false);
    }

    private void initViews() {
        absListView = (AbsListView) fragmentRootView.findViewById(R.id.recyclerView);
    }

    private void setUpViews() {
        setUpAbsListView();
    }

    private void setUpAbsListView() {
        List<Artist> artists = ArtistLoader.getAllArtists(getActivity());
        fillAbsListView(artists);
    }

    private void fillAbsListView(List<Artist> artists) {
        ArtistViewListAdapter artistAdapter = new ArtistViewListAdapter(getActivity(), artists);
        absListView.setAdapter(artistAdapter);
        absListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Artist artist = (Artist) parent.getItemAtPosition(position);
                final View artistImageView = view.findViewById(R.id.artist_image);

                Pair[] sharedElements = {Pair.create(artistImageView, getString(R.string.transition_artist_image))};
                NavigationUtil.goToArtist(getActivity(), artist.id, sharedElements);
            }
        });
        absListView.setPadding(0, getTopPadding(), 0, getBottomPadding());
    }
}
