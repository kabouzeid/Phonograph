package com.kabouzeid.gramophone.ui.fragments.artistviewpager;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.kabouzeid.gramophone.adapter.songadapter.AlbumSongAdapter;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.loader.ArtistSongLoader;
import com.kabouzeid.gramophone.model.Song;

import java.util.List;

/**
 * Created by karim on 04.01.15.
 */
public class ViewPagerTabArtistSongListFragment extends AbsViewPagerTabArtistListFragment {
    @Override
    protected RecyclerView.Adapter getAdapter() {
        final List<Song> songs = ArtistSongLoader.getArtistSongList(getActivity(), getArtistId());
        return new AlbumSongAdapter(getActivity(), songs);
    }

    @Override
    protected int getNumColumns() {
        return 1;
    }
}
