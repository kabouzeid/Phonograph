package com.kabouzeid.gramophone.adapter.base;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class MediaEntryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    @Nullable
    @Optional
    @InjectView(R.id.image)
    public ImageView image;
    @Nullable
    @Optional
    @InjectView(R.id.image_text)
    public TextView imageText;

    @Nullable
    @Optional
    @InjectView(R.id.title)
    public TextView title;
    @Nullable
    @Optional
    @InjectView(R.id.text)
    public TextView text;

    @Nullable
    @Optional
    @InjectView(R.id.menu)
    public View menu;

    @Nullable
    @Optional
    @InjectView(R.id.palette_color_container)
    public View paletteColorContainer;

    @Nullable
    @Optional
    @InjectView(R.id.separator)
    public View separator;
    @Nullable
    @Optional
    @InjectView(R.id.short_separator)
    public View shortSeparator;

    @Nullable
    @Optional
    @InjectView(R.id.selected_indicator)
    public View selectedIndicator;

    public MediaEntryViewHolder(View itemView) {
        super(itemView);
        ButterKnife.inject(this, itemView);

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
