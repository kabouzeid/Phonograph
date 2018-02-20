package com.poupa.vinylmusicplayer.appshortcuts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.graphics.drawable.IconCompat;
import android.util.TypedValue;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.Util;

/**
 * @author Adrian Campos
 */
@RequiresApi(Build.VERSION_CODES.N_MR1)
public final class AppShortcutIconGenerator {
    public static Icon generateThemedIcon(Context context, int iconId) {
        if (PreferenceUtil.getInstance().coloredAppShortcuts()) {
            return generateUserThemedIcon(context, iconId).toIcon();
        } else {
            return generateDefaultThemedIcon(context, iconId).toIcon();
        }
    }

    private static IconCompat generateDefaultThemedIcon(Context context, int iconId) {
        // Return an Icon of iconId with default colors
        return generateThemedIcon(context, iconId,
                context.getColor(R.color.app_shortcut_default_foreground),
                context.getColor(R.color.app_shortcut_default_background)
        );
    }

    private static IconCompat generateUserThemedIcon(Context context, int iconId) {
        // Get background color from context's theme
        final TypedValue typedColorBackground = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorBackground, typedColorBackground, true);

        // Return an Icon of iconId with those colors
        return generateThemedIcon(context, iconId,
                ThemeStore.primaryColor(context),
                typedColorBackground.data
        );
    }

    private static IconCompat generateThemedIcon(Context context, int iconId, int foregroundColor, int backgroundColor) {
        // Get and tint foreground and background drawables
        Drawable vectorDrawable = Util.getTintedVectorDrawable(context, iconId, foregroundColor);
        Drawable backgroundDrawable = Util.getTintedVectorDrawable(context, R.drawable.ic_app_shortcut_background, backgroundColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AdaptiveIconDrawable adaptiveIconDrawable = new AdaptiveIconDrawable(backgroundDrawable, vectorDrawable);
            return IconCompat.createWithAdaptiveBitmap(drawableToBitmap(adaptiveIconDrawable));
        } else {
            // Squash the two drawables together
            LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{backgroundDrawable, vectorDrawable});

            // Return as an Icon
            return IconCompat.createWithBitmap(drawableToBitmap(layerDrawable));
        }
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
