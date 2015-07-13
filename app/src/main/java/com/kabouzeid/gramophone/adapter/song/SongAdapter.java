package com.kabouzeid.gramophone.adapter.song;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialcab.MaterialCab;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.base.AbsMultiSelectAdapter;
import com.kabouzeid.gramophone.adapter.base.MediaEntryViewHolder;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.DeleteSongsDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.menu.SongMenuHelper;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongAdapter extends AbsMultiSelectAdapter<SongAdapter.ViewHolder, Song> implements MaterialCab.Callback {

    public static final String TAG = AlbumSongAdapter.class.getSimpleName();
    private static final int FADE_IN_TIME = 500;

    protected final AppCompatActivity activity;
    protected ArrayList<Song> dataSet;

    protected int itemLayoutRes;

    public SongAdapter(AppCompatActivity activity, ArrayList<Song> dataSet, @LayoutRes int itemLayoutRes, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
        setHasStableIds(true);
    }

    public void swapDataSet(ArrayList<Song> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    public ArrayList<Song> getDataSet() {
        return dataSet;
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false);
        return createViewHolder(view);
    }

    protected ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Song song = dataSet.get(position);

        if (holder.title != null) {
            holder.title.setText(getSongTitle(song));
        }
        if (holder.text != null) {
            holder.text.setText(getSongText(song));
        }
        if (holder.image != null) {
            ImageLoader.getInstance().displayImage(
                    getSongImageLoaderUri(song),
                    holder.image,
                    new DisplayImageOptions.Builder()
                            .cacheInMemory(true)
                            .showImageOnFail(R.drawable.default_album_art)
                            .resetViewBeforeLoading(true)
                            .displayer(new FadeInBitmapDisplayer(FADE_IN_TIME, true, true, false))
                            .build()
            );
        }

        holder.itemView.setActivated(isChecked(song));
    }

    protected String getSongTitle(Song song) {
        return song.title;
    }

    protected String getSongText(Song song) {
        return song.artistName;
    }

    protected String getSongImageLoaderUri(Song song) {
        return MusicUtil.getSongImageLoaderString(song);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected Song getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull ArrayList<Song> selection) {
        switch (menuItem.getItemId()) {
            case R.id.action_delete_from_disk:
                DeleteSongsDialog.create(selection).show(activity.getSupportFragmentManager(), "DELETE_SONGS");
                break;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(selection).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                break;
            case R.id.action_add_to_current_playing:
                MusicPlayerRemote.enqueue(selection);
                break;
        }
    }

    public class ViewHolder extends MediaEntryViewHolder {
        protected int DEFAULT_MENU_RES = SongMenuHelper.MENU_RES;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            setImageTransitionName(activity.getString(R.string.transition_album_art));

            if (menu == null) {
                return;
            }
            menu.setOnClickListener(new SongMenuHelper.OnClickSongMenu(activity) {
                @Override
                public Song getSong() {
                    return ViewHolder.this.getSong();
                }

                @Override
                public int getMenuRes() {
                    return getSongMenuRes();
                }

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return onSongMenuItemClick(item) || super.onMenuItemClick(item);
                }
            });
        }

        protected Song getSong() {
            return dataSet.get(getAdapterPosition());
        }

        protected int getSongMenuRes() {
            return DEFAULT_MENU_RES;
        }

        protected boolean onSongMenuItemClick(MenuItem item) {
            if (image != null && image.getVisibility() == View.VISIBLE) {
                switch (item.getItemId()) {
                    case R.id.action_go_to_album:
                        Pair[] albumPairs = new Pair[]{
                                Pair.create(image, activity.getResources().getString(R.string.transition_album_art))
                        };
                        if (activity instanceof AbsSlidingMusicPanelActivity)
                            albumPairs = ((AbsSlidingMusicPanelActivity) activity).getSharedViewsWithPlayPauseFab(albumPairs);
                        NavigationUtil.goToAlbum(activity, getSong().albumId, albumPairs);
                        return true;
                }
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                MusicPlayerRemote.openQueue(dataSet, getAdapterPosition(), true);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
