package com.kabouzeid.gramophone.adapter;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
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
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.ViewUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumAdapter extends AbsMultiSelectAdapter<AlbumAdapter.ViewHolder, Album> {

    public static final String TAG = AlbumAdapter.class.getSimpleName();
    private static final int FADE_IN_TIME = 500;

    private final AppCompatActivity activity;
    private boolean usePalette;
    private List<Album> dataSet;
    private int defaultFooterColor;

    public AlbumAdapter(@NonNull AppCompatActivity activity, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        usePalette = PreferenceUtil.getInstance(activity).coloredAlbumFooters();
        defaultFooterColor = ColorUtil.resolveColor(activity, R.attr.default_bar_color);
        loadDataSet();
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Album album = dataSet.get(position);

        setFooterColor(holder.title, holder.text, holder.footer, defaultFooterColor, false);

        final boolean isChecked = isChecked(album);
        holder.itemView.setActivated(isChecked);
        holder.selectedIndicator.setVisibility(isChecked ? View.VISIBLE : View.GONE);

        holder.title.setText(album.title);
        holder.text.setText(album.artistName);

        ImageLoader.getInstance().displayImage(
                MusicUtil.getAlbumImageLoaderString(album),
                holder.image,
                new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .showImageOnFail(R.drawable.default_album_art)
                        .resetViewBeforeLoading(true)
                        .displayer(new FadeInBitmapDisplayer(FADE_IN_TIME, true, true, false))
                        .build(),
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        FadeInBitmapDisplayer.animate(view, FADE_IN_TIME);
                        if (usePalette)
                            applyPalette(null, holder.title, holder.text, holder.footer);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if (usePalette)
                            applyPalette(loadedImage, holder.title, holder.text, holder.footer);
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
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull ArrayList<Album> selection) {
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

    @NonNull
    private ArrayList<Song> getSongList(@NonNull List<Album> albums) {
        final ArrayList<Song> songs = new ArrayList<>();
        for (Album album : albums) {
            songs.addAll(AlbumSongLoader.getAlbumSongList(activity, album.id));
        }
        return songs;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        @InjectView(R.id.image)
        ImageView image;
        @InjectView(R.id.title)
        TextView title;
        @InjectView(R.id.text)
        TextView text;
        @InjectView(R.id.footer)
        View footer;
        @InjectView(R.id.selected_indicator)
        ImageView selectedIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                image.setTransitionName(activity.getString(R.string.transition_album_art));
                // fixes the ripple, so that it starts at the right position instead of in the middle
                itemView.setOnTouchListener(new View.OnTouchListener() {

                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public boolean onTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                        ((FrameLayout) view.findViewById(R.id.container)).getForeground().setHotspot(motionEvent.getX(), motionEvent.getY());
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
                        Pair.create(image,
                                activity.getResources().getString(R.string.transition_album_art)
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

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).id;
    }

    private void loadDataSet() {
        dataSet = AlbumLoader.getAllAlbums(activity);
    }

    private void applyPalette(@Nullable Bitmap bitmap, @NonNull final TextView title, @NonNull final TextView artist, @NonNull final View footer) {
        if (bitmap != null) {
            Palette.from(bitmap)
                    .resizeBitmapSize(100)
                    .generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(@NonNull Palette palette) {
                            setFooterColor(title, artist, footer, palette.getVibrantColor(defaultFooterColor), true);
                        }
                    });
        } else {
            setFooterColor(title, artist, footer, defaultFooterColor, true);
        }
    }

    private void setFooterColor(@NonNull final TextView title, @NonNull final TextView artist, final View footer, int footerColor, boolean animate) {
        int textColor = ColorUtil.getTextColorForBackground(footerColor);
        title.setTextColor(textColor);
        artist.setTextColor(textColor);
        if (animate) {
            ViewUtil.animateViewColor(footer, footer.getDrawingCacheBackgroundColor(), footerColor);
        } else {
            footer.setBackgroundColor(footerColor);
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
    public void onDataBaseEvent(@NonNull DataBaseChangedEvent event) {
        switch (event.getAction()) {
            case DataBaseChangedEvent.ALBUMS_CHANGED:
            case DataBaseChangedEvent.DATABASE_CHANGED:
                loadDataSet();
                notifyDataSetChanged();
                break;
        }
    }

    @Subscribe
    public void onUIChangeEvent(@NonNull UIPreferenceChangedEvent event) {
        switch (event.getAction()) {
            case UIPreferenceChangedEvent.ALBUM_OVERVIEW_PALETTE_CHANGED:
                usePalette = (boolean) event.getValue();
                notifyDataSetChanged();
                break;
        }
    }
}
