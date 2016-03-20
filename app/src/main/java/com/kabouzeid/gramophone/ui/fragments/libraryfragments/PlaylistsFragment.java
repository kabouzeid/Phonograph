package com.kabouzeid.gramophone.ui.fragments.libraryfragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.MusicLibraryPagerAdapter;
import com.kabouzeid.gramophone.adapter.PlaylistAdapter;
import com.kabouzeid.gramophone.loader.PlaylistLoader;
import com.kabouzeid.gramophone.misc.WrappedAsyncTaskLoader;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.smartplaylist.HistoryPlaylist;
import com.kabouzeid.gramophone.model.smartplaylist.LastAddedPlaylist;
import com.kabouzeid.gramophone.model.smartplaylist.MyTopTracksPlaylist;

import java.util.ArrayList;

import hugo.weaving.DebugLog;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistsFragment extends AbsLibraryPagerRecyclerViewFragment<PlaylistAdapter, LinearLayoutManager> implements LoaderManager.LoaderCallbacks<ArrayList<Playlist>> {

    public static final String TAG = PlaylistsFragment.class.getSimpleName();

    private static final int LOADER_ID = MusicLibraryPagerAdapter.MusicFragments.PLAYLIST.ordinal();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @NonNull
    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @NonNull
    @Override
    protected PlaylistAdapter createAdapter() {
        ArrayList<Playlist> dataSet = getAdapter() == null ? new ArrayList<Playlist>() : getAdapter().getDataSet();
        return new PlaylistAdapter(getLibraryFragment().getMainActivity(), dataSet, R.layout.item_list_single_row, getLibraryFragment());
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_playlists;
    }

    @Override
    public void onMediaStoreChanged() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @DebugLog
    @Override
    public Loader<ArrayList<Playlist>> onCreateLoader(int id, Bundle args) {
        return new AsyncPlaylistLoader(getActivity());
    }

    @DebugLog
    @Override
    public void onLoadFinished(Loader<ArrayList<Playlist>> loader, ArrayList<Playlist> data) {
        getAdapter().swapDataSet(data);
    }

    @DebugLog
    @Override
    public void onLoaderReset(Loader<ArrayList<Playlist>> loader) {
        getAdapter().swapDataSet(new ArrayList<Playlist>());
    }

    private static class AsyncPlaylistLoader extends WrappedAsyncTaskLoader<ArrayList<Playlist>> {
        public AsyncPlaylistLoader(Context context) {
            super(context);
        }

        private static ArrayList<Playlist> getAllPlaylists(Context context) {
            ArrayList<Playlist> playlists = new ArrayList<>();

            playlists.add(new LastAddedPlaylist(context));
            playlists.add(new HistoryPlaylist(context));
            playlists.add(new MyTopTracksPlaylist(context));

            playlists.addAll(PlaylistLoader.getAllPlaylists(context));

            return playlists;
        }

        @DebugLog
        @Override
        public ArrayList<Playlist> loadInBackground() {
            return getAllPlaylists(getContext());
        }
    }
}