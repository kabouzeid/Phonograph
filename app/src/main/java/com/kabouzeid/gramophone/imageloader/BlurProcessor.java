package com.kabouzeid.gramophone.imageloader;

import android.graphics.Bitmap;

import com.kabouzeid.gramophone.helper.bitmapblur.StackBlurManager;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class BlurProcessor implements BitmapProcessor {

    final int blurRadius;

    public BlurProcessor(int blurRadius) {
        this.blurRadius = blurRadius;
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        return new StackBlurManager(bitmap).process(blurRadius);
    }
}
