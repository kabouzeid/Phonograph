package com.kabouzeid.gramophone.adapter.base;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class MediaEntryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    @Nullable
    @Bind(R.id.image)
    public ImageView image;

    @Nullable
    @Bind(R.id.image_text)
    public TextView imageText;

    @Nullable
    @Bind(R.id.title)
    public TextView title;

    @Nullable
    @Bind(R.id.text)
    public TextView text;

    @Nullable
    @Bind(R.id.menu)
    public View menu;

    @Nullable
    @Bind(R.id.separator)
    public View separator;

    @Nullable
    @Bind(R.id.short_separator)
    public View shortSeparator;

    @Nullable
    @Bind(R.id.selected_indicator)
    public View selectedIndicator;

    @Nullable
    @Bind(R.id.drag_view)
    public View dragView;

    @Nullable
    @Bind(R.id.palette_color_container)
    public View paletteColorContainer;
    public int paletteColor;

    public MediaEntryViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    protected void setImageTransitionName(@NonNull String transitionName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && image != null) {
            image.setTransitionName(transitionName);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public void onClick(View v) {

    }
}
