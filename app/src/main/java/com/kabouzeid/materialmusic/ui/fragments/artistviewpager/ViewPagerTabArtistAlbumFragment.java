package com.kabouzeid.materialmusic.ui.fragments.artistviewpager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.adapter.AlbumViewGridAdapter;
import com.kabouzeid.materialmusic.comparator.AlbumAlphabeticComparator;
import com.kabouzeid.materialmusic.interfaces.KabViewsDisableAble;
import com.kabouzeid.materialmusic.loader.ArtistAlbumLoader;
import com.kabouzeid.materialmusic.misc.AppKeys;
import com.kabouzeid.materialmusic.model.Album;
import com.kabouzeid.materialmusic.ui.activities.AlbumDetailActivity;
import com.melnykov.fab.FloatingActionButton;

import java.util.Collections;
import java.util.List;

/**
 * Created by karim on 04.01.15.
 */
public class ViewPagerTabArtistAlbumFragment extends AbsViewPagerTabArtistListFragment {
    private FloatingActionButton fab;

    @Override
    protected ListAdapter getAdapter() {
        List<Album> albums = ArtistAlbumLoader.getArtistAlbumList(getParentActivity(), getArtistId());
        Collections.sort(albums, new AlbumAlphabeticComparator());
        ListAdapter adapter = new AlbumViewGridAdapter(getParentActivity(), albums);
        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Album album = (Album) parent.getItemAtPosition(position);
                View albumArtView = view.findViewById(R.id.album_art);

                openAlbumDetailsActivityIfPossible(album, albumArtView);
            }
        });
        setColumns(2);
        return adapter;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fab = (FloatingActionButton) getParentActivity().findViewById(R.id.fab);
    }

    @SuppressWarnings("unchecked")
    private void openAlbumDetailsActivityIfPossible(Album album, View albumArtForTransition) {
        if (areParentActivitiesViewsEnabled()) {
            disableViews();
            disableParentActivitiesViews();

            final Intent intent = new Intent(getActivity(), AlbumDetailActivity.class);
            intent.putExtra(AppKeys.E_ALBUM, album.id);

            final ActivityOptionsCompat activityOptions;
            if (fab != null && albumArtForTransition != null) {
                activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                        Pair.create(albumArtForTransition, getString(R.string.transition_album_cover)),
                        Pair.create((View) fab, getString(R.string.transition_fab))
                );
            } else {
                activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
            }
            ActivityCompat.startActivity(getActivity(), intent, activityOptions.toBundle());
        }
    }

    private void disableParentActivitiesViews() {
        if (getParentActivity() instanceof KabViewsDisableAble) {
            ((KabViewsDisableAble) getParentActivity()).disableViews();
        }
    }

    private boolean areParentActivitiesViewsEnabled() {
        return !(getParentActivity() instanceof KabViewsDisableAble) || ((KabViewsDisableAble) getParentActivity()).areViewsEnabled();
    }
}
