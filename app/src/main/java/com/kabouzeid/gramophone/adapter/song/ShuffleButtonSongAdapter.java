package com.kabouzeid.gramophone.adapter.song;

import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.model.Song;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ShuffleButtonSongAdapter extends SongAdapter {
    private static final int SHUFFLE_BUTTON = 0;
    private static final int SONG = 1;

    public ShuffleButtonSongAdapter(AppCompatActivity activity, ArrayList<Song> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, itemLayoutRes, usePalette, cabHolder);
    }

    @NonNull
    @Override
    public SongAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == SHUFFLE_BUTTON) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_list_single_row, parent, false);
            return createViewHolder(view);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    protected SongAdapter.ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        position--;
        if (position < 0) return -2;
        return super.getItemId(position);
    }

    @Override
    protected Song getIdentifier(int position) {
        position--;
        if (position < 0) return new Song();
        return super.getIdentifier(position);
    }

    @Override
    public int getItemCount() {
        int superItemCount = super.getItemCount();
        return superItemCount == 0 ? 0 : superItemCount + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? SHUFFLE_BUTTON : SONG;
    }

    @Override
    public void onBindViewHolder(@NonNull final SongAdapter.ViewHolder holder, int position) {
        if (holder.getItemViewType() == SHUFFLE_BUTTON) {
            int accentColor = ThemeStore.accentColor(activity);
            if (holder.title != null) {
                holder.title.setText(activity.getResources().getString(R.string.action_shuffle_all).toUpperCase());
                holder.title.setTextColor(accentColor);
                holder.title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            }
            if (holder.text != null) {
                holder.text.setVisibility(View.GONE);
            }
            if (holder.menu != null) {
                holder.menu.setVisibility(View.GONE);
            }
            if (holder.image != null) {
                final int padding = activity.getResources().getDimensionPixelSize(R.dimen.default_item_margin) / 2;
                holder.image.setPadding(padding, padding, padding, padding);
                holder.image.setColorFilter(accentColor);
                holder.image.setImageResource(R.drawable.ic_shuffle_white_24dp);
            }
            if (holder.separator != null) {
                holder.separator.setVisibility(View.VISIBLE);
            }
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.GONE);
            }
        } else {
            super.onBindViewHolder(holder, position - 1);
        }
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        position--;
        if (position < 0) return "";
        return super.getSectionName(position);
    }

    public class ViewHolder extends SongAdapter.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        protected Song getSong() {
            if (getItemViewType() == SHUFFLE_BUTTON) return new Song();
            return dataSet.get(getAdapterPosition() - 1);
        }

        @Override
        public void onClick(View v) {
            if (getItemViewType() == SHUFFLE_BUTTON) {
                MusicPlayerRemote.openAndShuffleQueue(dataSet, true);
                return;
            }
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                MusicPlayerRemote.openQueue(dataSet, getAdapterPosition() - 1, true);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (getItemViewType() == SHUFFLE_BUTTON) return false;
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
