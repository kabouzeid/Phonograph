package com.kabouzeid.gramophone.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.widget.Toast;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.DataBaseChangedEvent;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.PlaylistSong;
import com.kabouzeid.gramophone.model.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistsUtil {

    public static int createPlaylist(final Context context, final String name) {
        if (name != null && name.length() > 0) {
            final ContentResolver resolver = context.getContentResolver();
            final String[] projection = new String[]{
                    MediaStore.Audio.PlaylistsColumns.NAME
            };
            final String selection = MediaStore.Audio.PlaylistsColumns.NAME + " = '" + name + "'";
            Cursor cursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    projection, selection, null, null);
            if (cursor.getCount() <= 0) {
                final ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.PlaylistsColumns.NAME, name);
                final Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                        values);
                cursor.close();
                if (uri != null) {
                    Toast.makeText(context, context.getResources().getString(
                            R.string.created_playlist_x, name), Toast.LENGTH_SHORT).show();
                    App.bus.post(new DataBaseChangedEvent(DataBaseChangedEvent.PLAYLISTS_CHANGED));
                    return Integer.parseInt(uri.getLastPathSegment());
                }
            }
            cursor.close();
        }
        Toast.makeText(context, context.getResources().getString(
                R.string.couldnot_create_playlist_x, name), Toast.LENGTH_SHORT).show();
        return -1;
    }

//    public static void clearPlaylist(final Context context, final int playlistId) {
//        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
//        context.getContentResolver().delete(uri, null, null);
//    }

//    public static void deletePlaylists(final Context context, final long playlistId) {
//        final Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
//        String where = MediaStore.Audio.Playlists._ID + "=?";
//        String[] whereVal = {String.valueOf(playlistId)};
//        context.getContentResolver().delete(uri, where, whereVal);
//        App.bus.post(new DataBaseChangedEvent(DataBaseChangedEvent.PLAYLISTS_CHANGED));
//    }

    public static void deletePlaylists(final Context context, final ArrayList<Playlist> playlists) {
        final Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Playlists._ID + " IN (");
        for (int i = 0; i < playlists.size(); i++) {
            selection.append(playlists.get(i).id);
            if (i < playlists.size() - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
        context.getContentResolver().delete(uri, selection.toString(), null);
        App.bus.post(new DataBaseChangedEvent(DataBaseChangedEvent.PLAYLISTS_CHANGED));
    }

//    public static void addToPlaylist(final Context context, final Song song, final int playlistId) {
//        List<Song> helperList = new ArrayList<>();
//        helperList.add(song);
//        addToPlaylist(context, helperList, playlistId);
//    }

    public static void addToPlaylist(final Context context, final List<Song> songs, final int playlistId) {
        final int size = songs.size();
        final ContentResolver resolver = context.getContentResolver();
        final String[] projection = new String[]{
                "max(" + MediaStore.Audio.Playlists.Members.PLAY_ORDER + ")",
        };
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor cursor = null;
        int base = 0;

        try {
            cursor = resolver.query(uri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                base = cursor.getInt(0) + 1;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        int numinserted = 0;
        for (int offSet = 0; offSet < size; offSet += 1000)
            numinserted += resolver.bulkInsert(uri, makeInsertItems(songs, offSet, 1000, base));

        Toast.makeText(context, context.getResources().getString(
                R.string.inserted_x_songs_into_playlist, numinserted), Toast.LENGTH_SHORT).show();
        App.bus.post(new DataBaseChangedEvent(DataBaseChangedEvent.PLAYLISTS_CHANGED));
    }

    public static ContentValues[] makeInsertItems(final List<Song> songs, final int offset, int len, final int base) {
        if (offset + len > songs.size()) {
            len = songs.size() - offset;
        }

        ContentValues[] contentValues = new ContentValues[len];

        for (int i = 0; i < len; i++) {
            contentValues[i] = new ContentValues();
            contentValues[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i);
            contentValues[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songs.get(offset + i).id);
        }
        return contentValues;
    }

//    public static void removeFromPlaylist(final Context context, final PlaylistSong song) {
//        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
//                "external", song.playlistId);
//        String selection = MediaStore.Audio.Playlists.Members._ID + " =?";
//        String[] selectionArgs = new String[]{String.valueOf(song.idInPlayList)};
//
//        context.getContentResolver().delete(uri, selection, selectionArgs);
//        App.bus.post(new DataBaseChangedEvent(DataBaseChangedEvent.PLAYLISTS_CHANGED));
//    }

    public static void removeFromPlaylist(final Context context, final List<PlaylistSong> songs) {
        final int playlistId = songs.get(0).playlistId;
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
                "external", playlistId);
        String selectionArgs[] = new String[songs.size()];
        for (int i = 0; i < selectionArgs.length; i++) {
            selectionArgs[i] = String.valueOf(songs.get(i).idInPlayList);
        }
        String selection = MediaStore.Audio.Playlists.Members._ID + " in (";
        for (String selectionArg : selectionArgs) selection += "?, ";
        selection = selection.substring(0, selection.length() - 2) + ")";

        context.getContentResolver().delete(uri, selection, selectionArgs);
        App.bus.post(new DataBaseChangedEvent(DataBaseChangedEvent.PLAYLISTS_CHANGED));
    }
//
//    public static int getSongCountForPlaylist(final Context context, final long playlistId) {
//        Cursor c = context.getContentResolver().query(
//                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
//                new String[]{BaseColumns._ID}, MUSIC_ONLY_SELECTION, null, null);
//        if (c != null) {
//            int count = 0;
//            if (c.moveToFirst()) {
//                count = c.getCount();
//            }
//            c.close();
//            return count;
//        }
//        return 0;
//    }

    public static void moveItem(final Context context, int playlistId, int from, int to) {
        MediaStore.Audio.Playlists.Members.moveItem(context.getContentResolver(),
                playlistId, from, to);
    }

    public static void renamePlaylist(final Context context, final long id, final String newName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Audio.PlaylistsColumns.NAME, newName);
        context.getContentResolver().update(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                contentValues,
                MediaStore.Audio.Playlists._ID + "=?",
                new String[]{String.valueOf(id)});
        App.bus.post(new DataBaseChangedEvent(DataBaseChangedEvent.PLAYLISTS_CHANGED));
    }

    public static String getNameForPlaylist(final Context context, final long id) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.PlaylistsColumns.NAME},
                BaseColumns._ID + "=?",
                new String[]{String.valueOf(id)},
                null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getString(0);
                }
            } finally {
                cursor.close();
            }
        }
        return "";
    }
}