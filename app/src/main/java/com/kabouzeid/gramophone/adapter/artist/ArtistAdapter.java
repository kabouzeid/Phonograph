package com.kabouzeid.gramophone.adapter.artist;

import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.base.AbsMultiSelectAdapter;
import com.kabouzeid.gramophone.adapter.base.MediaEntryViewHolder;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.DeleteSongsDialog;
import com.kabouzeid.gramophone.glide.PhonographColoredTarget;
import com.kabouzeid.gramophone.glide.artistimage.ArtistImage;
import com.kabouzeid.gramophone.glide.palette.BitmapPaletteTranscoder;
import com.kabouzeid.gramophone.glide.palette.BitmapPaletteWrapper;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.loader.ArtistSongLoader;
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.ArtistSignatureUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistAdapter extends AbsMultiSelectAdapter<ArtistAdapter.ViewHolder, Artist> implements FastScrollRecyclerView.SectionedAdapter {

    protected final AppCompatActivity activity;
    protected ArrayList<Artist> dataSet;

    protected int itemLayoutRes;

    protected boolean usePalette = false;

    public ArtistAdapter(@NonNull AppCompatActivity activity, ArrayList<Artist> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
        this.usePalette = usePalette;
        setHasStableIds(true);
    }

    public void swapDataSet(ArrayList<Artist> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    public void usePalette(boolean usePalette) {
        this.usePalette = usePalette;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).id;
    }

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
        final Artist artist = dataSet.get(position);

//        final int defaultBarColor = PhonographColorUtil.resolveColor(activity, R.attr.default_bar_color);
//        setColors(defaultBarColor, holder);

        boolean isChecked = isChecked(artist);
        holder.itemView.setActivated(isChecked);
        if (holder.selectedIndicator != null) {
            holder.selectedIndicator.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        }

        if (holder.title != null) {
            holder.title.setText(artist.name);
        }
        if (holder.text != null) {
            holder.text.setText(MusicUtil.getArtistInfoString(activity, artist));
        }
        holder.itemView.setActivated(isChecked(artist));

        loadArtistImage(artist, holder);
    }

    private void setColors(int color, ViewHolder holder) {
        if (holder.paletteColorContainer != null) {
            // TODO
//            holder.paletteColorContainer.setBackgroundColor(color);
//            if (holder.title != null) {
//                holder.title.setTextColor(PhonographColorUtil.getPrimaryTextColorForBackground(activity, color));
//            }
//            if (holder.text != null) {
//                holder.text.setTextColor(PhonographColorUtil.getSecondaryTextColorForBackground(activity, color));
//            }
        }
    }

    protected void loadArtistImage(Artist artist, final ViewHolder holder) {
        if (holder.image == null) return;
        Glide.with(activity)
                .load(new ArtistImage(artist.name, false))
                .asBitmap()
                .transcode(new BitmapPaletteTranscoder(activity), BitmapPaletteWrapper.class)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.default_artist_image)
                .animate(android.R.anim.fade_in)
                .priority(Priority.LOW)
                .signature(ArtistSignatureUtil.getInstance(activity).getArtistSignature(artist.name))
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .into(new PhonographColoredTarget(holder.image) {
                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
                        setColors(getDefaultBarColor(), holder);
                    }

                    @Override
                    public void onColorReady(int color) {
                        if (usePalette)
                            setColors(color, holder);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected Artist getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected String getName(Artist artist) {
        return artist.name;
    }

    @Override
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull ArrayList<Artist> selection) {
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
    private ArrayList<Song> getSongList(@NonNull List<Artist> artists) {
        final ArrayList<Song> songs = new ArrayList<>();
        for (Artist artist : artists) {
            songs.addAll(ArtistSongLoader.getArtistSongList(activity, artist.id));
        }
        return songs;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return MusicUtil.getSectionName(dataSet.get(position).name);
    }

    public class ViewHolder extends MediaEntryViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            setImageTransitionName(activity.getString(R.string.transition_artist_image));
            if (menu != null) {
                menu.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                Pair[] artistPairs = new Pair[]{
                        Pair.create(image,
                                activity.getResources().getString(R.string.transition_artist_image)
                        )};
                NavigationUtil.goToArtist(activity, dataSet.get(getAdapterPosition()).id, artistPairs);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
