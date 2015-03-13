package com.kabouzeid.gramophone.ui.fragments.artistviewpager;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.lastfm.artist.LastFMArtistBiographyLoader;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerTabArtistBioFragment extends AbsViewPagerTabArtistListFragment {


    @Override
    protected RecyclerView.Adapter getAdapter() {
        final SimpleTextAdapter adapter = new SimpleTextAdapter(getActivity(), "loading");
        setAdapter(adapter);

        LastFMArtistBiographyLoader.loadArtistBio(getActivity(), getArtistName(), new LastFMArtistBiographyLoader.ArtistBioLoaderCallback() {
            @Override
            public void onArtistBioLoaded(String biography) {
                if (biography == null || biography.trim().equals("")) {
                    try {
                        biography = getResources().getString(R.string.biography_unavailable);
                    } catch (IllegalStateException e) {
                        biography = "Error";
                    }
                }
                adapter.setText(biography);
            }
        });
        return null;
    }

    @Override
    protected int getNumColumns() {
        return 1;
    }

    private static class SimpleTextAdapter extends RecyclerView.Adapter<SimpleTextAdapter.ViewHolder> {
        private Context context;
        private String text;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_artist_details_biography, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textView.setText(Html.fromHtml(text));
            holder.textView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        @Override
        public int getItemCount() {
            return 1;
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.text);
            }
        }

        public SimpleTextAdapter(Context context, String text) {
            this.context = context;
            this.text = text;
        }

        public void setText(String text){
            this.text = text;
            notifyDataSetChanged();
        }
    }
}
