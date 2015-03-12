package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.AlbumAdapter;
import com.kabouzeid.gramophone.loader.AlbumLoader;
import com.kabouzeid.gramophone.model.Album;

import java.util.List;

/**
 * Created by karim on 22.11.14.
 */
public class AlbumViewFragment extends AbsMainActivityFragment {
    public static final String TAG = AlbumViewFragment.class.getSimpleName();

    private App app;
    private RecyclerView recyclerView;
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
        recyclerView.setEnabled(true);
    }

    @Override
    public void disableViews() {
        super.disableViews();
        recyclerView.setEnabled(false);
    }

    private void initViews() {
        recyclerView = (RecyclerView) fragmentRootView.findViewById(R.id.absList);
    }

    private void setUpViews() {
        setUpAbsListView();
    }

    private void setUpAbsListView() {
        List<Album> albums = AlbumLoader.getAllAlbums(getActivity());
        fillAbsListView(albums);
    }

    private void fillAbsListView(List<Album> albums) {
        AlbumAdapter albumAdapter = new AlbumAdapter(getActivity(), albums);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recyclerView.setAdapter(albumAdapter);
        recyclerView.setPadding(0, getTopPadding(), 0, getBottomPadding());
    }
}
