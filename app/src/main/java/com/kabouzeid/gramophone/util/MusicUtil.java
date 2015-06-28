package com.kabouzeid.gramophone.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.loader.PlaylistLoader;
import com.kabouzeid.gramophone.loader.SongFilePathLoader;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.model.DataBaseChangedEvent;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.Song;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class MusicUtil {
    public static final String TAG = MusicUtil.class.getSimpleName();

    public static Uri getAlbumArtUri(int albumId) {
        final Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");

        return ContentUris.withAppendedId(sArtworkUri, albumId);
    }

    public static Uri getSongUri(int songId) {
        return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);
    }

    public static Intent createShareSongFileIntent(final Context context, int songId) {
        return new Intent()
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + SongFilePathLoader.getSongFilePath(context, songId)))
                .setType("audio/*");
    }

    public static void setRingtone(final Context context, final int id) {
        final ContentResolver resolver = context.getContentResolver();
        final Uri uri = getSongUri(id);
        try {
            final ContentValues values = new ContentValues(2);
            values.put(MediaStore.Audio.AudioColumns.IS_RINGTONE, "1");
            values.put(MediaStore.Audio.AudioColumns.IS_ALARM, "1");
            resolver.update(uri, values, null, null);
        } catch (final UnsupportedOperationException ignored) {
            return;
        }

        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.MediaColumns.TITLE},
                BaseColumns._ID + "=?",
                new String[]{String.valueOf(id)},
                null);
        try {
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                Settings.System.putString(resolver, Settings.System.RINGTONE, uri.toString());
                final String message = context.getString(R.string.x_has_been_set_as_ringtone, cursor.getString(0));
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public static String getArtistInfoString(Context context, Artist artist) {
        return artist.songCount + " " + context.getResources().getString(R.string.songs) + " | " + artist.albumCount + " " + context.getResources().getString(R.string.albums);
    }

    public static String getReadableDurationString(long songDurationMillis) {
        long minutes = (songDurationMillis / 1000) / 60;
        long seconds = (songDurationMillis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    //iTunes uses for example 1002 for track 2 CD1 or 3011 for track 11 CD3.
    //this method converts those values to normal tracknumbers
    public static int getFixedTrackNumber(int trackNumberToFix) {
        return trackNumberToFix % 1000;
    }

    public static void insertAlbumArt(Context context, int albumId, String path) {
        ContentResolver contentResolver = context.getContentResolver();

        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        contentResolver.delete(ContentUris.withAppendedId(artworkUri, albumId), null, null);

        ContentValues values = new ContentValues();
        values.put("album_id", albumId);
        values.put("_data", path);

        contentResolver.insert(artworkUri, values);
    }

    public static void deleteAlbumArt(Context context, int albumId) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri localUri = Uri.parse("content://media/external/audio/albumart");
        contentResolver.delete(ContentUris.withAppendedId(localUri, albumId), null, null);
    }

    public static File createAlbumArtFile(String name) {
        return new File(createAlbumArtDir(), name + System.currentTimeMillis());
    }

    public static File createAlbumArtDir() {
        File albumArtDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/.albumart/");
        if (!albumArtDir.exists()) {
            albumArtDir.mkdirs();
            try {
                new File(albumArtDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "error while creating .nomedia file", e);
            }
        }
        return albumArtDir;
    }

    public static void deleteTracks(final Context context, final List<Song> songs) {
        final String[] projection = new String[]{
                BaseColumns._ID, MediaStore.MediaColumns.DATA
        };
        final StringBuilder selection = new StringBuilder();
        selection.append(BaseColumns._ID + " IN (");
        for (int i = 0; i < songs.size(); i++) {
            selection.append(songs.get(i).id);
            if (i < songs.size() - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
        final Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection.toString(),
                null, null);
        if (cursor != null) {
            // Step 1: Remove selected tracks from the current playlist, as well
            // as from the album art cache
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                final int id = cursor.getInt(0);
                final Song song = SongLoader.getSong(context, id);
                MusicPlayerRemote.removeFromQueue(song);
                cursor.moveToNext();
            }

            // Step 2: Remove selected tracks from the database
            context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    selection.toString(), null);

            // Step 3: Remove files from card
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                final String name = cursor.getString(1);
                try { // File.delete can throw a security exception
                    final File f = new File(name);
                    if (!f.delete()) {
                        // I'm not sure if we'd ever get here (deletion would
                        // have to fail, but no exception thrown)
                        Log.e("MusicUtils", "Failed to delete file " + name);
                    }
                    cursor.moveToNext();
                } catch (final SecurityException ex) {
                    cursor.moveToNext();
                } catch (NullPointerException e) {
                    Log.e("MusicUtils", "Failed to find file " + name);
                }
            }
            cursor.close();
        }
        context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
        Toast.makeText(context, context.getString(R.string.deleted_x_songs, songs.size()), Toast.LENGTH_SHORT).show();
        App.bus.post(new DataBaseChangedEvent(DataBaseChangedEvent.DATABASE_CHANGED));
    }

    private static Playlist getFavoritesPlaylist(final Context context) {
        return PlaylistLoader.getPlaylist(context, context.getString(R.string.favorites));
    }

    private static Playlist getOrCreateFavoritesPlaylist(final Context context) {
        return PlaylistLoader.getPlaylist(context, PlaylistsUtil.createPlaylist(context, context.getString(R.string.favorites)));
    }

    public static boolean isFavorite(final Context context, final Song song) {
        return PlaylistsUtil.doPlaylistContains(context, getFavoritesPlaylist(context).id, song.id);
    }

    public static void toggleFavorite(final Context context, final Song song) {
        if (isFavorite(context, song)) {
            PlaylistsUtil.removeFromPlaylist(context, song, getFavoritesPlaylist(context).id);
        } else {
            PlaylistsUtil.addToPlaylist(context, song, getOrCreateFavoritesPlaylist(context).id, false);
        }
    }
}
