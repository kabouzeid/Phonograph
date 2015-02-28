package com.kabouzeid.materialmusic.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by karim on 22.12.14.
 */
public final class InternalStorageUtil {
    private static final String TAG = InternalStorageUtil.class.getSimpleName();

    public static synchronized void writeObject(final Context context, final String key, final Object object) throws IOException {
        try {
            String tempFileName = "TEMP_" + key;
            FileOutputStream fos;
            fos = context.openFileOutput(tempFileName, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
            fos.close();
            renameAppFile(context, tempFileName, key);
        } catch (IOException e) {
            Log.e(TAG, "Writing Object to internal storage failed! Maybe the Object is not serializable?", e);
        }
    }

    public static synchronized void renameAppFile(final Context context, String originalFileName, String newFileName) {
        File originalFile = context.getFileStreamPath(originalFileName);
        File newFile = new File(originalFile.getParent(), newFileName);
        if (newFile.exists()) {
            context.deleteFile(newFileName);
        }
        //noinspection ResultOfMethodCallIgnored
        originalFile.renameTo(newFile);
    }

    public static synchronized Object readObject(final Context context, String key) throws IOException,
            ClassNotFoundException {
        FileInputStream fis = context.openFileInput(key);
        ObjectInputStream ois = new ObjectInputStream(fis);
        return ois.readObject();
    }
}
