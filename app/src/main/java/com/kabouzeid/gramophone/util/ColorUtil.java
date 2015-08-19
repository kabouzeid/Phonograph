package com.kabouzeid.gramophone.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;

import com.kabouzeid.gramophone.R;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ColorUtil {
    public static final int PALETTE_BITMAP_SIZE = 100;

    public static int generateColor(Context context, Bitmap bitmap) {
        return getColor(context, generatePalette(bitmap));
    }

    public static Palette generatePalette(Bitmap bitmap) {
        return Palette.from(bitmap)
                .resizeBitmapSize(PALETTE_BITMAP_SIZE)
                .generate();
    }

    public static int getColor(Context context, @Nullable Palette palette) {
        if (palette != null) {
            if (palette.getVibrantSwatch() != null) {
                return palette.getVibrantSwatch().getRgb();
            } else if (palette.getMutedSwatch() != null) {
                return palette.getMutedSwatch().getRgb();
            } else if (palette.getDarkVibrantSwatch() != null) {
                return palette.getDarkVibrantSwatch().getRgb();
            } else if (palette.getDarkMutedSwatch() != null) {
                return palette.getDarkMutedSwatch().getRgb();
            } else if (palette.getLightVibrantSwatch() != null) {
                return palette.getLightVibrantSwatch().getRgb();
            } else if (palette.getLightMutedSwatch() != null) {
                return palette.getLightMutedSwatch().getRgb();
            }
        }
        return ColorUtil.resolveColor(context, R.attr.default_bar_color);
    }

    public static int resolveColor(@NonNull Context context, @AttrRes int colorAttr) {
        TypedArray a = context.obtainStyledAttributes(new int[]{colorAttr});
        int resId = a.getColor(0, 0);
        a.recycle();
        return resId;
    }

    public static int getOpaqueColor(@ColorInt int color) {
        return color | 0xFF000000;
    }

    public static int getColorWithAlpha(float alpha, @ColorInt int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        return a + rgb;
    }

    @SuppressWarnings("ResourceType")
    public static int shiftColorDown(@ColorInt int color) {
        int alpha = Color.alpha(color);
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f; // value component
        return (alpha << 24) + (0x00ffffff & Color.HSVToColor(hsv));
    }

    @SuppressWarnings("ResourceType")
    public static int shiftColorUp(@ColorInt int color) {
        int alpha = Color.alpha(color);
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 1.1f; // value component
        return (alpha << 24) + (0x00ffffff & Color.HSVToColor(hsv));
    }

    public static int getPrimaryTextColor(final Context context, boolean dark) {
        return dark ? ContextCompat.getColor(context, R.color.primary_text_default_material_light) : ContextCompat.getColor(context, R.color.primary_text_default_material_dark);
    }

    public static int getSecondaryTextColor(final Context context, boolean dark) {
        return dark ? ContextCompat.getColor(context, R.color.secondary_text_default_material_light) : ContextCompat.getColor(context, R.color.secondary_text_default_material_dark);
    }

    public static float getLuminance(@ColorInt int color) {
        return (Color.red(color) * 0.299f + Color.green(color) * 0.587f + Color.blue(color) * 0.114f);
    }

    public static boolean useDarkTextColorOnBackground(@ColorInt int backgroundColor) {
        return getLuminance(backgroundColor) > (255f / 2f);
    }

    public static boolean useDarkFabDrawableOnBackground(@ColorInt int backgroundColor) {
        return getLuminance(backgroundColor) > (255f / 1.3f);
    }

    public static int getPrimaryTextColorForBackground(final Context context, @ColorInt int backgroundColor) {
        return getPrimaryTextColor(context, useDarkTextColorOnBackground(backgroundColor));
    }

    public static int getSecondaryTextColorForBackground(final Context context, @ColorInt int backgroundColor) {
        return getSecondaryTextColor(context, useDarkTextColorOnBackground(backgroundColor));
    }

    public static int getFabDrawableColorForBackground(final Context context, @ColorInt int backgroundColor) {
        return getPrimaryTextColor(context, useDarkFabDrawableOnBackground(backgroundColor));
    }
}
