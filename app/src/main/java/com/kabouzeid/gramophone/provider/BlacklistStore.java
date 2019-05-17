package com.kabouzeid.gramophone.provider;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import androidx.annotation.NonNull;

import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.util.FileUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BlacklistStore extends SQLiteOpenHelper {
    private static BlacklistStore sInstance = null;
    public static final String DATABASE_NAME = "blacklist.db";
    private static final int VERSION = 1;
    private Context context;

    public BlacklistStore(final Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(@NonNull final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + BlacklistStoreColumns.NAME + " ("
                + BlacklistStoreColumns.PATH + " STRING NOT NULL);");
    }

    @Override
    public void onUpgrade(@NonNull final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BlacklistStoreColumns.NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BlacklistStoreColumns.NAME);
        onCreate(db);
    }

    @NonNull
    public static synchronized BlacklistStore getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new BlacklistStore(context.getApplicationContext());
            if (!PreferenceUtil.getInstance(context).initializedBlacklist()) {
                // blacklisted by default
                sInstance.addPathImpl(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS));
                sInstance.addPathImpl(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS));
                sInstance.addPathImpl(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES));

                PreferenceUtil.getInstance(context).setInitializedBlacklist();
            }
        }
        return sInstance;
    }

    public void addPath(File file) {
        addPathImpl(file);
        notifyMediaStoreChanged();
    }

    private void addPathImpl(File file) {
        if (file == null || contains(file)) {
            return;
        }
        String path = FileUtil.safeGetCanonicalPath(file);

        final SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();

        try {
            // add the entry
            final ContentValues values = new ContentValues(1);
            values.put(BlacklistStoreColumns.PATH, path);
            database.insert(BlacklistStoreColumns.NAME, null, values);

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public boolean contains(File file) {
        if (file == null) {
            return false;
        }
        String path = FileUtil.safeGetCanonicalPath(file);

        final SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(BlacklistStoreColumns.NAME,
                new String[]{BlacklistStoreColumns.PATH},
                BlacklistStoreColumns.PATH + "=?",
                new String[]{path},
                null, null, null, null);

        boolean containsPath = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        return containsPath;
    }

    public void removePath(File file) {
        final SQLiteDatabase database = getWritableDatabase();
        String path = FileUtil.safeGetCanonicalPath(file);

        database.delete(BlacklistStoreColumns.NAME,
                BlacklistStoreColumns.PATH + "=?",
                new String[]{path});

        notifyMediaStoreChanged();
    }

    public void clear() {
        final SQLiteDatabase database = getWritableDatabase();
        database.delete(BlacklistStoreColumns.NAME, null, null);

        notifyMediaStoreChanged();
    }

    private void notifyMediaStoreChanged() {
        context.sendBroadcast(new Intent(MusicService.MEDIA_STORE_CHANGED));
    }

    @NonNull
    public List<String> getPaths() {
        Cursor cursor = getReadableDatabase().query(BlacklistStoreColumns.NAME,
                new String[]{BlacklistStoreColumns.PATH},
                null, null, null, null, null);

        List<String> paths = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                paths.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        if (cursor != null)
            cursor.close();
        return paths;
    }

    public interface BlacklistStoreColumns {
        String NAME = "blacklist";

        String PATH = "path";
    }
}
