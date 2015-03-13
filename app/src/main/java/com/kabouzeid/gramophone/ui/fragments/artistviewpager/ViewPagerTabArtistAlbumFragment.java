package com.kabouzeid.gramophone.ui.fragments.artistviewpager;

import android.support.v7.widget.RecyclerView;

import com.kabouzeid.gramophone.adapter.AlbumAdapter;
import com.kabouzeid.gramophone.loader.ArtistAlbumLoader;
import com.kabouzeid.gramophone.model.Album;

import java.util.List;

/**
 * Created by karim on 04.01.15.
 */
public class ViewPagerTabArtistAlbumFragment extends AbsViewPagerTabArtistListFragment {

    @Override
    protected RecyclerView.Adapter getAdapter() {
        List<Album> albums = ArtistAlbumLoader.getArtistAlbumList(getActivity(), getArtistId());
        return new AlbumAdapter(getActivity(), albums);
    }

    @Override
    protected int getNumColumns() {
        return 2;
    }
}
