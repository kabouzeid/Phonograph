package com.kabouzeid.gramophone.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ArtistJSONStore extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "artists_last_fm.db";
    private static final int VERSION = 1;
    private static ArtistJSONStore sInstance = null;

    public ArtistJSONStore(final Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    public static synchronized ArtistJSONStore getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new ArtistJSONStore(context.getApplicationContext());
        }
        return sInstance;
    }

    public void addArtistJSON(final String artistName, final String json) {
        if (artistName == null || json == null) {
            return;
        }

        final SQLiteDatabase database = getWritableDatabase();
        final ContentValues values = new ContentValues(2);

        database.beginTransaction();

        values.put(ArtistJSONColumns.ARTIST_NAME, artistName.trim().toLowerCase());
        values.put(ArtistJSONColumns.JSON_DATA, json);

        database.insert(ArtistJSONColumns.NAME, null, values);
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public String getArtistJSON(final String artistName) {
        if (artistName == null) {
            return null;
        }

        final SQLiteDatabase database = getReadableDatabase();
        final String[] projection = new String[]{
                ArtistJSONColumns.JSON_DATA,
                ArtistJSONColumns.ARTIST_NAME
        };
        final String selection = ArtistJSONColumns.ARTIST_NAME + "=?";
        final String[] having = new String[]{
                artistName.trim().toLowerCase()
        };
        Cursor cursor = database.query(ArtistJSONColumns.NAME, projection, selection, having, null,
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            final String json = cursor.getString(cursor.getColumnIndexOrThrow(ArtistJSONColumns.JSON_DATA));
            cursor.close();
            return json;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    public void removeArtistJSON(final String artistName) {
        final SQLiteDatabase database = getReadableDatabase();
        database.delete(ArtistJSONColumns.NAME, ArtistJSONColumns.ARTIST_NAME + "=?", new String[]{
                artistName.trim().toLowerCase()
        });

    }

    public interface ArtistJSONColumns {
        String NAME = "artist_json";
        String ARTIST_NAME = "artist_name";
        String JSON_DATA = "json_data";
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + ArtistJSONColumns.NAME +
                        " (" + ArtistJSONColumns.ARTIST_NAME + " TEXT NOT NULL," +
                        ArtistJSONColumns.JSON_DATA + " TEXT NOT NULL);"
        );
    }


    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ArtistJSONColumns.NAME);
        onCreate(db);
    }
}
