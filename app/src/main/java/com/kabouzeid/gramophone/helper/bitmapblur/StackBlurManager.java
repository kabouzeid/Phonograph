/**
 * StackBlur v1.0 for Android
 *
 * @Author: Enrique López Mañas <eenriquelopez@gmail.com>
 * http://www.lopez-manas.com
 * <p/>
 * Author of the original algorithm: Mario Klingemann <mario.quasimondo.com>
 * <p/>
 * This is a compromise between Gaussian Blur and Box blur
 * It creates much better looking blurs than Box Blur, but is
 * 7x faster than my Gaussian Blur implementation.
 * <p/>
 * I called it Stack Blur because this describes best how this
 * filter works internally: it creates a kind of moving stack
 * of colors whilst scanning through the image. Thereby it
 * just has to add one new block of color to the right side
 * of the stack and remove the leftmost color. The remaining
 * colors on the topmost layer of the stack are either added on
 * or reduced by one, depending on if they are on the right or
 * on the left side of the stack.
 * @copyright: Enrique López Mañas
 * @license: Apache License 2.0
 */


package com.kabouzeid.gramophone.helper.bitmapblur;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kabouzeid.gramophone.util.Util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// class modified by Karim Abou Zeid (kabouzeid)

public class StackBlurManager {
    static final int EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors();
    static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(EXECUTOR_THREADS);

    /**
     * Resized original image
     */
    private final Bitmap _image;

    /**
     * Most recent result of blurring
     */
    @Nullable
    private Bitmap _result;

    /**
     * Method of blurring
     */
    @NonNull
    private final BlurProcess _blurProcess;

    /**
     * Constructor method (basic initialization and construction of the pixel array)
     *
     * @param image The image that will be analysed
     */
    public StackBlurManager(@NonNull Bitmap image) {
        _image = Util.getResizedBitmap(image, 500, 500, false);
        _blurProcess = new JavaBlurProcess();
    }

    /**
     * Process the image on the given radius. Radius must be at least 1
     *
     * @param radius
     */
    @Nullable
    public Bitmap process(int radius) {
        _result = _blurProcess.blur(_image, radius);
        return _result;
    }

    /**
     * Returns the blurred image as a bitmap
     *
     * @return blurred image
     */
    @Nullable
    public Bitmap returnBlurredImage() {
        return _result;
    }
}
