package com.kabouzeid.gramophone.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ColorUtil {

    public static int resolveColor(@NonNull Context context, @AttrRes int colorAttr) {
        TypedArray a = context.obtainStyledAttributes(new int[]{colorAttr});
        int resId = a.getColor(0, 0);
        a.recycle();
        return resId;
    }

    public static int getOpaqueColor(@ColorInt int color) {
        return color | 0xFF000000;
    }

    public static int getColorWithAlpha(float alpha, int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        return a + rgb;
    }

    @SuppressWarnings("ResourceType")
    public static int shiftColorDown(int color) {
        int alpha = Color.alpha(color);
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f; // value component
        return (alpha << 24) + (0x00ffffff & Color.HSVToColor(hsv));
    }

    @NonNull
    public static ColorStateList getEmptyColorStateList(int color) {
        return new ColorStateList(
                new int[][]{
                        new int[]{}
                },
                new int[]{color}
        );
    }

    public static boolean useDarkTextColorOnBackground(int backgroundColor) {
        return (Color.red(backgroundColor) * 0.299 + Color.green(backgroundColor) * 0.587 + Color.blue(backgroundColor) * 0.114) > 186;
    }

    public static int getTextColorForBackground(int backgroundColor) {
        return (Color.red(backgroundColor) * 0.299 + Color.green(backgroundColor) * 0.587 + Color.blue(backgroundColor) * 0.114) > 186 ? Color.BLACK : Color.WHITE;
    }
}
