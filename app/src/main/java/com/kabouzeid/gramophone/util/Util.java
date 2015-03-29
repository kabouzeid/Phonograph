package com.kabouzeid.gramophone.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.kabouzeid.gramophone.R;

/**
 * Created by karim on 12.12.14.
 */
public class Util {
    private static int albumArtSize = 600;

    public static int resolveDrawable(Context context, int drawable) {
        TypedArray a = context.obtainStyledAttributes(new int[]{drawable});
        int resId = a.getResourceId(0, 0);
        a.recycle();
        return resId;
    }

    public static int resolveColor(Context context, int color) {
        TypedArray a = context.obtainStyledAttributes(new int[]{color});
        int resId = a.getColor(0, 0);
        a.recycle();
        return resId;
    }

    public static boolean isWindowTranslucent(Context context) {
        TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.windowTranslucentStatus});
        boolean result = a.getBoolean(0, false);
        a.recycle();
        return result;
    }

    public static int getActionBarSize(Context context) {
        TypedValue typedValue = new TypedValue();
        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = context.obtainStyledAttributes(typedValue.data, textSizeAttr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return actionBarSize;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @TargetApi(19)
    public static void setNavBarTranslucent(Window window, boolean translucent) {
        if (translucent) {
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            return;
        }

        final WindowManager.LayoutParams attrs = window
                .getAttributes();
        attrs.flags &= (~WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.setAttributes(attrs);
        window.clearFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    @TargetApi(19)
    public static void setStatusBarTranslucent(Window window, boolean translucent) {
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

    public static final boolean isOnline(final Context context) {
        if (context == null) {
            return false;
        }

        boolean state = false;
        final boolean onlyOnWifi = PreferenceUtils.getInstance(context).autoDownloadOnlyOnWifi();

        /* Monitor network connections */
        final ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        /* Wi-Fi connection */
        final NetworkInfo wifiNetwork = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null) {
            state = wifiNetwork.isConnectedOrConnecting();
        }

        /* Mobile data connection */
        final NetworkInfo mbobileNetwork = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mbobileNetwork != null) {
            if (!onlyOnWifi) {
                state = mbobileNetwork.isConnectedOrConnecting();
            }
        }

        /* Other networks */
        final NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (!onlyOnWifi) {
                state = activeNetwork.isConnectedOrConnecting();
            }
        }

        return state;
    }

    public static String getFileSizeString(long sizeInBytes) {
        long fileSizeInKB = sizeInBytes / 1024;
        long fileSizeInMB = fileSizeInKB / 1024;
        return fileSizeInMB + " MB";
    }

    public static String getFilePathFromContentProviderUri(Context context, Uri uri) {
        String path = "";
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(projection[0]);
        if (cursor.moveToFirst()) {
            path = cursor.getString(column_index);
        }
        cursor.close();
        return path;
    }

    private static Bitmap getScaledBitmap(final Bitmap bitmap) {
        return Bitmap.createScaledBitmap(bitmap, albumArtSize, albumArtSize, false);
    }

    public static void hideSoftKeyboard(Activity activity) {
        if (activity != null) {
            View currentFocus = activity.getCurrentFocus();
            if (currentFocus != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }

    public static boolean hasLollipopSDK() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean hasKitKatSDK() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean isTablet(final Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    public static boolean isInPortraitMode(final Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public static Drawable getTintedDrawable(Resources res, @DrawableRes int drawableResId, int color) {
        Drawable drawable = res.getDrawable(drawableResId);
        if (drawable != null) {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
        return drawable;
    }
}
