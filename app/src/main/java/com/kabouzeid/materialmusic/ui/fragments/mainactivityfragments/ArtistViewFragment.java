package com.kabouzeid.materialmusic.ui.fragments.mainactivityfragments;


import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.kabouzeid.materialmusic.App;
import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.adapter.ArtistViewListAdapter;
import com.kabouzeid.materialmusic.comparator.ArtistAlphabeticComparator;
import com.kabouzeid.materialmusic.interfaces.KabSearchAbleFragment;
import com.kabouzeid.materialmusic.interfaces.KabViewsDisableAble;
import com.kabouzeid.materialmusic.loader.ArtistLoader;
import com.kabouzeid.materialmusic.misc.AppKeys;
import com.kabouzeid.materialmusic.model.Artist;
import com.kabouzeid.materialmusic.ui.activities.ArtistDetailActivity;
import com.kabouzeid.materialmusic.ui.activities.base.AbsFabActivity;
import com.kabouzeid.materialmusic.util.Util;

import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistViewFragment extends Fragment implements KabSearchAbleFragment, KabViewsDisableAble {
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
    public void onResume() {
        super.onResume();
        enableViews();
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

    private void setUpAbsListView(String query) {
        List<Artist> artists = ArtistLoader.getArtists(getActivity(), query);
        fillAbsListView(artists);
    }

    private void fillAbsListView(List<Artist> artists) {
        Collections.sort(artists, new ArtistAlphabeticComparator());
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
                    absFabActivity.goToArtistDetailsActivity(artist.id, sharedElements);
                } else {
                    Intent intent = new Intent(getActivity(), ArtistDetailActivity.class);
                    intent.putExtra(AppKeys.E_ARTIST, artist.id);
                    startActivity(intent);
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (app.isInPortraitMode() || app.isTablet()) {
                absListView.setPadding(0, Util.getActionBarSize(getActivity()) + Util.getStatusBarHeight(getActivity()), 0, Util.getNavigationBarHeight(getActivity()));
            } else {
                absListView.setPadding(0, Util.getActionBarSize(getActivity()) + Util.getStatusBarHeight(getActivity()), 0, 0);
            }
        } else {
            absListView.setPadding(0, Util.getActionBarSize(getActivity()), 0, 0);
        }
    }

    @Override
    public void search(String query) {
        setUpAbsListView(query);
    }

    @Override
    public void returnToNonSearch() {
        setUpAbsListView();
    }

    private void disableParentActivityViews() {
        if (getActivity() instanceof KabViewsDisableAble) {
            ((KabViewsDisableAble) getActivity()).disableViews();
        }
    }

    private boolean areParentActivityViewsEnabled() {
        return !(getActivity() instanceof KabViewsDisableAble) || ((KabViewsDisableAble) getActivity()).areViewsEnabled();
    }

    @Override
    public void enableViews() {
        areViewsEnabled = true;
        absListView.setEnabled(true);
    }

    @Override
    public void disableViews() {
        areViewsEnabled = false;
        absListView.setEnabled(false);
    }

    @Override
    public boolean areViewsEnabled() {
        return areViewsEnabled;
    }
}
