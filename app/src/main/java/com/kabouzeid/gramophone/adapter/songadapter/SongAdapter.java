package com.kabouzeid.gramophone.adapter.songadapter;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MenuItemClickHelper;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.model.DataBaseChangedEvent;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.Util;
import com.koushikdutta.ion.Ion;
import com.squareup.otto.Subscribe;

import java.util.List;

/**
 * Created by karim on 27.11.14.
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    public static final String TAG = AlbumSongAdapter.class.getSimpleName();
    private static final int SHUFFLE_BUTTON = 0;
    private static final int SONG = 1;

    protected Activity activity;
    protected List<Song> dataSet;

    public SongAdapter(Activity activity) {
        this.activity = activity;
        loadDataSet();
    }

    private void loadDataSet() {
        dataSet = SongLoader.getAllSongs(activity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_list_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? SHUFFLE_BUTTON : SONG;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (getItemViewType(position) == SONG) {
            final Song song = dataSet.get(position - 1);

            holder.songTitle.setText(song.title);
            holder.songInfo.setText(song.artistName);

            Ion.with(activity)
                    .load(MusicUtil.getAlbumArtUri(song.albumId).toString())
                    .withBitmap()
                    .resize(holder.albumArt.getWidth(), holder.albumArt.getHeight())
                    .centerCrop()
                    .error(R.drawable.default_album_art)
                    .intoImageView(holder.albumArt);
        } else {
            int accentColor = Util.resolveColor(activity, R.attr.colorAccent);
            holder.songTitle.setText(activity.getResources().getString(R.string.shuffle_all).toUpperCase());
            holder.songTitle.setTextColor(accentColor);
            holder.songTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            holder.songInfo.setVisibility(View.GONE);
            holder.overflowButton.setVisibility(View.GONE);
            final int padding = activity.getResources().getDimensionPixelSize(R.dimen.default_item_margin) / 2;
            holder.albumArt.setPadding(padding, padding, padding, padding);
            holder.albumArt.setColorFilter(accentColor);
            holder.albumArt.setImageResource(R.drawable.ic_shuffle_white_48dp);
            holder.separator.setVisibility(View.VISIBLE);
            holder.short_separator.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size() + 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView songTitle;
        TextView songInfo;
        ImageView overflowButton;
        ImageView albumArt;
        View separator;
        View short_separator;

        public ViewHolder(View itemView) {
            super(itemView);
            songTitle = (TextView) itemView.findViewById(R.id.song_title);
            songInfo = (TextView) itemView.findViewById(R.id.song_info);
            albumArt = (ImageView) itemView.findViewById(R.id.album_art);
            overflowButton = (ImageView) itemView.findViewById(R.id.menu);
            separator = itemView.findViewById(R.id.separator);
            short_separator = itemView.findViewById(R.id.short_separator);

            overflowButton.setOnClickListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getItemViewType() == SHUFFLE_BUTTON) {
                        MusicPlayerRemote.shuffleAllSongs(activity);
                    } else {
                        MusicPlayerRemote.openQueue(dataSet, getAdapterPosition() - 1, true);
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            PopupMenu popupMenu = new PopupMenu(activity, v);
            popupMenu.inflate(R.menu.menu_item_song);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    final int position = getAdapterPosition() - 1;
                    switch (item.getItemId()) {
                        case R.id.action_go_to_album:
                            Pair[] albumPairs = new Pair[]{
                                    Pair.create(albumArt, activity.getResources().getString(R.string.transition_album_cover))
                            };
                            if (activity instanceof AbsFabActivity)
                                albumPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(albumPairs);
                            NavigationUtil.goToAlbum(activity, dataSet.get(position).albumId, albumPairs);
                            return true;
                    }
                    return MenuItemClickHelper.handleSongMenuClick(activity, dataSet.get(position), item);
                }
            });
            popupMenu.show();
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
    public void onDataBaseEvent(DataBaseChangedEvent event) {
        switch (event.getAction()) {
            case DataBaseChangedEvent.SONGS_CHANGED:
            case DataBaseChangedEvent.DATABASE_CHANGED:
                loadDataSet();
                notifyDataSetChanged();
                break;
        }
    }
}
