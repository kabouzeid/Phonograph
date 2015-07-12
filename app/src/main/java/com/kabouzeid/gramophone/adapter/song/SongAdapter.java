package com.kabouzeid.gramophone.adapter.song;

import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.ThemeSingleton;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.AbsMultiSelectAdapter;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.DeleteSongsDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.menu.SongMenuHelper;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.model.DataBaseChangedEvent;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongAdapter extends AbsMultiSelectAdapter<SongAdapter.ViewHolder, Song> implements MaterialCab.Callback {

    public static final String TAG = AlbumSongAdapter.class.getSimpleName();
    private static final int SHUFFLE_BUTTON = 0;
    private static final int SONG = 1;

    protected final AppCompatActivity activity;
    protected ArrayList<Song> dataSet;

    public SongAdapter(AppCompatActivity activity, CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        loadDataSet();
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        if (position == 0) return -1;
        return dataSet.get(position - 1).id;
    }

    private void loadDataSet() {
        dataSet = SongLoader.getAllSongs(activity);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? SHUFFLE_BUTTON : SONG;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (getItemViewType(position) == SONG) {
            final Song song = dataSet.get(position - 1);

            holder.title.setText(song.title);
            holder.text.setText(song.artistName);
            ImageLoader.getInstance().displayImage(
                    MusicUtil.getSongImageLoaderString(song),
                    holder.image,
                    new DisplayImageOptions.Builder()
                            .cacheInMemory(true)
                            .showImageOnFail(R.drawable.default_album_art)
                            .resetViewBeforeLoading(true)
                            .build()
            );
            holder.itemView.setActivated(isChecked(song));
        } else {
            holder.title.setText(activity.getResources().getString(R.string.action_shuffle_all).toUpperCase());
            holder.title.setTextColor(ThemeSingleton.get().positiveColor);
            holder.title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            holder.text.setVisibility(View.GONE);
            holder.menu.setVisibility(View.GONE);
            final int padding = activity.getResources().getDimensionPixelSize(R.dimen.default_item_margin) / 2;
            holder.image.setPadding(padding, padding, padding, padding);
            holder.image.setColorFilter(ThemeSingleton.get().positiveColor);
            holder.image.setImageResource(R.drawable.ic_shuffle_white_48dp);
            holder.separator.setVisibility(View.VISIBLE);
            holder.short_separator.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        final int dataSetSize = dataSet.size();
        return dataSetSize > 0 ? dataSetSize + 1 : 0;
    }

    @Override
    protected Song getIdentifier(int position) {
        return dataSet.get(position - 1);
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        @InjectView(R.id.title)
        TextView title;
        @InjectView(R.id.text)
        TextView text;
        @InjectView(R.id.menu)
        ImageView menu;
        @InjectView(R.id.image)
        ImageView image;
        @InjectView(R.id.separator)
        View separator;
        @InjectView(R.id.short_separator)
        View short_separator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                image.setTransitionName(activity.getString(R.string.transition_album_art));
            }

            menu.setOnClickListener(new SongMenuHelper.OnClickSongMenu(activity) {
                @Override
                public Song getSong() {
                    return dataSet.get(getAdapterPosition() - 1);
                }

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_go_to_album:
                            Pair[] albumPairs = new Pair[]{
                                    Pair.create(image, activity.getResources().getString(R.string.transition_album_art))
                            };
                            if (activity instanceof AbsFabActivity)
                                albumPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(albumPairs);
                            NavigationUtil.goToAlbum(activity, getSong().albumId, albumPairs);
                            return true;
                    }
                    return super.onMenuItemClick(item);
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (getItemViewType() == SHUFFLE_BUTTON) {
                MusicPlayerRemote.shuffleAllSongs(activity, true);
            } else if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                MusicPlayerRemote.openQueue(dataSet, getAdapterPosition() - 1, true);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (getItemViewType() == SONG)
                toggleChecked(getAdapterPosition());
            return true;
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
            case DataBaseChangedEvent.SONGS_CHANGED:
            case DataBaseChangedEvent.DATABASE_CHANGED:
                loadDataSet();
                notifyDataSetChanged();
                break;
        }
    }
}
