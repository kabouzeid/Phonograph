package com.kabouzeid.gramophone.adapter.songadapter;

import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistSongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    final TextView songTitle;
    final TextView songInfo;
    final ImageView overflowButton;
    final ImageView albumArt;
    final View view;

    final AbsPlaylistSongAdapter adapter;

    public PlaylistSongViewHolder(final AbsPlaylistSongAdapter adapter, View itemView, final int songMenu) {
        super(itemView);
        this.adapter = adapter;
        view = itemView;
        songTitle = (TextView) itemView.findViewById(R.id.song_title);
        songInfo = (TextView) itemView.findViewById(R.id.song_info);
        albumArt = (ImageView) itemView.findViewById(R.id.album_art);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        overflowButton = (ImageView) itemView.findViewById(R.id.menu);
        overflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(adapter.activity, v);
                popupMenu.inflate(songMenu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return adapter.onMenuItemClick(item, PlaylistSongViewHolder.this, getAdapterPosition());
                    }
                });
                popupMenu.show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        adapter.onClick(v, getAdapterPosition());
    }

    @Override
    public boolean onLongClick(View v) {
        return adapter.onLongClick(v, getAdapterPosition());
    }

    protected interface onViewHolderMenuItemClickListener {
        boolean onMenuItemClick(MenuItem item, PlaylistSongViewHolder viewHolder, int adapterPosition);
    }

    protected interface onViewHolderClickListener {
        void onClick(View v, int adapterPosition);
    }

    protected interface onViewHolderLongClickListener {
        boolean onLongClick(View v, int adapterPosition);
    }
}
