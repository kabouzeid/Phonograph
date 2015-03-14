package com.kabouzeid.gramophone.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.util.Pair;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.kabouzeid.gramophone.view.SquareImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by karim on 24.11.14.
 */
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    public static final String TAG = AlbumAdapter.class.getSimpleName();
    private Activity activity;
    private boolean usePalette;
    private List<Album> dataSet;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.album_tile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Album album = dataSet.get(position);

        if (usePalette) resetColors(holder.title, holder.artist, holder.footer);
        Picasso.with(activity)
                .load(MusicUtil.getAlbumArtUri(album.id))
                .placeholder(R.drawable.default_album_art)
                .into(holder.image, new Callback.EmptyCallback() {
                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                        if (usePalette) {
                            final Bitmap bitmap = ((BitmapDrawable) holder.image.getDrawable()).getBitmap();
                            if (bitmap != null)
                                applyPalette(bitmap, holder.title, holder.artist, holder.footer);
                        }
                    }

                    @Override
                    public void onError() {
                        super.onError();
                        if (usePalette) {
                            paletteBlackAndWhite(holder.title, holder.artist, holder.footer);
                        }
                    }
                });

        holder.title.setText(album.title);
        holder.artist.setText(album.artistName);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView image;
        TextView title;
        TextView artist;
        View footer;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.album_art);
            title = (TextView) itemView.findViewById(R.id.album_title);
            artist = (TextView) itemView.findViewById(R.id.album_interpret);
            footer = itemView.findViewById(R.id.footer);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Pair[] albumPairs = new Pair[]{
                    Pair.create(image,
                            activity.getResources().getString(R.string.transition_album_cover)
                    )};
            if (activity instanceof AbsFabActivity)
                albumPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(albumPairs);
            NavigationUtil.goToAlbum(activity, dataSet.get(getPosition()).id, albumPairs);
        }
    }

    public AlbumAdapter(Activity activity, List<Album> objects) {
        this.activity = activity;
        dataSet = objects;

        usePalette = true;
    }

    private void applyPalette(Bitmap bitmap, final TextView title, final TextView artist, final View footer) {
        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                final Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                if (vibrantSwatch != null) {
                    title.setTextColor(vibrantSwatch.getTitleTextColor());
                    artist.setTextColor(vibrantSwatch.getTitleTextColor());
                    ViewUtil.animateViewColor(footer, activity.getResources().getColor(R.color.materialmusic_default_bar_color), vibrantSwatch.getRgb());
                } else {
                    paletteBlackAndWhite(title, artist, footer);
                }
            }
        });
    }

    private void paletteBlackAndWhite(final TextView title, final TextView artist, final View footer) {
        title.setTextColor(Util.resolveColor(activity, R.attr.title_text_color));
        artist.setTextColor(Util.resolveColor(activity, R.attr.caption_text_color));
        int defaultBarColor = activity.getResources().getColor(R.color.materialmusic_default_bar_color);
        ViewUtil.animateViewColor(footer, defaultBarColor, defaultBarColor);
    }

    private void resetColors(final TextView title, final TextView artist, final View footer) {
        title.setTextColor(Util.resolveColor(activity, R.attr.title_text_color));
        artist.setTextColor(Util.resolveColor(activity, R.attr.caption_text_color));
        int defaultBarColor = activity.getResources().getColor(R.color.materialmusic_default_bar_color);
        footer.setBackgroundColor(defaultBarColor);
    }
}
