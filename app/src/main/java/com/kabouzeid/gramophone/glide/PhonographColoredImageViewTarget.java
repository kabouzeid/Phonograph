package com.kabouzeid.gramophone.glide;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.glide.palette.BitmapPaletteImageViewTarget;
import com.kabouzeid.gramophone.glide.palette.BitmapPaletteWrapper;
import com.kabouzeid.gramophone.util.ColorUtil;

public abstract class PhonographColoredImageViewTarget extends BitmapPaletteImageViewTarget {
    public PhonographColoredImageViewTarget(ImageView view) {
        super(view);
    }

    @Override
    public void onLoadFailed(Exception e, Drawable errorDrawable) {
        super.onLoadFailed(e, errorDrawable);
        onColorReady(getDefaultBarColor());
    }

    @Override
    public void onResourceReady(BitmapPaletteWrapper resource, GlideAnimation<? super BitmapPaletteWrapper> glideAnimation) {
        super.onResourceReady(resource, glideAnimation);
        onColorReady(ColorUtil.getColor(resource.getPalette(), getDefaultBarColor()));
    }

    private int getDefaultBarColor() {
        return ColorUtil.resolveColor(getView().getContext(), R.attr.default_bar_color);
    }

    public abstract void onColorReady(int color);
}
