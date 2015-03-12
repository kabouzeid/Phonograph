package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.ArtistViewListAdapter;
import com.kabouzeid.gramophone.comparator.ArtistAlphabeticComparator;
import com.kabouzeid.gramophone.loader.ArtistLoader;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.ui.activities.ArtistDetailActivity;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;

import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistViewFragment extends MainActivityFragment {
    public static final String TAG = ArtistViewFragment.class.getSimpleName();

    private App app;
    private AbsListView absListView;
    private View fragmentRootView;
    private boolean areViewsEnabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        app = (App) getActivity().getApplicationContext();
        super.onCreate(savedInstanceState);
    }

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
        absListView = (AbsListView) fragmentRootView.findViewById(R.id.absList);
    }

    private void setUpViews() {
        setUpAbsListView();
    }

    private void setUpAbsListView() {
        List<Artist> artists = ArtistLoader.getAllArtists(getActivity());
        fillAbsListView(artists);
    }

    private void fillAbsListView(List<Artist> artists) {
        //Collections.sort(artists, new ArtistAlphabeticComparator());
        ArtistViewListAdapter artistAdapter = new ArtistViewListAdapter(getActivity(), artists);
        absListView.setAdapter(artistAdapter);
        absListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Artist artist = (Artist) parent.getItemAtPosition(position);
                final View artistImageView = view.findViewById(R.id.artist_image);

                if (getActivity() instanceof AbsFabActivity) {
                    AbsFabActivity absFabActivity = (AbsFabActivity) getActivity();
                    Pair[] sharedElements = {Pair.create(artistImageView, getString(R.string.transition_artist_image))};
                    absFabActivity.goToArtist(artist.id, sharedElements);
                } else {
                    Intent intent = new Intent(getActivity(), ArtistDetailActivity.class);
                    intent.putExtra(AppKeys.E_ARTIST, artist.id);
                    startActivity(intent);
                }
            }
        });

        absListView.setPadding(0, getTopPadding(app), 0, getBottomPadding(app));

    }

    @Override
    public void search(String query) {
        setUpAbsListView(query);
    }

    private void setUpAbsListView(String query) {
        List<Artist> artists = ArtistLoader.getArtists(getActivity(), query);
        fillAbsListView(artists);
    }

    @Override
    public void returnToNonSearch() {
        setUpAbsListView();
    }
}
