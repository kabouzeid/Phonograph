package com.kabouzeid.gramophone.adapter;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.util.DialogUtils;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.DeleteSongsDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.loader.AlbumLoader;
import com.kabouzeid.gramophone.loader.AlbumSongLoader;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.DataBaseChangedEvent;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.model.UIPreferenceChangedEvent;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumAdapter extends AbsMultiSelectAdapter<AlbumAdapter.ViewHolder, Album> {

    public static final String TAG = AlbumAdapter.class.getSimpleName();
    private static final int FADE_IN_TIME = 500;

    private final AppCompatActivity activity;
    private boolean usePalette;
    private List<Album> dataSet;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_grid_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Album album = dataSet.get(position);

        resetColors(holder.title, holder.artist, holder.footer);

        final boolean isChecked = isChecked(album);
        holder.view.setActivated(isChecked);
        holder.checkMark.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);

        holder.title.setText(album.title);
        holder.artist.setText(album.artistName);

        ImageLoader.getInstance().displayImage(
                MusicUtil.getAlbumArtUri(album.id).toString(),
                holder.albumArt,
                new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .showImageOnFail(R.drawable.default_album_art)
                        .resetViewBeforeLoading(true)
                        .displayer(new FadeInBitmapDisplayer(FADE_IN_TIME))
                        .build(),
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        FadeInBitmapDisplayer.animate(view, FADE_IN_TIME);
                        if (usePalette)
                            paletteBlackAndWhite(holder.title, holder.artist, holder.footer);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if (usePalette)
                            applyPalette(loadedImage, holder.title, holder.artist, holder.footer);
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected Album getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected void onMultipleItemAction(MenuItem menuItem, ArrayList<Album> selection) {
        switch (menuItem.getItemId()) {
            case R.id.action_delete_from_disk:
                DeleteSongsDialog.create(getSongList(selection)).show(activity.getSupportFragmentManager(), "DELETE_SONGS");
                break;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(getSongList(selection)).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                break;
            case R.id.action_add_to_current_playing:
                MusicPlayerRemote.enqueue(getSongList(selection));
                break;
        }
    }

    private ArrayList<Song> getSongList(List<Album> albums) {
        final ArrayList<Song> songs = new ArrayList<>();
        for (Album album : albums) {
            songs.addAll(AlbumSongLoader.getAlbumSongList(activity, album.id));
        }
        return songs;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        final ImageView albumArt;
        final TextView title;
        final TextView artist;
        final View footer;
        final ImageView checkMark;
        final View view;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            albumArt = (ImageView) itemView.findViewById(R.id.album_art);
            title = (TextView) itemView.findViewById(R.id.album_title);
            artist = (TextView) itemView.findViewById(R.id.album_interpret);
            footer = itemView.findViewById(R.id.footer);
            checkMark = (ImageView) itemView.findViewById(R.id.check_mark);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

            // fixes the ripple starts at the right position
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setOnTouchListener(new View.OnTouchListener() {

                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        ((FrameLayout) view.findViewById(R.id.content)).getForeground().setHotspot(motionEvent.getX(), motionEvent.getY());
                        return false;
                    }
                });
            }
        }

        @Override
        public void onClick(View v) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                Pair[] albumPairs = new Pair[]{
                        Pair.create(albumArt,
                                activity.getResources().getString(R.string.transition_album_cover)
                        )};
                if (activity instanceof AbsFabActivity)
                    albumPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(albumPairs);
                NavigationUtil.goToAlbum(activity, dataSet.get(getAdapterPosition()).id, albumPairs);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }

    public AlbumAdapter(AppCompatActivity activity, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        usePalette = PreferenceUtils.getInstance(activity).coloredAlbumFooters();
        loadDataSet();
    }

    private void loadDataSet() {
        dataSet = AlbumLoader.getAllAlbums(activity);
    }

    private void applyPalette(Bitmap bitmap, final TextView title, final TextView artist, final View footer) {
        if (bitmap != null) {
            Palette.from(bitmap)
                    .resizeBitmapSize(100)
                    .generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            final Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                            if (vibrantSwatch != null) {
                                title.setTextColor(Util.getOpaqueColor(vibrantSwatch.getTitleTextColor()));
                                artist.setTextColor(Util.getOpaqueColor(vibrantSwatch.getTitleTextColor()));
                                ViewUtil.animateViewColor(footer, footer.getDrawingCacheBackgroundColor(), vibrantSwatch.getRgb());
                            } else {
                                paletteBlackAndWhite(title, artist, footer);
                            }
                        }
                    });
        } else {
            paletteBlackAndWhite(title, artist, footer);
        }
    }

    private void paletteBlackAndWhite(final TextView title, final TextView artist, final View footer) {
        title.setTextColor(Util.getOpaqueColor(DialogUtils.resolveColor(activity, R.attr.title_text_color)));
        artist.setTextColor(Util.getOpaqueColor(DialogUtils.resolveColor(activity, R.attr.caption_text_color)));
        int defaultBarColor = DialogUtils.resolveColor(activity, R.attr.default_bar_color);
        ViewUtil.animateViewColor(footer, defaultBarColor, defaultBarColor);
    }

    private void resetColors(final TextView title, final TextView artist, final View footer) {
        title.setTextColor(DialogUtils.resolveColor(activity, R.attr.title_text_color));
        artist.setTextColor(DialogUtils.resolveColor(activity, R.attr.caption_text_color));
        int defaultBarColor = DialogUtils.resolveColor(activity, R.attr.default_bar_color);
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
    public void onUIChangeEvent(UIPreferenceChangedEvent event) {
        switch (event.getAction()) {
            case UIPreferenceChangedEvent.ALBUM_OVERVIEW_PALETTE_CHANGED:
                usePalette = (boolean) event.getValue();
                notifyDataSetChanged();
                break;
        }
    }
}
