package com.kabouzeid.materialmusic.ui.fragments.mainactivityfragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.kabouzeid.materialmusic.App;
import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.adapter.AlbumViewGridAdapter;
import com.kabouzeid.materialmusic.comparator.AlbumAlphabeticComparator;
import com.kabouzeid.materialmusic.interfaces.KabViewsDisableAble;
import com.kabouzeid.materialmusic.loader.AlbumLoader;
import com.kabouzeid.materialmusic.misc.AppKeys;
import com.kabouzeid.materialmusic.model.Album;
import com.kabouzeid.materialmusic.ui.activities.AlbumDetailActivity;
import com.melnykov.fab.FloatingActionButton;

import java.util.Collections;
import java.util.List;

/**
 * Created by karim on 22.11.14.
 */
public class AlbumViewFragment extends MainActivityFragment {
    public static final String TAG = AlbumViewFragment.class.getSimpleName();

    private App app;
    private AbsListView absListView;
    private View fragmentRootView;
    private FloatingActionButton fab;
    private boolean areViewsEnabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        app = (App) getActivity().getApplicationContext();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_albumview, container, false);
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

    private void initViews() {
        absListView = (AbsListView) fragmentRootView.findViewById(R.id.absList);
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
    }

    private void setUpViews() {
        setUpAbsListView();
    }

    private void setUpAbsListView() {
        List<Album> albums = AlbumLoader.getAllAlbums(getActivity());
        fillAbsListView(albums);
    }

    private void fillAbsListView(List<Album> albums) {
        Collections.sort(albums, new AlbumAlphabeticComparator());
        AlbumViewGridAdapter albumViewGridAdapter = new AlbumViewGridAdapter(getActivity(), albums);
        absListView.setAdapter(albumViewGridAdapter);
        absListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Album album = (Album) parent.getItemAtPosition(position);
                View albumArtView = view.findViewById(R.id.album_art);

                openAlbumDetailsActivityIfPossible(album, albumArtView);
            }
        });

        absListView.setPadding(0, getTopPadding(app), 0, getBottomPadding(app));
    }

    @SuppressWarnings("unchecked")
    private void openAlbumDetailsActivityIfPossible(Album album, View albumArtForTransition) {
        if (areParentActivitiesViewsEnabled()) {
            disableViews();
            disableParentActivitiesViews();

            final Intent intent = new Intent(getActivity(), AlbumDetailActivity.class);
            intent.putExtra(AppKeys.E_ALBUM, album.id);

            final ActivityOptionsCompat activityOptions;
            if (fab != null && albumArtForTransition != null) {
                activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                        Pair.create(albumArtForTransition, getString(R.string.transition_album_cover)),
                        Pair.create((View) fab, getString(R.string.transition_fab))
                );
            } else {
                activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
            }
            ActivityCompat.startActivity(getActivity(), intent, activityOptions.toBundle());
        }
    }

    private void disableParentActivitiesViews() {
        if (getActivity() instanceof KabViewsDisableAble) {
            ((KabViewsDisableAble) getActivity()).disableViews();
        }
    }

    private boolean areParentActivitiesViewsEnabled() {
        return !(getActivity() instanceof KabViewsDisableAble) || ((KabViewsDisableAble) getActivity()).areViewsEnabled();
    }

    @Override
    public void search(String query) {
        setUpAbsListView(query);
    }

    private void setUpAbsListView(String query) {
        List<Album> albums = AlbumLoader.getAlbums(getActivity(), query);
        fillAbsListView(albums);
    }

    @Override
    public void returnToNonSearch() {
        setUpAbsListView();
    }
}
