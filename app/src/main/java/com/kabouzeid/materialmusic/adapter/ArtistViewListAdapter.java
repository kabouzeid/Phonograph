package com.kabouzeid.materialmusic.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.lastfm.artist.LastFMArtistThumbnailLoader;
import com.kabouzeid.materialmusic.model.Artist;

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
        final ImageView artistArt = (ImageView) convertView.findViewById(R.id.artist_image);

        artistName.setText(artist.name);
        artistArt.setImageResource(R.drawable.default_artist_image);

        final Object tag = artist.name;
        artistArt.setTag(tag);

        LastFMArtistThumbnailLoader.loadArtistThumbnail(context, artist.name, new LastFMArtistThumbnailLoader.ArtistThumbnailLoaderCallback() {
            @Override
            public void onArtistThumbnailLoaded(Bitmap thumbnail) {
                if (artistArt.getTag().equals(tag)) {
                    if (thumbnail != null) {
                        artistArt.setImageBitmap(thumbnail);
                    } else {
                        artistArt.setImageResource(R.drawable.default_artist_image);
                    }
                }
            }
        });

        return convertView;
    }
}
