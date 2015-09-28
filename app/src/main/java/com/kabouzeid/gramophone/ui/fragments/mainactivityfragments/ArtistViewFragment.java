package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.artist.ArtistAdapter;
import com.kabouzeid.gramophone.loader.ArtistLoader;
import com.kabouzeid.gramophone.util.PreferenceUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistViewFragment extends AbsMainActivityRecyclerViewLayoutModeFragment<ArtistAdapter, GridLayoutManager> {

    public static final String TAG = ArtistViewFragment.class.getSimpleName();

    @NonNull
    @Override
    protected GridLayoutManager createLayoutManager() {
        return new GridLayoutManager(getActivity(), getColumnCount());
    }

    @NonNull
    @Override
    protected ArtistAdapter createAdapter() {
        return new ArtistAdapter(
                getMainActivity(),
                ArtistLoader.getAllArtists(getActivity()),
                getItemLayoutRes(),
                loadUsePalette(),
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

    @Override
    protected int loadLayoutMode() {
        return PreferenceUtil.getInstance(getActivity()).getArtistLayoutMode();
    }

    @Override
    protected void saveLayoutMode(int layoutMode) {
        PreferenceUtil.getInstance(getActivity()).setArtistLayoutMode(layoutMode);
    }

    @Override
    public void setUsePaletteAndSaveValue(boolean usePalette) {
        getAdapter().usePalette(usePalette);
        PreferenceUtil.getInstance(getActivity()).setArtistColoredFooters(usePalette);
    }

    @Override
    public boolean loadUsePalette() {
        return PreferenceUtil.getInstance(getActivity()).artistColoredFooters();
    }
}
