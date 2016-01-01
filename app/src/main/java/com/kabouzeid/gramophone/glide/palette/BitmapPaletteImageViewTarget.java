package com.kabouzeid.gramophone.glide.palette;

import android.widget.ImageView;

import com.bumptech.glide.request.target.ImageViewTarget;

public class BitmapPaletteImageViewTarget extends ImageViewTarget<BitmapPaletteWrapper> {
    public BitmapPaletteImageViewTarget(ImageView view) {
        super(view);
    }

    @Override
    protected void setResource(BitmapPaletteWrapper bitmapPaletteWrapper) {
        view.setImageBitmap(bitmapPaletteWrapper.getBitmap());
    }
}
