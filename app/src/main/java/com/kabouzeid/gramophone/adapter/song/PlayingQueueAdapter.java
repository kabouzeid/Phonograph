package com.kabouzeid.gramophone.adapter.song;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.annotation.SwipeableItemResults;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.ViewUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlayingQueueAdapter extends SongAdapter
        implements DraggableItemAdapter<PlayingQueueAdapter.ViewHolder>, SwipeableItemAdapter<PlayingQueueAdapter.ViewHolder> {

    private static final int HISTORY = 0;
    private static final int CURRENT = 1;
    private static final int UP_NEXT = 2;

    public Song songToRemove;

    private int current;

    private static AppCompatActivity activity;

    public PlayingQueueAdapter(AppCompatActivity activity, ArrayList<Song> dataSet, int current, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, itemLayoutRes, usePalette, cabHolder);
        this.current = current;
        this.activity = activity;

    }

    @Override
    protected SongAdapter.ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder.imageText != null) {
            holder.imageText.setText(String.valueOf(position - current));
        }
        if (holder.getItemViewType() == HISTORY || holder.getItemViewType() == CURRENT) {
            setAlpha(holder, 0.5f);
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

    @Override
    protected void loadAlbumCover(Song song, SongAdapter.ViewHolder holder) {
        // We don't want to load it in this adapter
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

    @Override
    public boolean onCheckCanStartDrag(ViewHolder holder, int position, int x, int y) {
        return ViewUtil.hitTest(holder.imageText, x, y);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(ViewHolder holder, int position) {
        return null;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        MusicPlayerRemote.moveSong(fromPosition, toPosition);
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return true;
    }

    @Override
    public int onGetSwipeReactionType(ViewHolder holder, int position, int x, int y) {
        if (onCheckCanStartDrag(holder, position, x, y)) {
            return SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_BOTH_H;
        } else {
            return SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H;
        }
    }

    @Override
    public void onSetSwipeBackground(ViewHolder holder, int i, int i1) {
            holder.itemView.setBackgroundColor(getBackgroundColor());
            holder.dummyContainer.setBackgroundColor(ATHUtil.resolveColor(activity, R.attr.cardBackgroundColor));
    }

    @Override
    public SwipeResultAction onSwipeItem(ViewHolder holder, int position, @SwipeableItemResults int result) {

        if (result == SwipeableItemConstants.RESULT_CANCELED) {
            return new SwipeResultActionDefault();
        } else {
            return new MySwipeResultActionRemoveItem(this, position);
        }
    }

    public class ViewHolder extends SongAdapter.ViewHolder {
        @DraggableItemStateFlags
        private int mDragStateFlags;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            if (imageText != null) {
                imageText.setVisibility(View.VISIBLE);
            }
            if (image != null) {
                image.setVisibility(View.GONE);
            }

        }

        @Override
        protected int getSongMenuRes() {
            return R.menu.menu_item_playing_queue_song;
        }

        @Override
        protected boolean onSongMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_remove_from_playing_queue:
                    MusicPlayerRemote.removeFromQueue(getAdapterPosition());
                    return true;
            }
            return super.onSongMenuItemClick(item);
        }

        @Override
        public void setDragStateFlags(int flags) {
            mDragStateFlags = flags;
        }

        @Override
        public int getDragStateFlags() {
            return mDragStateFlags;
        }

        @Override
        public View getSwipeableContainerView() {
            return dummyContainer;
        }
    }

    static class MySwipeResultActionRemoveItem extends SwipeResultActionRemoveItem {
        private PlayingQueueAdapter adapter;
        private int position;
        private Song songToRemove;

        public MySwipeResultActionRemoveItem(PlayingQueueAdapter adapter, int position) {
            this.adapter = adapter;
            this.position = position;
        }

        @Override
        protected void onPerformAction() {
            initializeSnackBar(adapter, position);
            songToRemove = adapter.dataSet.get(position);
        }
        @Override
        protected void onSlideAnimationEnd() {
            //Swipe animation is much smoother when we do the heavy lifting after it's completed
            adapter.setSongToRemove(songToRemove);
            MusicPlayerRemote.removeFromQueue(songToRemove);
        }
    }

    public static int getBackgroundColor(){
        //TODO: Find a better way to get the album background color
        TextView tV = ((TextView) activity.findViewById(R.id.player_queue_sub_header));
        if(tV != null){
            int color = tV.getCurrentTextColor();
            return color;
        }else{
            return ATHUtil.resolveColor(activity, R.attr.cardBackgroundColor);
        }
    }

    public static void initializeSnackBar(final PlayingQueueAdapter adapter,final int position){

        CharSequence snackBarTitle = activity.getString(R.string.snack_bar_title_removed_song);

        Snackbar snackbar = Snackbar.make((View) activity.findViewById(R.id.content_container),
                snackBarTitle,
                Snackbar.LENGTH_LONG);

        TextView songTitle = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);

        songTitle.setSingleLine();
        songTitle.setEllipsize(TextUtils.TruncateAt.END);
        songTitle.setText(adapter.dataSet.get(position).title + snackBarTitle);

        snackbar.setAction(R.string.snack_bar_action_undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayerRemote.addSong(position,adapter.getSongToRemove());
            }
        });
        snackbar.setActionTextColor(getBackgroundColor());
        snackbar.show();

    }

    public void setSongToRemove (@NonNull Song song){
        songToRemove = song;
    }

    public Song getSongToRemove(){
        return songToRemove;
    }
}
