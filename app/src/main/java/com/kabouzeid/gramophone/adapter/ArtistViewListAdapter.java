package com.kabouzeid.gramophone.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.lastfm.artist.LastFMArtistThumbnailUrlLoader;
import com.kabouzeid.gramophone.model.Artist;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by karim on 29.12.14.
 */
public class ArtistViewListAdapter extends ArrayAdapter<Artist> {
    private Context context;


    public ArtistViewListAdapter(Context context, List<Artist> objects) {
        super(context, R.layout.item_artist_view, objects);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Artist artist = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_artist_view, parent, false);
        }
        final TextView artistName = (TextView) convertView.findViewById(R.id.artist_name);
        final TextView artistInfo = (TextView) convertView.findViewById(R.id.artist_info);
        final ImageView artistArt = (ImageView) convertView.findViewById(R.id.artist_image);

        artistName.setText(artist.name);
        artistInfo.setText(artist.getSubTitle());

        artistArt.setImageResource(R.drawable.default_artist_image);

        LastFMArtistThumbnailUrlLoader.loadArtistThumbnailUrl(context, artist.name, false, new LastFMArtistThumbnailUrlLoader.ArtistThumbnailUrlLoaderCallback() {
            @Override
            public void onArtistThumbnailUrlLoaded(String url) {
                Picasso.with(getContext())
                        .load(url)
                        .placeholder(R.drawable.default_artist_image)
                        .into(artistArt);
            }
        });

        return convertView;
    }
}
