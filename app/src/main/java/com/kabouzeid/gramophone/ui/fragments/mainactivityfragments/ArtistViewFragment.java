package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.artist.ArtistAdapter;
import com.kabouzeid.gramophone.loader.ArtistLoader;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistViewFragment extends AbsMainActivityRecyclerViewFragment {

    public static final String TAG = ArtistViewFragment.class.getSimpleName();

    @NonNull
    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new GridLayoutManager(getActivity(), 2);
    }

    @NonNull
    @Override
    protected RecyclerView.Adapter createAdapter() {
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
}
