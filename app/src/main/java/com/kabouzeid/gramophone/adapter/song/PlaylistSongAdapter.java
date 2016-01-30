package com.kabouzeid.gramophone.adapter.song;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.RemoveFromPlaylistDialog;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.model.PlaylistSong;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Karim Abou Zeid (kabouzeid)
 */
@SuppressWarnings("unchecked")
public class PlaylistSongAdapter extends SongAdapter implements DraggableItemAdapter<PlaylistSongAdapter.ViewHolder> {

    public static final String TAG = PlaylistSongAdapter.class.getSimpleName();

    private OnMoveItemListener onMoveItemListener;

    public PlaylistSongAdapter(@NonNull AppCompatActivity activity, @NonNull ArrayList<PlaylistSong> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder, @Nullable OnMoveItemListener onMoveItemListener) {
        super(activity, (ArrayList<Song>) (List) dataSet, itemLayoutRes, usePalette, cabHolder);
        overrideMultiSelectMenuRes(R.menu.menu_playlists_songs_selection);
        this.onMoveItemListener = onMoveItemListener;
    }

    @Override
    protected SongAdapter.ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull ArrayList<Song> selection) {
        switch (menuItem.getItemId()) {
            case R.id.action_remove_from_playlist:
                RemoveFromPlaylistDialog.create((ArrayList<PlaylistSong>) (List) selection).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                return;
        }
        super.onMultipleItemAction(menuItem, selection);
    }

    @Override
    public boolean onCheckCanStartDrag(ViewHolder holder, int position, int x, int y) {
        return onMoveItemListener != null && ViewUtil.hitTest(holder.dragView, x, y);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(ViewHolder holder, int position) {
        return null;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (onMoveItemListener != null && fromPosition != toPosition) {
            onMoveItemListener.onMoveItem(fromPosition, toPosition);
        }
    }

    public interface OnMoveItemListener {
        void onMoveItem(int fromPosition, int toPosition);
    }

    public class ViewHolder extends SongAdapter.ViewHolder implements DraggableItemViewHolder {
        @DraggableItemStateFlags
        private int mDragStateFlags;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            if (dragView != null) {
                if (onMoveItemListener != null) {
                    dragView.setVisibility(View.VISIBLE);
                } else {
                    dragView.setVisibility(View.GONE);
                }
            }
        }

        @Override
        protected int getSongMenuRes() {
            return R.menu.menu_item_playlist_song;
        }

        @Override
        protected boolean onSongMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_remove_from_playlist:
                    RemoveFromPlaylistDialog.create((PlaylistSong) getSong()).show(activity.getSupportFragmentManager(), "REMOVE_FROM_PLAYLIST");
                    return true;
            }
            return super.onSongMenuItemClick(item);
        }

        @Override
        public void setDragStateFlags(@DraggableItemStateFlags int flags) {
            mDragStateFlags = flags;
        }

        @Override
        @DraggableItemStateFlags
        public int getDragStateFlags() {
            return mDragStateFlags;
        }
    }
}
