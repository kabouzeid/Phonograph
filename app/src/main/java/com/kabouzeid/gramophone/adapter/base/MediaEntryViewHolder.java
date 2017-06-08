package com.kabouzeid.gramophone.adapter.base;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class MediaEntryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    @Nullable
    @BindView(R.id.song_view)
    public LinearLayout songView;

    @Nullable
    @BindView(R.id.image)
    public ImageView image;

    @Nullable
    @BindView(R.id.image_text)
    public TextView imageText;

    @Nullable
    @BindView(R.id.title)
    public TextView title;

    @Nullable
    @BindView(R.id.title_scrollview)
    public HorizontalScrollView title_scrollview;

    @Nullable
    @BindView(R.id.text)
    public TextView text;

    @Nullable
    @BindView(R.id.menu)
    public View menu;

    @Nullable
    @BindView(R.id.separator)
    public View separator;

    @Nullable
    @BindView(R.id.short_separator)
    public View shortSeparator;

    @Nullable
    @BindView(R.id.drag_view)
    public View dragView;

    @Nullable
    @BindView(R.id.palette_color_container)
    public View paletteColorContainer;

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

    /**
     * Forces a ripple animation in a view at a specified set of coordinates
     * @param view the view where the ripple will be contained
     * @param x the x coordinate of where the ripple should begin
     * @param y the y coordinate where the ripple should begin
     */
    public void forceRippleAnimation(View view, int x , int y)
    {
        Drawable background = view.getBackground();

        if(Build.VERSION.SDK_INT >= 21 && background instanceof RippleDrawable)
        {
            final RippleDrawable rippleDrawable = (RippleDrawable) background;

            rippleDrawable.setHotspot(x,y);

            rippleDrawable.setState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled});

            rippleDrawable.setState(new int[]{});

        }
    }
}
