package com.kabouzeid.gramophone.appwidgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class BaseAppWidget extends AppWidgetProvider {

    protected static Bitmap createBitmap(Drawable drawable, float sizeMultiplier) {
        Bitmap bitmap = Bitmap.createBitmap((int) (drawable.getIntrinsicWidth() * sizeMultiplier), (int) (drawable.getIntrinsicHeight() * sizeMultiplier), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        drawable.setBounds(0, 0, c.getWidth(), c.getHeight());
        drawable.draw(c);
        return bitmap;
    }

    protected static Bitmap createRoundedBitmap(Drawable drawable, int width, int height, float tl, float tr, float bl, float br) {
        if (drawable == null) return null;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(c);

        Bitmap rounded = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(rounded);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(bitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        canvas.drawPath(composeRoundedRectPath(new RectF(0, 0, width, height), tl, tr, bl, br), paint);

        return rounded;
    }

    protected static Path composeRoundedRectPath(RectF rect, float tl, float tr, float bl, float br) {
        Path path = new Path();
        tl = tl < 0 ? 0 : tl;
        tr = tr < 0 ? 0 : tr;
        bl = bl < 0 ? 0 : bl;
        br = br < 0 ? 0 : br;

        path.moveTo(rect.left + tl, rect.top);
        path.lineTo(rect.right - tr, rect.top);
        path.quadTo(rect.right, rect.top, rect.right, rect.top + tr);
        path.lineTo(rect.right, rect.bottom - br);
        path.quadTo(rect.right, rect.bottom, rect.right - br, rect.bottom);
        path.lineTo(rect.left + bl, rect.bottom);
        path.quadTo(rect.left, rect.bottom, rect.left, rect.bottom - bl);
        path.lineTo(rect.left, rect.top + tl);
        path.quadTo(rect.left, rect.top, rect.left + tl, rect.top);
        path.close();

        return path;
    }

    protected PendingIntent buildPendingIntent(Context context, final String action, final ComponentName serviceName) {
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);
        return PendingIntent.getService(context, 0, intent, 0);
    }
}
