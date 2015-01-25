package com.kabouzeid.materialmusic.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.model.Album;
import com.kabouzeid.materialmusic.util.MusicUtil;
import com.kabouzeid.materialmusic.util.Util;
import com.kabouzeid.materialmusic.util.ViewUtil;
import com.kabouzeid.materialmusic.view.SquareImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

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
        usePalette = true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Album album = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.album_tile, parent, false);
        }
        final SquareImageView albumArt = (SquareImageView) convertView.findViewById(R.id.album_art);
        final TextView title = (TextView) convertView.findViewById(R.id.album_title);
        final TextView artist = (TextView) convertView.findViewById(R.id.album_interpret);
        final View footer = convertView.findViewById(R.id.footer);

        title.setText(album.title);
        artist.setText(album.artistName);

        ImageLoader.getInstance().displayImage(MusicUtil.getAlbumArtUri(album.id).toString(), albumArt, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                albumArt.setImageDrawable(null);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (usePalette) {
                    paletteBugFixBlackAndWhite(title, artist, footer);
                }
                albumArt.setImageResource(R.drawable.default_album_art);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (usePalette) {
                    applyPalette(loadedImage, title, artist, footer);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

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
                    ViewUtil.animateViewColor(footer, Util.resolveColor(context, R.attr.colorPrimary),
                            vibrantSwatch.getRgb());
                } else {
                    paletteBugFixBlackAndWhite(title, artist, footer);
                }
            }
        });
    }

    private void paletteBugFixBlackAndWhite(TextView title, TextView artist, View footer) {
        title.setTextColor(Util.resolveColor(context, R.attr.title_text_color));
        artist.setTextColor(Util.resolveColor(context, R.attr.caption_text_color));
        ViewUtil.animateViewColor(footer, Util.resolveColor(context, R.attr.colorPrimary),
                Util.resolveColor(context, R.attr.colorPrimary));
    }
}
