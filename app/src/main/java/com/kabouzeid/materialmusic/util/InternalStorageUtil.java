package com.kabouzeid.materialmusic.util;

import android.content.Context;
import android.util.Log;

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
            FileOutputStream fos;
            fos = context.openFileOutput(key, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Writing Object to internal storage failed! Maybe the Object is not serializable?", e);
        }
    }

    public static synchronized Object readObject(Context context, String key) throws IOException,
            ClassNotFoundException {
        FileInputStream fis = context.openFileInput(key);
        ObjectInputStream ois = new ObjectInputStream(fis);
        return ois.readObject();
    }
}
