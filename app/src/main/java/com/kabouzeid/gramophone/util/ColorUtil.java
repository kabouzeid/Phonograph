package com.kabouzeid.gramophone.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
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

    @Deprecated
    @ColorInt
    public static int generateColor(Context context, Bitmap bitmap) {
        return getColor(context, generatePalette(bitmap));
    }

    public static Palette generatePalette(Bitmap bitmap) {
        return Palette.from(bitmap)
                .resizeBitmapSize(PALETTE_BITMAP_SIZE)
                .generate();
    }

    @Deprecated
    @ColorInt
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

    @ColorInt
    public static int getColor(@Nullable Palette palette, int fallback) {
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
        return fallback;
    }

    @ColorInt
    public static int resolveColor(@NonNull Context context, @AttrRes int colorAttr) {
        TypedArray a = context.obtainStyledAttributes(new int[]{colorAttr});
        int resId = a.getColor(0, 0);
        a.recycle();
        return resId;
    }

    @ColorInt
    public static int getOpaqueColor(@ColorInt int color) {
        return color | 0xFF000000;
    }

    @ColorInt
    public static int getColorWithAlpha(float alpha, @ColorInt int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        return a + rgb;
    }

    @ColorInt
    public static int shiftColor(@ColorInt int color, @FloatRange(from = 0.0f, to = 2.0f) float by) {
        if (by == 1f) return color;
        int alpha = Color.alpha(color);
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= by; // value component
        // don't use  getColorWithAlpha(float alpha, int baseColor) here
        return (alpha << 24) + (0x00ffffff & Color.HSVToColor(hsv));
    }

    @SuppressWarnings("ResourceType")
    @ColorInt
    public static int shiftColorDown(@ColorInt int color) {
        return shiftColor(color, 0.9f);
    }

    @SuppressWarnings("ResourceType")
    @ColorInt
    public static int shiftColorUp(@ColorInt int color) {
        return shiftColor(color, 1.1f);
    }

    @ColorInt
    public static int getPrimaryTextColor(final Context context, boolean dark) {
        return dark ? ContextCompat.getColor(context, R.color.primary_text_default_material_light) : ContextCompat.getColor(context, R.color.primary_text_default_material_dark);
    }

    @ColorInt
    public static int getSecondaryTextColor(final Context context, boolean dark) {
        return dark ? ContextCompat.getColor(context, R.color.secondary_text_default_material_light) : ContextCompat.getColor(context, R.color.secondary_text_default_material_dark);
    }

    @ColorInt
    public static int getPrimaryDisabledTextColor(final Context context, boolean dark) {
        return dark ? ContextCompat.getColor(context, R.color.primary_text_disabled_material_light) : ContextCompat.getColor(context, R.color.primary_text_disabled_material_dark);
    }

    @ColorInt
    public static int getSecondaryDisabledTextColor(final Context context, boolean dark) {
        return dark ? ContextCompat.getColor(context, R.color.secondary_text_disabled_material_light) : ContextCompat.getColor(context, R.color.secondary_text_disabled_material_dark);
    }

    public static float getLuminance(@ColorInt int color) {
        return (Color.red(color) * 0.299f + Color.green(color) * 0.587f + Color.blue(color) * 0.114f);
    }

    public static boolean useDarkTextColorOnBackground(@ColorInt int backgroundColor) {
        return getLuminance(backgroundColor) > (255f / 1.5f);
    }

    @ColorInt
    public static int getPrimaryTextColorForBackground(final Context context, @ColorInt int backgroundColor) {
        return getPrimaryTextColor(context, useDarkTextColorOnBackground(backgroundColor));
    }

    @ColorInt
    public static int getSecondaryTextColorForBackground(final Context context, @ColorInt int backgroundColor) {
        return getSecondaryTextColor(context, useDarkTextColorOnBackground(backgroundColor));
    }

    @ColorInt
    public static int getPrimaryDisabledTextColorForBackground(final Context context, @ColorInt int backgroundColor) {
        return getPrimaryDisabledTextColor(context, useDarkTextColorOnBackground(backgroundColor));
    }

    @ColorInt
    public static int getSecondaryDisabledTextColorForBackground(final Context context, @ColorInt int backgroundColor) {
        return getSecondaryDisabledTextColor(context, useDarkTextColorOnBackground(backgroundColor));
    }

    @ColorInt
    public static int shiftBackgroundColorForLightText(@ColorInt int backgroundColor) {
        while (ColorUtil.useDarkTextColorOnBackground(backgroundColor)) {
            backgroundColor = ColorUtil.shiftColorDown(backgroundColor);
        }
        return backgroundColor;
    }

    @ColorInt
    public static int shiftBackgroundColorForDarkText(@ColorInt int backgroundColor) {
        while (!ColorUtil.useDarkTextColorOnBackground(backgroundColor)) {
            backgroundColor = ColorUtil.shiftColorUp(backgroundColor);
        }
        return backgroundColor;
    }

    public static int[] rgb2lab(int R, int G, int B) {
        //http://www.brucelindbloom.com

        float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
        float Ls, as, bs;
        float eps = 216.f / 24389.f;
        float k = 24389.f / 27.f;

        float Xr = 0.964221f;  // reference white D50
        float Yr = 1.0f;
        float Zr = 0.825211f;

        // RGB to XYZ
        r = R / 255.f; //R 0..1
        g = G / 255.f; //G 0..1
        b = B / 255.f; //B 0..1

        // assuming sRGB (D65)
        if (r <= 0.04045)
            r = r / 12;
        else
            r = (float) Math.pow((r + 0.055) / 1.055, 2.4);

        if (g <= 0.04045)
            g = g / 12;
        else
            g = (float) Math.pow((g + 0.055) / 1.055, 2.4);

        if (b <= 0.04045)
            b = b / 12;
        else
            b = (float) Math.pow((b + 0.055) / 1.055, 2.4);


        X = 0.436052025f * r + 0.385081593f * g + 0.143087414f * b;
        Y = 0.222491598f * r + 0.71688606f * g + 0.060621486f * b;
        Z = 0.013929122f * r + 0.097097002f * g + 0.71418547f * b;

        // XYZ to Lab
        xr = X / Xr;
        yr = Y / Yr;
        zr = Z / Zr;

        if (xr > eps)
            fx = (float) Math.pow(xr, 1 / 3.);
        else
            fx = (float) ((k * xr + 16.) / 116.);

        if (yr > eps)
            fy = (float) Math.pow(yr, 1 / 3.);
        else
            fy = (float) ((k * yr + 16.) / 116.);

        if (zr > eps)
            fz = (float) Math.pow(zr, 1 / 3.);
        else
            fz = (float) ((k * zr + 16.) / 116);

        Ls = (116 * fy) - 16;
        as = 500 * (fx - fy);
        bs = 200 * (fy - fz);

        int[] lab = new int[3];
        lab[0] = (int) (2.55 * Ls + .5);
        lab[1] = (int) (as + .5);
        lab[2] = (int) (bs + .5);
        return lab;
    }

    /**
     * Computes the difference between two RGB colors by converting them to the L*a*b scale and
     * comparing them using the CIE76 algorithm { http://en.wikipedia.org/wiki/Color_difference#CIE76}
     *
     * @return > 23 corresponds to a JND (just noticeable difference)
     */
    public static double getColorDifference(int a, int b) {
        int r1, g1, b1, r2, g2, b2;
        r1 = Color.red(a);
        g1 = Color.green(a);
        b1 = Color.blue(a);
        r2 = Color.red(b);
        g2 = Color.green(b);
        b2 = Color.blue(b);
        int[] lab1 = rgb2lab(r1, g1, b1);
        int[] lab2 = rgb2lab(r2, g2, b2);
        return Math.sqrt(Math.pow(lab2[0] - lab1[0], 2) + Math.pow(lab2[1] - lab1[1], 2) + Math.pow(lab2[2] - lab1[2], 2));
    }
}
