package com.kabouzeid.gramophone.adapter.album;

import android.graphics.Bitmap;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.base.AbsMultiSelectAdapter;
import com.kabouzeid.gramophone.adapter.base.MediaEntryViewHolder;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.DeleteSongsDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.loader.AlbumSongLoader;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumAdapter extends AbsMultiSelectAdapter<AlbumAdapter.ViewHolder, Album> {

    public static final String TAG = AlbumAdapter.class.getSimpleName();
    private static final int FADE_IN_TIME = 500;

    protected final AppCompatActivity activity;
    protected List<Album> dataSet;

    protected int itemLayoutRes;

    protected boolean usePalette = false;

    public AlbumAdapter(@NonNull AppCompatActivity activity, ArrayList<Album> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
        this.usePalette = usePalette;

        setHasStableIds(true);
    }

    public void usePalette(boolean usePalette) {
        this.usePalette = usePalette;
        notifyDataSetChanged();
    }

    public void swapDataSet(List<Album> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false);
        return createViewHolder(view, viewType);
    }

    protected ViewHolder createViewHolder(View view, int viewType) {
        return new ViewHolder(view);
    }

    protected String getAlbumTitle(Album album) {
        return album.title;
    }

    protected String getAlbumText(Album album) {
        return album.artistName;
    }

    protected String getAlbumImageLoaderUri(Album album) {
        return MusicUtil.getAlbumImageLoaderString(album);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Album album = dataSet.get(position);

        final int defaultBarColor = ColorUtil.resolveColor(activity, R.attr.default_bar_color);
        setColors(defaultBarColor, holder);

        final boolean isChecked = isChecked(album);
        holder.itemView.setActivated(isChecked);
        if (holder.selectedIndicator != null) {
            holder.selectedIndicator.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        }

        if (holder.title != null) {
            holder.title.setText(getAlbumTitle(album));
        }
        if (holder.text != null) {
            holder.text.setText(getAlbumText(album));
        }

        ImageLoader.getInstance().displayImage(
                getAlbumImageLoaderUri(album),
                holder.image,
                new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .showImageOnFail(R.drawable.default_album_art)
                        .resetViewBeforeLoading(true)
                        .postProcessor(new BitmapProcessor() {
                            @Override
                            public Bitmap process(Bitmap bitmap) {
                                holder.paletteColor = ColorUtil.generateColor(activity, bitmap);
                                return bitmap;
                            }
                        })
                        .displayer(new FadeInBitmapDisplayer(FADE_IN_TIME) {
                            @Override
                            public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
                                boolean loadedFromMemoryCache = loadedFrom == LoadedFrom.MEMORY_CACHE;
                                if (loadedFromMemoryCache) {
                                    imageAware.setImageBitmap(bitmap);
                                } else {
                                    super.display(bitmap, imageAware, loadedFrom);
                                }
                                if (usePalette)
                                    setColors(holder.paletteColor, holder);
                            }
                        })
                        .build(),
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        FadeInBitmapDisplayer.animate(view, FADE_IN_TIME);
                        if (usePalette)
                            setColors(defaultBarColor, holder);
                    }
                }
        );
    }

    private void setColors(int color, ViewHolder holder) {
        if (holder.paletteColorContainer != null) {
            holder.paletteColorContainer.setBackgroundColor(color);
            int textColor = ColorUtil.getTextColorForBackground(color);
            if (holder.title != null) {
                holder.title.setTextColor(textColor);
            }
            if (holder.text != null) {
                holder.text.setTextColor(textColor);
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).id;
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

    public class ViewHolder extends MediaEntryViewHolder {

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            setImageTransitionName(activity.getString(R.string.transition_album_art));
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
                if (activity instanceof AbsSlidingMusicPanelActivity)
                    albumPairs = ((AbsSlidingMusicPanelActivity) activity).getSharedViewsWithPlayPauseFab(albumPairs);
                NavigationUtil.goToAlbum(activity, dataSet.get(getAdapterPosition()).id, albumPairs);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
