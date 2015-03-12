package com.kabouzeid.gramophone.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.kabouzeid.gramophone.view.SquareImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by karim on 24.11.14.
 */
public class AlbumViewGridAdapter extends ArrayAdapter<Album> {
    public static final String TAG = AlbumViewGridAdapter.class.getSimpleName();
    private Context context;
    private boolean usePalette;

    public AlbumViewGridAdapter(Context context, List<Album> objects) {
        super(context, R.layout.album_tile, objects);
        this.context = context;
        usePalette = false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Album album = getItem(position);
        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.album_tile, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.image = (SquareImageView) convertView.findViewById(R.id.album_art);
            viewHolder.title = (TextView) convertView.findViewById(R.id.album_title);
            viewHolder.artist = (TextView) convertView.findViewById(R.id.album_interpret);
            viewHolder.footer = convertView.findViewById(R.id.footer);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (usePalette) resetColors(viewHolder.title, viewHolder.artist, viewHolder.footer);
        viewHolder.title.setText(album.title);
        viewHolder.artist.setText(album.artistName);

        Picasso.with(context)
                .load(MusicUtil.getAlbumArtUri(album.id))
                .placeholder(R.drawable.default_album_art)
                .into(viewHolder.image, new Callback.EmptyCallback(){
                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                        if(usePalette) {
                            final Bitmap bitmap = ((BitmapDrawable) viewHolder.image.getDrawable()).getBitmap();
                            if (bitmap != null) applyPalette(bitmap, viewHolder.title, viewHolder.artist, viewHolder.footer);
                        }
                    }

                    @Override
                    public void onError() {
                        super.onError();
                        if(usePalette) {
                            paletteBlackAndWhite(viewHolder.title, viewHolder.artist, viewHolder.footer);
                        }
                    }
                });

        return convertView;
    }

    private void applyPalette(Bitmap bitmap, final TextView title, final TextView artist, final View footer) {
        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                final Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                if (vibrantSwatch != null) {
                    title.setTextColor(vibrantSwatch.getTitleTextColor());
                    artist.setTextColor(vibrantSwatch.getTitleTextColor());
                    ViewUtil.animateViewColor(footer, getContext().getResources().getColor(R.color.materialmusic_default_bar_color),vibrantSwatch.getRgb());
                } else {
                    paletteBlackAndWhite(title, artist, footer);
                }
            }
        });
    }

    private void paletteBlackAndWhite(TextView title, TextView artist, View footer) {
        title.setTextColor(Util.resolveColor(context, R.attr.title_text_color));
        artist.setTextColor(Util.resolveColor(context, R.attr.caption_text_color));
        int defaultBarColor = getContext().getResources().getColor(R.color.materialmusic_default_bar_color);
        ViewUtil.animateViewColor(footer, defaultBarColor, defaultBarColor);
    }

    private void resetColors(TextView title, TextView artist, View footer){
        title.setTextColor(Util.resolveColor(context, R.attr.title_text_color));
        artist.setTextColor(Util.resolveColor(context, R.attr.caption_text_color));
        int defaultBarColor = getContext().getResources().getColor(R.color.materialmusic_default_bar_color);
        footer.setBackgroundColor(defaultBarColor);
    }

    static class ViewHolder {
        ImageView image;
        TextView title;
        TextView artist;
        View footer;
    }
}
