package com.kabouzeid.gramophone.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v4.util.Pair;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.loader.AlbumLoader;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.DataBaseChangedEvent;
import com.kabouzeid.gramophone.model.UiPreferenceChangedEvent;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.ImageViewBitmapInfo;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.BitmapInfo;
import com.squareup.otto.Subscribe;

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
        View view = LayoutInflater.from(activity).inflate(R.layout.item_grid_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        Object tag = holder.image.getTag();
        if (tag instanceof Future) {
            ((Future) tag).cancel();
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Album album = dataSet.get(position);

        resetColors(holder.title, holder.artist, holder.footer);

        holder.title.setText(album.title);
        holder.artist.setText(album.artistName);
        holder.image.setTag(
                Ion.with(activity)
                        .load(MusicUtil.getAlbumArtUri(album.id).toString())
                        .withBitmap()
                        .resize(holder.image.getWidth(), holder.image.getHeight())
                        .centerCrop()
                        .intoImageView(holder.image)
                        .withBitmapInfo()
                        .setCallback(new FutureCallback<ImageViewBitmapInfo>() {
                            @Override
                            public void onCompleted(Exception e, ImageViewBitmapInfo result) {
                                if(result != null){
                                    BitmapInfo info = result.getBitmapInfo();
                                    if(info != null){
                                        Bitmap bitmap = info.bitmap;
                                        if (bitmap != null) {
                                            if (usePalette)
                                                applyPalette(bitmap, holder.title, holder.artist, holder.footer);
                                            return;
                                        }
                                    }
                                }
                                holder.image.setImageResource(R.drawable.default_album_art);
                                if (usePalette)
                                    paletteBlackAndWhite(holder.title, holder.artist, holder.footer);
                            }
                        })
        );
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

    public AlbumAdapter(Activity activity) {
        this.activity = activity;
        usePalette = PreferenceUtils.getInstance(activity).coloredAlbumFootersEnabled();
        loadDataSet();
    }

    private void loadDataSet() {
        dataSet = AlbumLoader.getAllAlbums(activity);
    }

    private void applyPalette(Bitmap bitmap, final TextView title, final TextView artist, final View footer) {
        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                final Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                if (vibrantSwatch != null) {
                    title.setTextColor(vibrantSwatch.getTitleTextColor());
                    artist.setTextColor(vibrantSwatch.getTitleTextColor());
                    ViewUtil.animateViewColor(footer, Util.resolveColor(activity, R.attr.default_bar_color), vibrantSwatch.getRgb());
                } else {
                    paletteBlackAndWhite(title, artist, footer);
                }
            }
        });
    }

    private void paletteBlackAndWhite(final TextView title, final TextView artist, final View footer) {
        title.setTextColor(Util.resolveColor(activity, R.attr.title_text_color));
        artist.setTextColor(Util.resolveColor(activity, R.attr.caption_text_color));
        int defaultBarColor = Util.resolveColor(activity, R.attr.default_bar_color);
        ViewUtil.animateViewColor(footer, defaultBarColor, defaultBarColor);
    }

    private void resetColors(final TextView title, final TextView artist, final View footer) {
        title.setTextColor(Util.resolveColor(activity, R.attr.title_text_color));
        artist.setTextColor(Util.resolveColor(activity, R.attr.caption_text_color));
        int defaultBarColor = Util.resolveColor(activity, R.attr.default_bar_color);
        footer.setBackgroundColor(defaultBarColor);
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
            case DataBaseChangedEvent.ALBUMS_CHANGED:
            case DataBaseChangedEvent.DATABASE_CHANGED:
                loadDataSet();
                notifyDataSetChanged();
                break;
        }
    }

    @Subscribe
    public void onUIChangeEvent(UiPreferenceChangedEvent event) {
        switch (event.getAction()) {
            case UiPreferenceChangedEvent.ALBUM_OVERVIEW_PALETTE_CHANGED:
                usePalette = (boolean) event.getValue();
                notifyDataSetChanged();
                break;
        }
    }
}
