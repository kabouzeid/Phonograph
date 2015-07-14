package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.artist.ArtistAdapter;
import com.kabouzeid.gramophone.loader.ArtistLoader;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistViewFragment extends AbsMainActivityRecyclerViewFragment<ArtistAdapter, GridLayoutManager> {

    public static final String TAG = ArtistViewFragment.class.getSimpleName();

    @NonNull
    @Override
    protected GridLayoutManager createLayoutManager() {
        return new GridLayoutManager(getActivity(), 2);
    }

    @NonNull
    @Override
    protected ArtistAdapter createAdapter() {
        return new ArtistAdapter(
                getMainActivity(),
                ArtistLoader.getAllArtists(getActivity()),
                R.layout.item_grid,
                getMainActivity());
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_artists;
    }

    @Override
    public void onMediaStoreChanged() {
        getAdapter().swapDataSet(ArtistLoader.getAllArtists(getActivity()));
    }
}
