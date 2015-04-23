package com.kabouzeid.gramophone.adapter;

import android.app.Activity;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.lastfm.artist.LastFMArtistThumbnailUrlLoader;
import com.kabouzeid.gramophone.loader.ArtistLoader;
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.model.DataBaseChangedEvent;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.squareup.otto.Subscribe;

import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {
    protected final Activity activity;
    protected List<Artist> dataSet;

    public ArtistAdapter(Activity activity) {
        this.activity = activity;
        loadDataSet();
    }

    private void loadDataSet() {
        dataSet = ArtistLoader.getAllArtists(activity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_list_artist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Artist artist = dataSet.get(position);

        holder.artistName.setText(artist.name);
        holder.artistInfo.setText(MusicUtil.getArtistInfoString(activity, artist));
        holder.artistImage.setImageResource(R.drawable.default_artist_image);

        LastFMArtistThumbnailUrlLoader.loadArtistThumbnailUrl(activity, artist.name, false, new LastFMArtistThumbnailUrlLoader.ArtistThumbnailUrlLoaderCallback() {
            @Override
            public void onArtistThumbnailUrlLoaded(final String url) {
                Glide.with(activity)
                        .load(url)
                        .error(R.drawable.default_artist_image)
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
        final TextView artistName;
        final TextView artistInfo;
        final ImageView artistImage;

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
            NavigationUtil.goToArtist(activity, dataSet.get(getAdapterPosition()).id, artistPairs);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        App.bus.unregister(this);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        App.bus.register(this);
    }

    @Subscribe
    public void onDataBaseEvent(DataBaseChangedEvent event) {
        switch (event.getAction()) {
            case DataBaseChangedEvent.ARTISTS_CHANGED:
            case DataBaseChangedEvent.DATABASE_CHANGED:
                loadDataSet();
                notifyDataSetChanged();
                break;
        }
    }
}
