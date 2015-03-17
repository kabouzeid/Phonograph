package com.kabouzeid.gramophone.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.kabouzeid.gramophone.model.PlaylistSong;
import com.kabouzeid.gramophone.model.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karim on 16.03.15.
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
                return Integer.parseInt(uri.getLastPathSegment());
            }
            cursor.close();
        }
        return -1;
    }

    public static void deletePlaylist(final Context context, final int playlistId) {
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        context.getContentResolver().delete(uri, null, null);
    }

    public static void addToPlaylist(final Context context, final Song song, final long playlistId) {
        List<Song> helperList = new ArrayList<>();
        helperList.add(song);
        addToPlaylist(context, helperList, playlistId);
    }

    public static void addToPlaylist(final Context context, final List<Song> songs, final long playlistId) {
        final ContentResolver resolver = context.getContentResolver();

        final String[] projection = new String[]{
                MediaStore.Audio.PlaylistsColumns.NAME
        };

        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        cursor.moveToFirst();
        final String playlistName = cursor.getString(0);
        cursor.close();

        ContentValues[] contentValues = new ContentValues[songs.size()];
        for (int i = 0; i < songs.size(); i++) {
            contentValues[i] = new ContentValues();
            contentValues[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songs.get(i).id);
        }

        resolver.bulkInsert(uri, contentValues);
        Toast.makeText(context, "Added " + contentValues.length + " songs to playlist " + playlistName, Toast.LENGTH_SHORT).show();
        //TODO add string resource
    }

    public static void removeFromPlaylist(final Context context, final PlaylistSong song) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
                "external", song.playlistId);
        String selection = MediaStore.Audio.Playlists.Members._ID+ " =?";
        String[] selectionArgs = new String[]{String.valueOf(song.idInPlayList)};

        context.getContentResolver().delete(uri, selection, selectionArgs);
    }

    public static void removeFromPlaylist(final Context context, final List<PlaylistSong> songs) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
                "external", songs.get(0).playlistId);
        String selectionArgs[] = new String[songs.size()];
        for (int i = 0; i < selectionArgs.length; i++) {
            selectionArgs[i] = String.valueOf(songs.get(i).idInPlayList);
        }
        String selection = MediaStore.Audio.Playlists.Members._ID + " in (";
        for (String selectionArg : selectionArgs) selection += "?, ";
        selection = selection.substring(0, selection.length() - 2) + ")";

        context.getContentResolver().delete(uri, selection, selectionArgs);
    }
}
