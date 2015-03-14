package com.kabouzeid.gramophone.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.SongDetailDialogHelper;
import com.kabouzeid.gramophone.lastfm.artist.LastFMArtistThumbnailUrlLoader;
import com.kabouzeid.gramophone.loader.SongFilePathLoader;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.SongTagEditorActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * Created by karim on 29.12.14.
 */
public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {
    protected Activity activity;
    protected List<Artist> dataSet;

    public ArtistAdapter(Activity activity, List<Artist> objects) {
        this.activity = activity;
        dataSet = objects;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_artist_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Artist artist = dataSet.get(position);

        holder.artistName.setText(artist.name);
        holder.artistInfo.setText(artist.getSubTitle());
        holder.artistImage.setImageResource(R.drawable.default_artist_image);

        LastFMArtistThumbnailUrlLoader.loadArtistThumbnailUrl(activity, artist.name, false, new LastFMArtistThumbnailUrlLoader.ArtistThumbnailUrlLoaderCallback() {
            @Override
            public void onArtistThumbnailUrlLoaded(String url) {
                Picasso.with(activity)
                        .load(url)
                        .placeholder(R.drawable.default_artist_image)
                        .into(holder.artistImage);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView artistName;
        TextView artistInfo;
        ImageView artistImage;

        public ViewHolder(View itemView) {
            super(itemView);
            artistName = (TextView) itemView.findViewById(R.id.artist_name);
            artistInfo = (TextView) itemView.findViewById(R.id.artist_info);
            artistImage = (ImageView) itemView.findViewById(R.id.artist_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Pair[] artistPairs = new Pair[]{
                    Pair.create(artistImage,
                            activity.getResources().getString(R.string.transition_artist_image)
                    )};
            if (activity instanceof AbsFabActivity)
                artistPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(artistPairs);
            NavigationUtil.goToArtist(activity, dataSet.get(getPosition()).id, artistPairs);
        }
    }
}
