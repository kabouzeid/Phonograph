package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

import android.os.Bundle;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.AlbumViewGridAdapter;
import com.kabouzeid.gramophone.comparator.AlbumAlphabeticComparator;
import com.kabouzeid.gramophone.loader.AlbumLoader;
import com.kabouzeid.gramophone.model.Album;

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
        List<Album> albums = AlbumLoader.getAllAlbums(getActivity());
        fillAbsListView(albums);
    }

    private void fillAbsListView(List<Album> albums) {
        //Collections.sort(albums, new AlbumAlphabeticComparator());
        AlbumViewGridAdapter albumViewGridAdapter = new AlbumViewGridAdapter(getActivity(), albums);
        absListView.setAdapter(albumViewGridAdapter);
        absListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Album album = (Album) parent.getItemAtPosition(position);
                View albumArtView = view.findViewById(R.id.album_art);

                openAlbumDetailsActivity(album, albumArtView);
            }
        });

        absListView.setPadding(0, getTopPadding(app), 0, getBottomPadding(app));
    }

    private void openAlbumDetailsActivity(Album album, View albumArtForTransition) {
        getMainActivity().goToAlbum(album.id, new Pair[]{
                Pair.create(albumArtForTransition, getString(R.string.transition_album_cover))
        });
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
