package com.kabouzeid.gramophone.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Karim Abou Zeid (kabouzeid)
 *         <p/>
 *         A simple helper class for Android to read and write
 *         any serializeable object to the internal storage
 */
public final class InternalStorageUtil {

    /**
     * @param context a valid {@link Context}
     * @param key     the filename
     * @param object  any {@link java.io.Serializable} object which will be written to the internal storage
     */
    public static synchronized void writeObject(final Context context, final String key, final Object object) throws IOException {
        // First write the object to a file with ".tmp" postfix,
        // so when an error occurs, we do not overwrite the original
        // file (if exists) with a corrupted file.
        String tempFileName = key + ".tmp";
        FileOutputStream fos;
        fos = context.openFileOutput(tempFileName, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.close();
        fos.close();
        // after writing was successful we overwrite the original
        // file (if exists) with the new file
        renameAppFile(context, tempFileName, key);
    }

    /**
     * @param context          a valid {@link Context}
     * @param originalFileName the original filename
     * @param newFileName      the new filename
     */
    public static synchronized void renameAppFile(final Context context, String originalFileName, String newFileName) {
        File originalFile = context.getFileStreamPath(originalFileName);
        File newFile = new File(originalFile.getParent(), newFileName);
        if (newFile.exists()) {
            context.deleteFile(newFileName);
        }
        //noinspection ResultOfMethodCallIgnored
        originalFile.renameTo(newFile);
    }

    /**
     * @param context a valid {@link Context}
     * @param key     the filename
     */
    public static synchronized Object readObject(final Context context, String key) throws IOException,
            ClassNotFoundException {
        FileInputStream fis = context.openFileInput(key);
        ObjectInputStream ois = new ObjectInputStream(fis);
        return ois.readObject();
    }
}
