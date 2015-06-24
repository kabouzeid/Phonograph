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
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.model.Song;

import java.util.ArrayList;

/**
 * @author Andrew Neal, modified for Phonograph by Karim Abou Zeid
 *         <p/>
 *         This keeps track of the music playback and history state of the playback service
 */
public class MusicPlaybackQueueStore extends SQLiteOpenHelper {
    private static MusicPlaybackQueueStore sInstance = null;
    public static final String DATABASE_NAME = "music_playback_state.db";
    public static final String PLAYING_QUEUE_TABLE_NAME = "playing_queue";
    public static final String ORIGINAL_PLAYING_QUEUE_TABLE_NAME = "original_playing_queue";
    private static final int VERSION = 1;

    /**
     * Constructor of <code>MusicPlaybackState</code>
     *
     * @param context The {@link Context} to use
     */
    public MusicPlaybackQueueStore(final Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        createTable(db, PLAYING_QUEUE_TABLE_NAME);
        createTable(db, ORIGINAL_PLAYING_QUEUE_TABLE_NAME);
    }

    private void createTable(final SQLiteDatabase db, final String tableName) {
        //noinspection StringBufferReplaceableByString
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(tableName);
        builder.append("(");

        builder.append(BaseColumns._ID);
        builder.append(" INT NOT NULL,");

        builder.append(MediaStore.Audio.AudioColumns.TITLE);
        builder.append(" STRING NOT NULL,");

        builder.append(MediaStore.Audio.AudioColumns.ARTIST);
        builder.append(" STRING NOT NULL,");

        builder.append(MediaStore.Audio.AudioColumns.ALBUM);
        builder.append(" STRING NOT NULL,");

        builder.append(MediaStore.Audio.AudioColumns.DURATION);
        builder.append(" LONG NOT NULL,");

        builder.append(MediaStore.Audio.AudioColumns.TRACK);
        builder.append(" INT NOT NULL,");

        builder.append(MediaStore.Audio.AudioColumns.ARTIST_ID);
        builder.append(" INT NOT NULL,");

        builder.append(MediaStore.Audio.AudioColumns.ALBUM_ID);
        builder.append(" INT NOT NULL);");

        db.execSQL(builder.toString());
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        // not necessary yet
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // If we ever have downgrade, drop the table to be safe
        db.execSQL("DROP TABLE IF EXISTS " + PLAYING_QUEUE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ORIGINAL_PLAYING_QUEUE_TABLE_NAME);
        onCreate(db);
    }

    /**
     * @param context The {@link Context} to use
     * @return A new instance of this class.
     */
    public static synchronized MusicPlaybackQueueStore getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new MusicPlaybackQueueStore(context.getApplicationContext());
        }
        return sInstance;
    }

    public synchronized void saveQueues(final ArrayList<Song> playingQueue, final ArrayList<Song> originalPlayingQueue) {
        saveQueue(PLAYING_QUEUE_TABLE_NAME, playingQueue);
        saveQueue(ORIGINAL_PLAYING_QUEUE_TABLE_NAME, originalPlayingQueue);
    }

    /**
     * Clears the existing database and saves the queue into the db so that when the
     * app is restarted, the tracks you were listening to is restored
     *
     * @param queue the queue to save
     */
    private synchronized void saveQueue(final String tableName, final ArrayList<Song> queue) {
        final SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();

        try {
            database.delete(tableName, null, null);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        final int NUM_PROCESS = 20;
        int position = 0;
        while (position < queue.size()) {
            database.beginTransaction();
            try {
                for (int i = position; i < queue.size() && i < position + NUM_PROCESS; i++) {
                    Song song = queue.get(i);
                    ContentValues values = new ContentValues(4);

                    values.put(BaseColumns._ID, song.id);
                    values.put(MediaStore.Audio.AudioColumns.TITLE, song.title);
                    values.put(MediaStore.Audio.AudioColumns.ARTIST, song.artistName);
                    values.put(MediaStore.Audio.AudioColumns.ALBUM, song.albumName);
                    values.put(MediaStore.Audio.AudioColumns.DURATION, song.duration);
                    values.put(MediaStore.Audio.AudioColumns.TRACK, song.trackNumber);
                    values.put(MediaStore.Audio.AudioColumns.ARTIST_ID, song.artistId);
                    values.put(MediaStore.Audio.AudioColumns.ALBUM_ID, song.albumId);

                    database.insert(tableName, null, values);
                }
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
                position += NUM_PROCESS;
            }
        }
    }

    public ArrayList<Song> getSavedPlayingQueue() {
        return getQueue(PLAYING_QUEUE_TABLE_NAME);
    }

    public ArrayList<Song> getSavedOriginalPlayingQueue() {
        return getQueue(ORIGINAL_PLAYING_QUEUE_TABLE_NAME);
    }

    private ArrayList<Song> getQueue(final String tableName) {
        Cursor cursor = getReadableDatabase().query(tableName, null,
                null, null, null, null, null);
        return SongLoader.getSongs(cursor);
    }
}
