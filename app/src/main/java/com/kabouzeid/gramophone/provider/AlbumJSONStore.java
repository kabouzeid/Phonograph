package com.kabouzeid.gramophone.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class AlbumJSONStore extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "albums_last_fm.db";
    private static final int VERSION = 1;
    @Nullable
    private static AlbumJSONStore sInstance = null;

    public AlbumJSONStore(final Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Nullable
    public static synchronized AlbumJSONStore getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new AlbumJSONStore(context.getApplicationContext());
        }
        return sInstance;
    }

    public void addAlbumJSON(@Nullable final String albumAndArtistName, @Nullable final String json) {
        if (albumAndArtistName == null || json == null) {
            return;
        }

        final SQLiteDatabase database = getWritableDatabase();
        final ContentValues values = new ContentValues(2);

        database.beginTransaction();

        values.put(AlbumJSONColumns.ALBUM_PLUS_ARTIST_NAME, albumAndArtistName.trim().toLowerCase());
        values.put(AlbumJSONColumns.JSON_DATA, json);

        database.insert(AlbumJSONColumns.NAME, null, values);
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    @Nullable
    public String getJSONData(@Nullable final String albumAndArtistName) {
        if (albumAndArtistName == null) {
            return null;
        }

        final SQLiteDatabase database = getReadableDatabase();
        final String[] projection = new String[]{
                AlbumJSONColumns.JSON_DATA,
                AlbumJSONColumns.ALBUM_PLUS_ARTIST_NAME
        };
        final String selection = AlbumJSONColumns.ALBUM_PLUS_ARTIST_NAME + "=?";
        final String[] having = new String[]{
                albumAndArtistName.trim().toLowerCase()
        };
        Cursor cursor = database.query(AlbumJSONColumns.NAME, projection, selection, having, null,
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            final String json = cursor.getString(cursor.getColumnIndexOrThrow(AlbumJSONColumns.JSON_DATA));
            cursor.close();
            return json;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    public void removeAlbumJSON(@NonNull final String albumAndArtistName) {
        final SQLiteDatabase database = getReadableDatabase();
        database.delete(AlbumJSONColumns.NAME, AlbumJSONColumns.ALBUM_PLUS_ARTIST_NAME + " = ?", new String[]{
                albumAndArtistName.trim().toLowerCase()
        });

    }

    public interface AlbumJSONColumns {
        String NAME = "album_json";
        String ALBUM_PLUS_ARTIST_NAME = "album_plus_artist_name";
        String JSON_DATA = "json_data";
    }

    @Override
    public void onCreate(@NonNull final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + AlbumJSONColumns.NAME +
                        " (" + AlbumJSONColumns.ALBUM_PLUS_ARTIST_NAME + " TEXT NOT NULL," +
                        AlbumJSONColumns.JSON_DATA + " TEXT NOT NULL);"
        );
    }


    @Override
    public void onUpgrade(@NonNull final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AlbumJSONColumns.NAME);
        onCreate(db);
    }


}
