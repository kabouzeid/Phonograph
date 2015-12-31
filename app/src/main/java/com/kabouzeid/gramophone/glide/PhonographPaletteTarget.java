package com.kabouzeid.gramophone.glide;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.ColorUtil;

import hugo.weaving.DebugLog;

public abstract class PhonographPaletteTarget extends BitmapPaletteTarget {
    public PhonographPaletteTarget(ImageView view) {
        super(view);
    }

    @DebugLog
    @Override
    public void onLoadFailed(Exception e, Drawable errorDrawable) {
        super.onLoadFailed(e, errorDrawable);
        onColorReady(getDefaultBarColor());
    }

    @DebugLog
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
