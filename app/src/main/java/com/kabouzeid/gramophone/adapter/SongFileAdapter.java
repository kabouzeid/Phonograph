package com.kabouzeid.gramophone.adapter;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.base.AbsMultiSelectAdapter;
import com.kabouzeid.gramophone.adapter.base.MediaEntryViewHolder;
import com.kabouzeid.gramophone.glide.audiocover.AudioFileCover;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.util.Util;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class SongFileAdapter extends AbsMultiSelectAdapter<SongFileAdapter.ViewHolder, File> {

    private static final int FILE = 0;
    private static final int FOLDER = 1;

    private final AppCompatActivity activity;
    private ArrayList<File> dataSet;
    private final int itemLayoutRes;
    @Nullable
    private final Callbacks callbacks;
    @Nullable
    private final CabHolder cabHolder;

    public SongFileAdapter(@NonNull AppCompatActivity activity, @NonNull ArrayList<File> dataSet, @LayoutRes int itemLayoutRes, @Nullable Callbacks callback, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
        this.callbacks = callback;
        this.cabHolder = cabHolder;
        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        return dataSet.get(position).isDirectory() ? FOLDER : FILE;
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).hashCode();
    }

    public void swapDataSet(@NonNull ArrayList<File> files) {
        this.dataSet = files;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int index) {
        final File file = dataSet.get(index);

        holder.itemView.setActivated(isChecked(file));

        if (holder.getAdapterPosition() == getItemCount() - 1) {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.GONE);
            }
        } else {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.VISIBLE);
            }
        }

        if (holder.title != null) {
            holder.title.setText(getFileTitle(file));
        }
        if (holder.text != null) {
            if (holder.getItemViewType() == FILE) {
                holder.text.setText(getFileText(file));
            } else {
                holder.text.setVisibility(View.GONE);
            }
        }

        if (holder.image != null) {
            loadFileImage(file, holder);
        }
    }

    protected String getFileTitle(File file) {
        return file.getName();
    }

    protected String getFileText(File file) {
        return file.isDirectory() ? null : readableFileSize(file.length());
    }

    @SuppressWarnings("ConstantConditions")
    protected void loadFileImage(File file, final ViewHolder holder) {
        final int iconColor = ATHUtil.resolveColor(activity, R.attr.iconColor);
        if (file.isDirectory()) {
            holder.image.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
            holder.image.setImageResource(R.drawable.ic_folder_white_24dp);
        } else {
            Drawable error = Util.getTintedDrawable(activity, R.drawable.ic_music_note_white_24dp, iconColor);
            Glide.with(activity)
                    .load(new AudioFileCover(file.getPath()))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .error(error)
                    .placeholder(error)
                    .animate(android.R.anim.fade_in)
                    .signature(new MediaStoreSignature("", file.lastModified(), 0))
                    .into(holder.image);
        }
    }

    public static String readableFileSize(long size) {
        if (size <= 0) return size + " B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected File getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected String getName(File object) {
        return getFileTitle(object);
    }

    @Override
    protected void onMultipleItemAction(MenuItem menuItem, ArrayList<File> selection) {
        if (callbacks == null) return;
        switch (menuItem.getItemId()) {
            case R.id.action_add_to_current_playing:
                callbacks.onAddToCurrentPlaying(selection);
                break;
            case R.id.action_add_to_playlist:
                callbacks.onAddToPlaylist(selection);
                break;
            case R.id.action_delete_from_device:
                callbacks.onDeleteFromDevice(selection);
                break;
        }
    }

    public class ViewHolder extends MediaEntryViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
            if (menu != null && callbacks != null) {
                menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callbacks.onFileMenuClicked(dataSet.get(getAdapterPosition()));
                    }
                });
            }
        }

        @Override
        public void onClick(View v) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                if (callbacks != null) {
                    callbacks.onFileSelected(dataSet.get(getAdapterPosition()));
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            return toggleChecked(getAdapterPosition());
        }
    }

    public interface Callbacks {
        void onFileSelected(File file);

        void onFileMenuClicked(File file);

        void onAddToPlaylist(ArrayList<File> files);

        void onAddToCurrentPlaying(ArrayList<File> files);

        void onDeleteFromDevice(ArrayList<File> files);
    }
}