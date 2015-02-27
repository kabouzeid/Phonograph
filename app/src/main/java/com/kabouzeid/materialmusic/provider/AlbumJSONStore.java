package com.kabouzeid.materialmusic.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AlbumJSONStore extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "albumJSONLastFM.db";
    private static final int VERSION = 1;
    private static AlbumJSONStore sInstance = null;

    public AlbumJSONStore(final Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    public static synchronized AlbumJSONStore getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new AlbumJSONStore(context.getApplicationContext());
        }
        return sInstance;
    }

    public static void deleteDatabase(final Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }

    public void addAlbumJSON(final String albumAndArtistName, final String JSON) {
        if (albumAndArtistName == null || JSON == null) {
            return;
        }

        final SQLiteDatabase database = getWritableDatabase();
        final ContentValues values = new ContentValues(2);

        database.beginTransaction();

        values.put(AlbumJSONColumns.ALBUMANDARTIST_NAME, albumAndArtistName.trim().toLowerCase());
        values.put(AlbumJSONColumns.JSON, JSON);

        database.insert(AlbumJSONColumns.NAME, null, values);
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public String getAlbumJSON(final String albumAndArtistName) {
        if (albumAndArtistName == null) {
            return null;
        }

        final SQLiteDatabase database = getReadableDatabase();
        final String[] projection = new String[]{
                AlbumJSONColumns.JSON,
                AlbumJSONColumns.ALBUMANDARTIST_NAME
        };
        final String selection = AlbumJSONColumns.ALBUMANDARTIST_NAME + "=?";
        final String[] having = new String[]{
                albumAndArtistName.trim().toLowerCase()
        };
        Cursor cursor = database.query(AlbumJSONColumns.NAME, projection, selection, having, null,
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            final String JSON = cursor.getString(cursor.getColumnIndexOrThrow(AlbumJSONColumns.JSON));
            cursor.close();
            return JSON;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    public void removeItem(final String albumAndArtistName) {
        final SQLiteDatabase database = getReadableDatabase();
        database.delete(AlbumJSONColumns.NAME, AlbumJSONColumns.ALBUMANDARTIST_NAME + " = ?", new String[]{
                albumAndArtistName.trim().toLowerCase()
        });

    }

    public interface AlbumJSONColumns {
        public static final String NAME = "AlbumJSON";
        public static final String ALBUMANDARTIST_NAME = "AlbumAndArtistName";
        public static final String JSON = "JSON";
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + AlbumJSONColumns.NAME +
                        " (" + AlbumJSONColumns.ALBUMANDARTIST_NAME + " TEXT NOT NULL," +
                        AlbumJSONColumns.JSON + " TEXT NOT NULL);"
        );
    }


    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AlbumJSONColumns.NAME);
        onCreate(db);
    }


}
