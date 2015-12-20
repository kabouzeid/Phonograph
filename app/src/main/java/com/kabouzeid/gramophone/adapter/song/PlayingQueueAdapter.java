package com.kabouzeid.gramophone.adapter.song;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.model.Song;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlayingQueueAdapter extends SongAdapter {

    private static final int HISTORY = 0;
    private static final int CURRENT = 1;
    private static final int UP_NEXT = 2;

    private int current;

    public PlayingQueueAdapter(AppCompatActivity activity, ArrayList<Song> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, itemLayoutRes, usePalette, cabHolder);
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder.getItemViewType() == HISTORY) {
            setAlpha(holder, 0.5f);
        } else if (holder.getItemViewType() == CURRENT) {
            holder.itemView.setActivated(true);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < current) {
            return HISTORY;
        } else if (position > current) {
            return UP_NEXT;
        }
        return CURRENT;
    }

    public void swapDataSet(ArrayList<Song> dataSet, int position) {
        this.dataSet = dataSet;
        current = position;
        notifyDataSetChanged();
    }

    public void setCurrent(int current) {
        this.current = current;
        notifyDataSetChanged();
    }

    protected void setAlpha(SongAdapter.ViewHolder holder, float alpha) {
        if (holder.image != null) {
            holder.image.setAlpha(alpha);
        }
        if (holder.title != null) {
            holder.title.setAlpha(alpha);
        }
        if (holder.text != null) {
            holder.text.setAlpha(alpha);
        }
        if (holder.imageText != null) {
            holder.imageText.setAlpha(alpha);
        }
        if (holder.paletteColorContainer != null) {
            holder.paletteColorContainer.setAlpha(alpha);
        }
    }
}
