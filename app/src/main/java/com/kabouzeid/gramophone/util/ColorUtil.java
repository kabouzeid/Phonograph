package com.kabouzeid.gramophone.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;

import com.kabouzeid.gramophone.R;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ColorUtil {

    public static int generateColor(Context context, Bitmap bitmap) {
        return Palette.from(bitmap)
                .resizeBitmapSize(100)
                .generate()
                .getVibrantColor(ColorUtil.resolveColor(context, R.attr.default_bar_color));
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

    @NonNull
    public static ColorStateList getEmptyColorStateList(@ColorInt int color) {
        return new ColorStateList(
                new int[][]{
                        new int[]{}
                },
                new int[]{color}
        );
    }

    public static boolean useDarkTextColorOnBackground(@ColorInt int backgroundColor) {
        return (Color.red(backgroundColor) * 0.299 + Color.green(backgroundColor) * 0.587 + Color.blue(backgroundColor) * 0.114) > (255 / 2);
    }

    public static int getPrimaryTextColorForBackground(final Context context, @ColorInt int backgroundColor) {
        return useDarkTextColorOnBackground(backgroundColor) ? context.getResources().getColor(R.color.primary_text_default_material_light) : context.getResources().getColor(R.color.primary_text_default_material_dark);
    }

    public static int getSecondaryTextColorForBackground(final Context context, @ColorInt int backgroundColor) {
        return useDarkTextColorOnBackground(backgroundColor) ? context.getResources().getColor(R.color.secondary_text_default_material_light) : context.getResources().getColor(R.color.secondary_text_default_material_dark);
    }
}
