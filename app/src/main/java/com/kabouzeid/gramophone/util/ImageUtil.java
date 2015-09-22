package com.kabouzeid.gramophone.util;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ImageUtil {

    public static int calculateInSampleSize(int width, int height, int reqWidth) {
        // setting reqWidth matching to desired 1:1 ratio and screen-size
        if (width < height) {
            reqWidth = (height / width) * reqWidth;
        } else {
            reqWidth = (width / height) * reqWidth;
        }

        int inSampleSize = 1;

        if (height > reqWidth || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqWidth
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
