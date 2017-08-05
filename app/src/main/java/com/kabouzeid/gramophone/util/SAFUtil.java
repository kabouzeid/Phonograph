package com.kabouzeid.gramophone.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.kabouzeid.gramophone.R;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.generic.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SAFUtil {

    public static final String TAG = SAFUtil.class.getSimpleName();
    public static final String SEPARATOR = "###/SAF/###";

    public static final int REQUEST_SAF_PICK_FILE = 42;
    public static final int REQUEST_SAF_PICK_TREE = 43;

    public static boolean isSAFRequired(File file) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !file.canWrite();
    }

    public static boolean isSAFRequired(String path) {
        return isSAFRequired(new File(path));
    }

    public static boolean isSAFRequired(AudioFile audio) {
        return isSAFRequired(audio.getFile());
    }

    public static boolean isSAFRequired(List<String> paths) {
        for (String path : paths) {
            if (isSAFRequired(path)) return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void openFilePicker(Activity activity) {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("audio/*");
        i.putExtra("android.content.extra.SHOW_ADVANCED", true);
        activity.startActivityForResult(i, SAFUtil.REQUEST_SAF_PICK_FILE);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void openTreePicker(Activity activity) {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.putExtra("android.content.extra.SHOW_ADVANCED", true);
        activity.startActivityForResult(i, SAFUtil.REQUEST_SAF_PICK_TREE);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void saveTreeUri(Context context, Intent data) {
        Uri uri = data.getData();
        context.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PreferenceUtil.getInstance(context).setSAFSDCardUri(uri);
    }

    public static boolean isTreeUriSaved(Context context) {
        return !TextUtils.isEmpty(PreferenceUtil.getInstance(context).getSAFSDCardUri());
    }

    /**
     * https://github.com/vanilla-music/vanilla-music-tag-editor/commit/e00e87fef289f463b6682674aa54be834179ccf0#diff-d436417358d5dfbb06846746d43c47a5R359
     * Finds needed file through Document API for SAF. It's not optimized yet - you can still gain wrong URI on
     * files such as "/a/b/c.mp3" and "/b/a/c.mp3", but I consider it complete enough to be usable.
     *
     * @param dir      - document file representing current dir of search
     * @param segments - path segments that are left to find
     * @return URI for found file. Null if nothing found.
     */
    @Nullable
    public static Uri findDocument(DocumentFile dir, List<String> segments) {
        for (DocumentFile file : dir.listFiles()) {
            int index = segments.indexOf(file.getName());
            if (index == -1) {
                continue;
            }

            if (file.isDirectory()) {
                segments.remove(file.getName());
                return findDocument(file, segments);
            }

            if (file.isFile() && index == segments.size() - 1) {
                // got to the last part
                return file.getUri();
            }
        }

        return null;
    }

    public static void write(Activity activity, AudioFile audio, Uri safUri) {
        if (isSAFRequired(audio)) {
            writeSAF(activity, audio, safUri);
        } else {
            try {
                writeFile(audio);
            } catch (CannotWriteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeFile(AudioFile audio) throws CannotWriteException {
        audio.commit();
    }

    public static void writeSAF(final Activity activity, AudioFile audio, Uri safUri) {
        Uri uri = null;

        if (isTreeUriSaved(activity)) {
            List<String> pathSegments = new ArrayList<>(Arrays.asList(audio.getFile().getAbsolutePath().split("/")));
            Uri sdcard = Uri.parse(PreferenceUtil.getInstance(activity).getSAFSDCardUri());
            uri = findDocument(DocumentFile.fromTreeUri(activity, sdcard), pathSegments);
        }

        if (uri == null) {
            uri = safUri;
        }

        if (uri == null) {
            Log.e(TAG, "Can't get SAF URI");
            Toast.makeText(activity, R.string.saf_error_uri, Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // copy file to app folder to use jaudiotagger
            final File original = audio.getFile();
            File temp = File.createTempFile("tmp-media", '.' + Utils.getExtension(original));
            Utils.copy(original, temp);
            temp.deleteOnExit();
            audio.setFile(temp);
            writeFile(audio);

            ParcelFileDescriptor pfd = activity.getContentResolver().openFileDescriptor(uri, "rw");
            if (pfd == null) {
                Log.e(TAG, "SAF provided incorrect URI: " + uri);
                return;
            }

            // now read persisted data and write it to real FD provided by SAF
            FileInputStream fis = new FileInputStream(temp);
            byte[] audioContent = FileUtil.readBytes(fis);
            FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());
            fos.write(audioContent);
            fos.close();

            temp.delete();

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, String.format(activity.getString(R.string.saf_write_success), original.getAbsolutePath()), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (final Exception e) {
            Log.e(TAG, "Failed to write to file descriptor provided by SAF", e);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, String.format(activity.getString(R.string.saf_write_failed), e.getLocalizedMessage()), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

}
