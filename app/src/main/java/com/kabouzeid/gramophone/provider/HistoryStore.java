/*
* Copyright (C) 2014 The CyanogenMod Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.kabouzeid.gramophone.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.kabouzeid.gramophone.util.PreferenceUtil;

public class HistoryStore extends SQLiteOpenHelper {
    public static final int CONST_MAX_HISTORY_SIZE = 350;
    public static final int CONST_MIN_HISTORY_SIZE = 25;
    public static final int CONST_DEFAULT_HISTORY_SIZE = 100;

    private static final String DATABASE_NAME = "history.db";
    private static final int VERSION = 1;

    private int maxHistorySize;

    @Nullable
    private static HistoryStore sInstance = null;

    public HistoryStore(final Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        this.maxHistorySize = PreferenceUtil.getInstance(context).getMaxHistorySize();
    }

    @Override
    public void onCreate(@NonNull final SQLiteDatabase db) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(RecentStoreColumns.NAME);
        builder.append("(");
        builder.append(RecentStoreColumns.ID);
        builder.append(" LONG NOT NULL,");
        builder.append(RecentStoreColumns.TIME_PLAYED);
        builder.append(" LONG NOT NULL);");

        db.execSQL(builder.toString());
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RecentStoreColumns.NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RecentStoreColumns.NAME);
        onCreate(db);
    }

    @NonNull
    public static synchronized HistoryStore getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new HistoryStore(context.getApplicationContext());
        }
        return sInstance;
    }

    public void addSongId(final long songId) {
        if (songId == -1) {
            return;
        }

        final SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            // remove previous entries
            removeSongId(songId);

            // add the entry
            final ContentValues values = new ContentValues(2);
            values.put(RecentStoreColumns.ID, songId);
            values.put(RecentStoreColumns.TIME_PLAYED, System.currentTimeMillis());
            database.insert(RecentStoreColumns.NAME, null, values);

            // if our db is too large, delete the extra items
            Cursor cursor = null;
            try {
                cursor = database.query(RecentStoreColumns.NAME,
                        new String[]{RecentStoreColumns.TIME_PLAYED}, null, null, null, null,
                        RecentStoreColumns.TIME_PLAYED + " ASC");

                if (cursor != null && cursor.getCount() > maxHistorySize) {
                    cursor.moveToPosition(cursor.getCount() - maxHistorySize);
                    long timeOfRecordToKeep = cursor.getLong(0);

                    database.delete(RecentStoreColumns.NAME,
                            RecentStoreColumns.TIME_PLAYED + " < ?",
                            new String[]{String.valueOf(timeOfRecordToKeep)});

                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } finally {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    public void removeSongId(final long songId) {
        final SQLiteDatabase database = getWritableDatabase();
        database.delete(RecentStoreColumns.NAME,
                RecentStoreColumns.ID + " = ?",
                new String[]{String.valueOf(songId)});
    }

    public void refreshMaxHistorySize(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;

        final SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = null;
        database.beginTransaction();
        try {
            cursor = database.query(RecentStoreColumns.NAME,
                    new String[]{
                            RecentStoreColumns.ID,
                            RecentStoreColumns.TIME_PLAYED}, null, null, null, null,
                    RecentStoreColumns.TIME_PLAYED + " DESC");

            if (cursor != null && cursor.getCount() > maxHistorySize &&
                    cursor.moveToPosition(maxHistorySize)) {
                do {
                    database.delete(RecentStoreColumns.NAME,
                            RecentStoreColumns.ID + "=?",
                            new String[] {
                                    String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(RecentStoreColumns.ID)))
                            });
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    public void clear() {
        final SQLiteDatabase database = getWritableDatabase();
        database.delete(RecentStoreColumns.NAME, null, null);
    }

    public boolean contains(long id) {
        final SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(RecentStoreColumns.NAME,
                new String[]{RecentStoreColumns.ID},
                RecentStoreColumns.ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);

        boolean containsId = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        return containsId;
    }

    public Cursor queryRecentIds() {
        final SQLiteDatabase database = getReadableDatabase();
        return database.query(RecentStoreColumns.NAME,
                new String[]{RecentStoreColumns.ID}, null, null, null, null,
                RecentStoreColumns.TIME_PLAYED + " DESC");
    }

    public interface RecentStoreColumns {
        String NAME = "recent_history";

        String ID = "song_id";

        String TIME_PLAYED = "time_played";
    }
}
