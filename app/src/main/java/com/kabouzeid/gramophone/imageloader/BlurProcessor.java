package com.kabouzeid.gramophone.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RSRuntimeException;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

import com.kabouzeid.gramophone.BuildConfig;
import com.kabouzeid.gramophone.helper.StackBlur;
import com.kabouzeid.gramophone.util.ImageUtil;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class BlurProcessor implements BitmapProcessor {
    public static final float DEFAULT_BLUR_RADIUS = 5f;

    private Context context;
    private final float blurRadius;
    private final int sampling;

    private BlurProcessor(Builder builder) {
        this.context = builder.context;
        this.blurRadius = builder.blurRadius;
        this.sampling = builder.sampling;
    }

    // Something here seems to cause a memory leak... Go into LeakCanary for more details.
    @Override
    public Bitmap process(Bitmap bitmap) {
        int sampling;
        if (this.sampling == 0) {
            sampling = ImageUtil.calculateInSampleSize(bitmap.getWidth(), bitmap.getHeight(), 100);
        } else {
            sampling = this.sampling;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int scaledWidth = width / sampling;
        int scaledHeight = height / sampling;

        Bitmap out = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(out);
        canvas.scale(1 / (float) sampling, 1 / (float) sampling);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);

        if (Build.VERSION.SDK_INT > 16) {
            try {
                final RenderScript rs = RenderScript.create(context.getApplicationContext());
                final Allocation input = Allocation.createFromBitmap(rs, out, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
                final Allocation output = Allocation.createTyped(rs, input.getType());
                final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

                script.setRadius(blurRadius);
                script.setInput(input);
                script.forEach(output);

                output.copyTo(out);

                rs.destroy();

                return out;

            } catch (RSRuntimeException e) {
                // on some devices RenderScript.create() throws: android.support.v8.renderscript.RSRuntimeException: Error loading libRSSupport library
                if (BuildConfig.DEBUG) e.printStackTrace();
            }
        }

        return StackBlur.blur(out, blurRadius);
    }

    public static class Builder {
        private Context context;
        private float blurRadius = DEFAULT_BLUR_RADIUS;
        private int sampling;

        public Builder(@NonNull Context context) {
            this.context = context;
        }

        /**
         * @param blurRadius The radius to use. Must be between 0 and 25. Default is 5.
         * @return the same Builder
         */
        public Builder blurRadius(@FloatRange(from = 0.0f, to = 25.0f) float blurRadius) {
            this.blurRadius = blurRadius;
            return this;
        }

        /**
         * @param sampling The inSampleSize to use. Must be a power of 2, or 1 for no down sampling or 0 for auto detect sampling. Default is 0.
         * @return the same Builder
         */
        public Builder sampling(int sampling) {
            this.sampling = sampling;
            return this;
        }

        public Builder fromPrototype(BlurProcessor prototype) {
            context = prototype.context;
            blurRadius = prototype.blurRadius;
            sampling = prototype.sampling;
            return this;
        }

        public BlurProcessor build() {
            return new BlurProcessor(this);
        }
    }
}
