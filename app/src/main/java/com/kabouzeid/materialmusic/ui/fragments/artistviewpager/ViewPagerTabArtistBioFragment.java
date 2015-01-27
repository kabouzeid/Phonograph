package com.kabouzeid.materialmusic.ui.fragments.artistviewpager;


import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.lastfm.artist.LastFMArtistBiographyLoader;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerTabArtistBioFragment extends AbsViewPagerTabArtistListFragment {


    @Override
    protected ListAdapter getAdapter() {
        final List<String> strings = new ArrayList<>();
        strings.add("loading");
        ListAdapter adapter = new SimpleTextAdapter(getParentActivity(), strings);
        setAdapter(adapter);

        LastFMArtistBiographyLoader.loadArtistBio(getParentActivity(), getArtistName(), new LastFMArtistBiographyLoader.ArtistBioLoaderCallback() {
            @Override
            public void onArtistBioLoaded(String biography) {
                if (biography == null || biography.trim().equals("")) {
                    try {
                        biography = getResources().getString(R.string.biography_unavailable);
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "error while trying to access resources", e);
                        biography = "Error";
                    }
                }
                strings.clear();
                strings.add(biography);
                ListAdapter adapter = new SimpleTextAdapter(getParentActivity(), strings);
                setAdapter(adapter);
            }
        });
        return null;
    }

    private static class SimpleTextAdapter extends ArrayAdapter<String> {
        private Context context;

        public SimpleTextAdapter(Context context, List<String> objects) {
            super(context, R.layout.item_artist_details_biography, objects);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String string = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_artist_details_biography, parent, false);
            }
            TextView text = (TextView) convertView.findViewById(R.id.text);
            text.setText(Html.fromHtml(string));
            text.setMovementMethod(LinkMovementMethod.getInstance());
            return convertView;
        }
    }
}
