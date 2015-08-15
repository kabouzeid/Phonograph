package com.kabouzeid.gramophone.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;

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

    @Nullable
    public static Bitmap getEmbeddedSongArt(File songFile, @NonNull Context context) {
        try {
            AudioFile audioFile = AudioFileIO.read(songFile);
            byte[] data = audioFile.getTag().getFirstArtwork().getBinaryData();
            if (data != null) {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(data, 0, data.length, options);
                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, context);
                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                return BitmapFactory.decodeByteArray(data, 0, data.length, options);
            }
        } catch (@NonNull CannotReadException | TagException | IOException | ReadOnlyFileException | InvalidAudioFrameException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int calculateInSampleSize(@NonNull BitmapFactory.Options options, @NonNull Context context) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int reqLength = Math.round(getSmallerScreenSize(context) * 1.5f); // absolute maximum size the album art will ever have

        // setting reqWidth matching to desired 1:1 ratio and screen-size
        if (width < height) {
            reqLength = (height / width) * reqLength;
        } else {
            reqLength = (width / height) * reqLength;
        }

        int inSampleSize = 1;

        if (height > reqLength || width > reqLength) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqLength
                    && (halfWidth / inSampleSize) > reqLength) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static int getSmallerScreenSize(@NonNull Context c) {
        Point size = Util.getScreenSize(c);
        return Math.min(size.x, size.y);
    }
}
