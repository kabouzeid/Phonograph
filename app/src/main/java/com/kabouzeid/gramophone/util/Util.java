package com.kabouzeid.gramophone.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.kabouzeid.gramophone.R;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Util {

//    public static int resolveDrawable(Context context, int drawable) {
//        TypedArray a = context.obtainStyledAttributes(new int[]{drawable});
//        int resId = a.getResourceId(0, 0);
//        a.recycle();
//        return resId;
//    }

    public static int resolveColor(@NonNull Context context, @AttrRes int colorAttr) {
        TypedArray a = context.obtainStyledAttributes(new int[]{colorAttr});
        int resId = a.getColor(0, 0);
        a.recycle();
        return resId;
    }

//    public static boolean isWindowTranslucent(Context context) {
//        TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.windowTranslucentStatus});
//        boolean result = a.getBoolean(0, false);
//        a.recycle();
//        return result;
//    }

    public static int getActionBarSize(@NonNull Context context) {
        TypedValue typedValue = new TypedValue();
        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = context.obtainStyledAttributes(typedValue.data, textSizeAttr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return actionBarSize;
    }

    public static int getStatusBarHeight(@NonNull Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getNavigationBarHeight(@NonNull Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

//    @TargetApi(19)
//    public static void setNavBarTranslucent(Window window, boolean translucent) {
//        if (translucent) {
//            window.setFlags(
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            return;
//        }
//
//        final WindowManager.LayoutParams attrs = window
//                .getAttributes();
//        attrs.flags &= (~WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        window.setAttributes(attrs);
//        window.clearFlags(
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//    }

    @TargetApi(19)
    public static void setStatusBarTranslucent(@NonNull Window window, boolean translucent) {
        if (translucent) {
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            return;
        }

        final WindowManager.LayoutParams attrs = window
                .getAttributes();
        attrs.flags &= (~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setAttributes(attrs);
        window.clearFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    public static void setAllowDrawUnderStatusBar(@NonNull Window window) {
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @NonNull
    public static String getFileSizeString(long sizeInBytes) {
        long fileSizeInKB = sizeInBytes / 1024;
        long fileSizeInMB = fileSizeInKB / 1024;
        return fileSizeInMB + " MB";
    }

//    public static String getFilePathFromContentProviderUri(Context context, Uri uri) {
//        String path = "";
//        String[] projection = {MediaStore.MediaColumns.DATA};
//        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
//        if (cursor == null) return null;
//        int column_index = cursor.getColumnIndexOrThrow(projection[0]);
//        if (cursor.moveToFirst()) {
//            path = cursor.getString(column_index);
//        }
//        cursor.close();
//        return path;
//    }
//
//    private static Bitmap getScaledBitmap(final Bitmap bitmap) {
//        int albumArtSize = 600;
//        return Bitmap.createScaledBitmap(bitmap, albumArtSize, albumArtSize, false);
//    }

    public static void hideSoftKeyboard(@Nullable Activity activity) {
        if (activity != null) {
            View currentFocus = activity.getCurrentFocus();
            if (currentFocus != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }

    public static boolean isAtLeastLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isAtLeastKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean isTablet(@NonNull final Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    public static boolean isInPortraitMode(@NonNull final Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public static Drawable getTintedDrawable(@NonNull Context context, @DrawableRes int drawableResId, int color) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
        if (drawable != null) {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
        return drawable;
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isRTL(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Configuration config = context.getResources().getConfiguration();
            return config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        } else return false;
    }

    public static Bitmap getResizedBitmap(@NonNull Bitmap bm, int newHeight, int newWidth, boolean recycleOld) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        if (recycleOld && resizedBitmap != bm) {
            bm.recycle();
        }
        return resizedBitmap;
    }
}