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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.loader.PlaylistLoader;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.model.lyrics.AbsSynchronizedLyrics;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class MusicUtil {
    public static final String TAG = MusicUtil.class.getSimpleName();

    public static Uri getMediaStoreAlbumCoverUri(int albumId) {
        final Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");

        return ContentUris.withAppendedId(sArtworkUri, albumId);
    }

    public static Uri getSongFileUri(int songId) {
        return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);
    }

    @NonNull
    public static Intent createShareSongFileIntent(@NonNull final Song song) {
        return new Intent()
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + song.data))
                .setType("audio/*");
    }

    public static void setRingtone(@NonNull final Context context, final int id) {
        final ContentResolver resolver = context.getContentResolver();
        final Uri uri = getSongFileUri(id);
        try {
            final ContentValues values = new ContentValues(2);
            values.put(MediaStore.Audio.AudioColumns.IS_RINGTONE, "1");
            values.put(MediaStore.Audio.AudioColumns.IS_ALARM, "1");
            resolver.update(uri, values, null, null);
        } catch (@NonNull final UnsupportedOperationException ignored) {
            return;
        }

        try {
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
                }
            }
        } catch (SecurityException ignored) {
        }
    }

    @NonNull
    public static String getArtistInfoString(@NonNull final Context context, @NonNull final Artist artist) {
        int albumCount = artist.getAlbumCount();
        int songCount = artist.getSongCount();
        String albumString = albumCount == 1 ? context.getResources().getString(R.string.album) : context.getResources().getString(R.string.albums);
        String songString = songCount == 1 ? context.getResources().getString(R.string.song) : context.getResources().getString(R.string.songs);
        return albumCount + " " + albumString + " • " + songCount + " " + songString;
    }

    public static String getReadableDurationString(long songDurationMillis) {
        long minutes = (songDurationMillis / 1000) / 60;
        long seconds = (songDurationMillis / 1000) % 60;
        if (minutes < 60) {
            return String.format("%01d:%02d", minutes, seconds);
        } else {
            long hours = minutes / 60;
            minutes = minutes % 60;
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
    }

    //iTunes uses for example 1002 for track 2 CD1 or 3011 for track 11 CD3.
    //this method converts those values to normal tracknumbers
    public static int getFixedTrackNumber(int trackNumberToFix) {
        return trackNumberToFix % 1000;
    }

    public static void insertAlbumArt(@NonNull Context context, int albumId, String path) {
        ContentResolver contentResolver = context.getContentResolver();

        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        contentResolver.delete(ContentUris.withAppendedId(artworkUri, albumId), null, null);

        ContentValues values = new ContentValues();
        values.put("album_id", albumId);
        values.put("_data", path);

        contentResolver.insert(artworkUri, values);
    }

    public static void deleteAlbumArt(@NonNull Context context, int albumId) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri localUri = Uri.parse("content://media/external/audio/albumart");
        contentResolver.delete(ContentUris.withAppendedId(localUri, albumId), null, null);
    }

    @NonNull
    public static File createAlbumArtFile() {
        return new File(createAlbumArtDir(), String.valueOf(System.currentTimeMillis()));
    }

    @NonNull
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File createAlbumArtDir() {
        File albumArtDir = new File(Environment.getExternalStorageDirectory(), "/albumthumbs/");
        if (!albumArtDir.exists()) {
            albumArtDir.mkdirs();
            try {
                new File(albumArtDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return albumArtDir;
    }

    public static void deleteTracks(@NonNull final Context context, @NonNull final List<Song> songs) {
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

        try {
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
                    } catch (@NonNull final SecurityException ex) {
                        cursor.moveToNext();
                    } catch (NullPointerException e) {
                        Log.e("MusicUtils", "Failed to find file " + name);
                    }
                }
                cursor.close();
            }
            context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
            Toast.makeText(context, context.getString(R.string.deleted_x_songs, songs.size()), Toast.LENGTH_SHORT).show();
        } catch (SecurityException ignored) {
        }
    }

    public static boolean isFavoritePlaylist(@NonNull final Context context, @NonNull final Playlist playlist) {
        return playlist.name != null && playlist.name.equals(context.getString(R.string.favorites));
    }

    public static Playlist getFavoritesPlaylist(@NonNull final Context context) {
        return PlaylistLoader.getPlaylist(context, context.getString(R.string.favorites));
    }

    private static Playlist getOrCreateFavoritesPlaylist(@NonNull final Context context) {
        return PlaylistLoader.getPlaylist(context, PlaylistsUtil.createPlaylist(context, context.getString(R.string.favorites)));
    }

    public static boolean isFavorite(@NonNull final Context context, @NonNull final Song song) {
        return PlaylistsUtil.doPlaylistContains(context, getFavoritesPlaylist(context).id, song.id);
    }

    public static void toggleFavorite(@NonNull final Context context, @NonNull final Song song) {
        if (isFavorite(context, song)) {
            PlaylistsUtil.removeFromPlaylist(context, song, getFavoritesPlaylist(context).id);
        } else {
            PlaylistsUtil.addToPlaylist(context, song, getOrCreateFavoritesPlaylist(context).id, false);
        }
    }

    public static boolean isArtistNameUnknown(@Nullable String artistName) {
        if (TextUtils.isEmpty(artistName)) return false;
        artistName = artistName.trim().toLowerCase();
        return artistName.equals("unknown") || artistName.equals("<unknown>");
    }

    @NonNull
    public static String getSectionName(@Nullable String musicMediaTitle) {
        if (TextUtils.isEmpty(musicMediaTitle)) return "";
        musicMediaTitle = musicMediaTitle.trim().toLowerCase();
        if (musicMediaTitle.startsWith("the ")) {
            musicMediaTitle = musicMediaTitle.substring(4);
        } else if (musicMediaTitle.startsWith("a ")) {
            musicMediaTitle = musicMediaTitle.substring(2);
        }
        if (musicMediaTitle.isEmpty()) return "";
        return String.valueOf(musicMediaTitle.charAt(0)).toUpperCase();
    }

    @Nullable
    public static String getLyrics(Song song) {
        String lyrics = null;

        File file = new File(song.data);

        try {
            lyrics = AudioFileIO.read(file).getTagOrCreateDefault().getFirst(FieldKey.LYRICS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (lyrics == null || lyrics.trim().isEmpty() || !AbsSynchronizedLyrics.isSynchronized(lyrics)) {
            File dir = file.getAbsoluteFile().getParentFile();

            if (dir != null && dir.exists() && dir.isDirectory()) {
                String format = ".*%s.*\\.(lrc|txt)";
                String filename = Pattern.quote(FileUtil.stripExtension(file.getName()));
                String songtitle = Pattern.quote(song.title);

                final ArrayList<Pattern> patterns = new ArrayList<>();
                patterns.add(Pattern.compile(String.format(format, filename), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
                patterns.add(Pattern.compile(String.format(format, songtitle), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));

                File[] files = dir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        for (Pattern pattern : patterns) {
                            if (pattern.matcher(f.getName()).matches()) return true;
                        }
                        return false;
                    }
                });

                if (files != null && files.length > 0) {
                    for (File f : files) {
                        try {
                            String newLyrics = FileUtil.read(f);
                            if (newLyrics != null && !newLyrics.trim().isEmpty()) {
                                if (AbsSynchronizedLyrics.isSynchronized(newLyrics)) {
                                    return newLyrics;
                                }
                                lyrics = newLyrics;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return lyrics;
    }
}
