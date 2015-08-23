package com.kabouzeid.gramophone.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.annotation.NonNull;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ImageUtil {

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
