package com.kabouzeid.gramophone.ui.fragments.artistviewpager;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.kabouzeid.gramophone.adapter.songadapter.SongAdapter;
import com.kabouzeid.gramophone.comparator.SongAlphabeticComparator;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.loader.ArtistSongLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsBaseActivity;

import java.util.Collections;
import java.util.List;

/**
 * Created by karim on 04.01.15.
 */
public class ViewPagerTabArtistSongListFragment extends AbsViewPagerTabArtistListFragment {
    @Override
    protected ListAdapter getAdapter() {
        final List<Song> songs = ArtistSongLoader.getArtistSongList(getParentActivity(), getArtistId());
        Collections.sort(songs, new SongAlphabeticComparator());
        AbsBaseActivity absBaseActivity = null;
        if (getParentActivity() instanceof AbsBaseActivity) {
            absBaseActivity = (AbsBaseActivity) getParentActivity();
        }
        ListAdapter adapter = new SongAdapter(getParentActivity(), absBaseActivity, songs);
        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MusicPlayerRemote.openQueue(songs, position, true);
            }
        });
        return adapter;
    }
}
