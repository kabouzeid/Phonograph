package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.PlaylistAdapter;
import com.kabouzeid.gramophone.loader.PlaylistLoader;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.smartplaylist.LastAddedPlaylist;
import com.kabouzeid.gramophone.model.smartplaylist.MyTopTracksPlaylist;
import com.kabouzeid.gramophone.model.smartplaylist.RecentlyPlayedPlaylist;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistViewFragment extends AbsMainActivityRecyclerViewFragment {

    public static final String TAG = PlaylistViewFragment.class.getSimpleName();

    @NonNull
    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new GridLayoutManager(getActivity(), 1);
    }

    @NonNull
    @Override
    protected RecyclerView.Adapter createAdapter() {
        ArrayList<Playlist> playlists = new ArrayList<>();

        playlists.add(new LastAddedPlaylist(getActivity()));
        playlists.add(new RecentlyPlayedPlaylist(getActivity()));
        playlists.add(new MyTopTracksPlaylist(getActivity()));

        playlists.addAll(PlaylistLoader.getAllPlaylists(getActivity()));

        return new PlaylistAdapter(getMainActivity(), playlists, R.layout.item_list_single_row, getMainActivity());
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_playlists;
    }
}