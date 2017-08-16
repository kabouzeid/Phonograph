package com.kabouzeid.gramophone.appwidgets.base;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.service.MusicService;

public abstract class BaseAppWidget extends AppWidgetProvider {
    public static final String NAME = "app_widget";

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
                         final int[] appWidgetIds) {
        defaultAppWidget(context, appWidgetIds);
        final Intent updateIntent = new Intent(MusicService.APP_WIDGET_UPDATE);
        updateIntent.putExtra(MusicService.EXTRA_APP_WIDGET_NAME, NAME);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcast(updateIntent);
    }

    /**
     * Handle a change notification coming over from
     * {@link MusicService}
     */
    public void notifyChange(final MusicService service, final String what) {
        if (hasInstances(service)) {
            if (MusicService.META_CHANGED.equals(what) || MusicService.PLAY_STATE_CHANGED.equals(what)) {
                performUpdate(service, null);
            }
        }
    }

    protected void pushUpdate(final Context context, final int[] appWidgetIds, final RemoteViews views) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (appWidgetIds != null) {
            appWidgetManager.updateAppWidget(appWidgetIds, views);
        } else {
            appWidgetManager.updateAppWidget(new ComponentName(context, getClass()), views);
        }
    }

    /**
     * Check against {@link AppWidgetManager} if there are any instances of this
     * widget.
     */
    protected boolean hasInstances(final Context context) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final int[] mAppWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                getClass()));
        return mAppWidgetIds.length > 0;
    }

    protected PendingIntent buildPendingIntent(Context context, final String action, final ComponentName serviceName) {
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);
        return PendingIntent.getService(context, 0, intent, 0);
    }

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

    abstract protected void defaultAppWidget(final Context context, final int[] appWidgetIds);

    abstract public void performUpdate(final MusicService service, final int[] appWidgetIds);

    protected Drawable getAlbumArtDrawable(final Resources resources, final Bitmap bitmap) {
        Drawable image;
        if (bitmap == null) {
            image = resources.getDrawable(R.drawable.default_album_art);
        } else {
            image = new BitmapDrawable(resources, bitmap);
        }
        return image;
    }

    protected String getSongArtistAndAlbum(final Song song) {
        final StringBuilder builder = new StringBuilder();
        builder.append(song.artistName);
        if (!TextUtils.isEmpty(song.artistName) && !TextUtils.isEmpty(song.albumName)) {
            builder.append(" • ");
        }
        builder.append(song.albumName);
        return builder.toString();
    }
}
