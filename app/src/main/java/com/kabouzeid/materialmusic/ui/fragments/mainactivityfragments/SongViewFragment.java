package com.kabouzeid.materialmusic.ui.fragments.mainactivityfragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.kabouzeid.materialmusic.App;
import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.adapter.songadapter.SongViewListAdapter;
import com.kabouzeid.materialmusic.comparator.SongAlphabeticComparator;
import com.kabouzeid.materialmusic.loader.SongLoader;
import com.kabouzeid.materialmusic.model.Song;
import com.kabouzeid.materialmusic.ui.activities.base.AbsBaseActivity;

import java.util.Collections;
import java.util.List;

/**
 * Created by karim on 29.12.14.
 */
public class SongViewFragment extends MainActivityFragment {
    public static final String TAG = SongViewFragment.class.getSimpleName();

    private App app;
    private AbsListView absListView;
    private View fragmentRootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        app = (App) getActivity().getApplicationContext();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_songview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        fragmentRootView = view;
        super.onViewCreated(view, savedInstanceState);
        initViews();
        setUpViews();
    }

    private void initViews() {
        absListView = (AbsListView) fragmentRootView.findViewById(R.id.absList);
    }

    private void setUpViews() {
        setUpAbsListView();
    }

    private void setUpAbsListView() {
        List<Song> songs = SongLoader.getAllSongs(getActivity());
        fillAbsListView(songs);
    }

    private void fillAbsListView(final List<Song> songs) {
        Collections.sort(songs, new SongAlphabeticComparator());
        AbsBaseActivity absBaseActivity = null;
        if (getActivity() instanceof AbsBaseActivity) {
            absBaseActivity = (AbsBaseActivity) getActivity();
        }
        SongViewListAdapter songAdapter = new SongViewListAdapter(getActivity(), absBaseActivity, songs);
        absListView.setAdapter(songAdapter);
        absListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                app.getMusicPlayerRemote().openQueue(songs, position, true);
            }
        });

        absListView.setPadding(0, getTopPadding(app), 0, getBottomPadding(app));
    }

    @Override
    public void search(String query) {
        setUpAbsListView(query);
    }

    private void setUpAbsListView(String query) {
        List<Song> songs = SongLoader.getSongs(getActivity(), query);
        fillAbsListView(songs);
    }

    @Override
    public void returnToNonSearch() {
        setUpAbsListView();
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
}
