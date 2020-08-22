package com.kabouzeid.gramophone.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.Toast;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.PlaylistAdapter;
import com.kabouzeid.gramophone.helper.M3UWriter;
import com.kabouzeid.gramophone.helper.menu.PlaylistMenuHelper;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.PlaylistSong;
import com.kabouzeid.gramophone.model.Song;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.provider.MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistsUtil {

    public static boolean doesPlaylistExist(@NonNull final Context context, final int playlistId) {
        return playlistId != -1 && doesPlaylistExist(context,
                MediaStore.Audio.Playlists._ID + "=?",
                new String[]{String.valueOf(playlistId)});
    }

    public static boolean doesPlaylistExist(@NonNull final Context context, final String name) {
        return doesPlaylistExist(context,
                MediaStore.Audio.PlaylistsColumns.NAME + "=?",
                new String[]{name});
    }

    public static int createPlaylist(@NonNull final Context context, @Nullable final String name) {
        int id = -1;
        if (name != null && name.length() > 0) {
            try {
                Cursor cursor = context.getContentResolver().query(EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Audio.Playlists._ID},
                        MediaStore.Audio.PlaylistsColumns.NAME + "=?", new String[]{name},
                        null);
                if (cursor == null || cursor.getCount() < 1) {
                    final ContentValues values = new ContentValues(1);
                    values.put(MediaStore.Audio.PlaylistsColumns.NAME, name);
                    final Uri uri = context.getContentResolver().insert(
                            EXTERNAL_CONTENT_URI,
                            values);
                    if (uri != null) {
                        // Necessary because somehow the MediaStoreObserver is not notified when adding a playlist
                        context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
                        Toast.makeText(context, context.getResources().getString(
                                R.string.created_playlist_x, name), Toast.LENGTH_SHORT).show();
                        id = Integer.parseInt(uri.getLastPathSegment());
                    }
                } else {
                    // Playlist exists
                    if (cursor.moveToFirst()) {
                        id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SecurityException ignored) {
            }
        }
        if (id == -1) {
            Toast.makeText(context, context.getResources().getString(
                    R.string.could_not_create_playlist), Toast.LENGTH_SHORT).show();
        }
        return id;
    }

    public static void deletePlaylists(@NonNull final Context context, @NonNull final List<Playlist> playlists) {
        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Playlists._ID + " IN (");
        for (int i = 0; i < playlists.size(); i++) {
            selection.append(playlists.get(i).id);
            if (i < playlists.size() - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
        try {
            context.getContentResolver().delete(EXTERNAL_CONTENT_URI, selection.toString(), null);
            context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
        } catch (SecurityException ignored) {
        }
    }

    public static void addToPlaylist(@NonNull final Context context, final Song song, final int playlistId, final boolean showToastOnFinish) {
        List<Song> helperList = new ArrayList<>();
        helperList.add(song);
        addToPlaylist(context, helperList, playlistId, showToastOnFinish);
    }
    public static void addToPlaylist(@NonNull final Context context, @NonNull final List<Song> songs, final int playlistId, final boolean showToastOnFinish) {
        final int size = songs.size();
        final ContentResolver resolver = context.getContentResolver();
        final String[] projection = new String[]{
                "max(" + MediaStore.Audio.Playlists.Members.PLAY_ORDER + ")",
        };
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor cursor = null;
        int base = 0;

        try {
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

            int numInserted = 0;
            for (int offSet = 0; offSet < size; offSet += 1000)
                numInserted += resolver.bulkInsert(uri, makeInsertItems(songs, offSet, 1000, base));

            if (showToastOnFinish) {
                Toast.makeText(context, context.getResources().getString(
                        R.string.inserted_x_songs_into_playlist_x, numInserted, getNameForPlaylist(context, playlistId)), Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException ignored) {
        }
    }

    public static void addToPlaylistWithoutDuplicates(@NonNull final Context context, @NonNull final List<Song> songs, final int[] songIds, final int playlistId, final boolean showToastOnFinish){
        boolean[] isSongInPlaylist = doPlaylistContains(context, playlistId, songIds);
        addToPlaylistWithoutDuplicates(context, songs, playlistId, isSongInPlaylist, showToastOnFinish);
    }
    public static void addToPlaylistWithoutDuplicates(@NonNull final Context context, @NonNull final List<Song> songs, final int playlistId, final boolean[] isSongInPlaylist,final boolean showToastOnFinish){
        ArrayList<Song> helperSongs = new ArrayList<Song>();
        for(int i = 0; i < songs.size(); i++){
            if(!isSongInPlaylist[i]){
                helperSongs.add(songs.get(i));
            }
        }
        addToPlaylist(context, helperSongs, playlistId, showToastOnFinish);
    }

    @NonNull
    public static ContentValues[] makeInsertItems(@NonNull final List<Song> songs, final int offset, int len, final int base) {
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

    public static void removeFromPlaylist(@NonNull final Context context, @NonNull final Song song, int playlistId) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
                "external", playlistId);
        String selection = MediaStore.Audio.Playlists.Members.AUDIO_ID + " =?";
        String[] selectionArgs = new String[]{String.valueOf(song.id)};

        try {
            context.getContentResolver().delete(uri, selection, selectionArgs);
        } catch (SecurityException ignored) {
        }
    }

    public static void removeFromPlaylist(@NonNull final Context context, @NonNull final List<PlaylistSong> songs) {
        final int playlistId = songs.get(0).playlistId;
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
                "external", playlistId);
        String selectionArgs[] = new String[songs.size()];
        for (int i = 0; i < selectionArgs.length; i++) {
            selectionArgs[i] = String.valueOf(songs.get(i).idInPlayList);
        }
        String selection = MediaStore.Audio.Playlists.Members._ID + " in (";
        //noinspection unused
        for (String selectionArg : selectionArgs) selection += "?, ";
        selection = selection.substring(0, selection.length() - 2) + ")";

        try {
            context.getContentResolver().delete(uri, selection, selectionArgs);
        } catch (SecurityException ignored) {
        }
    }

    public static boolean doPlaylistContains(@NonNull final Context context, final long playlistId, final int songId) {
        if (playlistId != -1) {
            try {
                Cursor c = context.getContentResolver().query(
                        MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                        new String[]{MediaStore.Audio.Playlists.Members.AUDIO_ID}, MediaStore.Audio.Playlists.Members.AUDIO_ID + "=?", new String[]{String.valueOf(songId)}, null);
                int count = 0;
                if (c != null) {
                    count = c.getCount();
                    c.close();
                }
                return count > 0;
            } catch (SecurityException ignored) {
            }
        }
        return false;
    }
    public static boolean[] doPlaylistContains(@NonNull final Context context, final long playlistId, final int[] songIds) {
        if (playlistId != -1) {
            final int[] songIdsOriginalOrder = songIds;
            int[] songIdsSorted = songIds.clone();
            java.util.Arrays.sort(songIdsSorted);

            boolean[] PlaylistContainsSongSorted = new boolean[songIdsSorted.length];
            boolean[] PlaylistContainsSongOriginalOrder;

            try {
                Cursor playlistSongs = context.getContentResolver().query(
                        MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                        new String[]{MediaStore.Audio.Playlists.Members.AUDIO_ID}, null, new String[]{}, MediaStore.Audio.Playlists.Members.AUDIO_ID + " ASC");

                if (playlistSongs != null && playlistSongs.getCount() > 0) {
                    playlistSongs.moveToNext(); //goes to first element

                    int songIndex = 0;
                    int playlistIndex = 0;
                    while (true)
                    {
                        if(songIndex >= songIdsSorted.length){
                            break;
                        }
                        int playlistSong = playlistSongs.getInt(0);
                        if(songIdsSorted[songIndex] > playlistSong)
                        {
                            playlistIndex++;
                            if(playlistIndex < playlistSongs.getCount())
                            {
                                playlistSongs.moveToNext();
                            }
                            else
                            {
                                break;
                            }
                        }
                        else if (songIdsSorted[songIndex] < playlistSong)
                        {
                            PlaylistContainsSongSorted[songIndex] = false;
                            songIndex++;
                        }
                        else if(songIdsSorted[songIndex] == playlistSong)
                        {
                            PlaylistContainsSongSorted[songIndex] = true;
                            songIndex++;
                        }
                    }
                    playlistSongs.close();
                }
            } catch (SecurityException ignored) {
            }
            //long[] partTimes = new long[songIds.length];//TODO: remove stopwatch
            //long startTime = System.currentTimeMillis();//TODO: remove stopwatch
            //Revert the sorting to return the right results
            PlaylistContainsSongOriginalOrder = PlaylistContainsSongSorted.clone();
            for(int i = 0; i < songIdsSorted.length; i++){
                //partTimes[i] = System.currentTimeMillis();//TODO: remove stopwatch
                if(songIdsOriginalOrder[i] == songIdsSorted[i]){
                    continue;
                }
                else{
                    int indexOfSongInSortedArray = i;
                    int compareValue;

                    int max = songIds.length;
                    int min = -1;
                    while(max - min > 15){ //this is a random number which I hope will result in a good efficiency. Tests showed it should be below 100.
                        compareValue = Integer.compare(songIdsOriginalOrder[i], songIdsSorted[indexOfSongInSortedArray]);
                        if(compareValue > 0){
                            min = indexOfSongInSortedArray;
                        }
                        else{
                            max = indexOfSongInSortedArray;
                        }
                        indexOfSongInSortedArray = (min + max)/2;
                    }
                    indexOfSongInSortedArray = min + 1;
                    while (songIdsOriginalOrder[i] != songIdsSorted[indexOfSongInSortedArray]){
                        indexOfSongInSortedArray++;
                    }
                    PlaylistContainsSongOriginalOrder[i] = PlaylistContainsSongSorted[indexOfSongInSortedArray];
                }
            }
            /*
            long stopTimeTotal = System.currentTimeMillis();//TODO: remove stopwatch
            long[] diffs = new long[songIds.length];//TODO: remove stopwatch
            diffs[songIds.length-1] = stopTimeTotal - partTimes[songIds.length-1];
            long min = diffs[songIds.length-1]; //TODO: remove stopwatch
            long max = diffs[songIds.length-1]; //TODO: remove stopwatch
            long sum = diffs[songIds.length-1]; //TODO: remove stopwatch
            for (int i = 0; i < songIdsSorted.length - 1; i++) {
                diffs[i] = partTimes[i+1] - partTimes[i];
                sum += diffs[i];
                min = Math.min(diffs[i], min);
                max = Math.max(diffs[i], max);
            }
            float mean = (float)sum / partTimes.length;
            long TotalTime = stopTimeTotal - startTime;//TODO: remove stopwatch
            */
            return PlaylistContainsSongOriginalOrder;
        }
        else{
            throw new IllegalArgumentException("Must be a non-negative integer");
        }
    }
    public static int doPlaylistContainsCount(@NonNull final Context context, final long playlistId, final int[] songIds) {
        return playlistContainsCount(context, doPlaylistContains(context, playlistId, songIds));
    }
    public static int playlistContainsCount(@NonNull final Context context, boolean[] isSongInPlaylist) {
        int count = 0;
        for(boolean isThisSongInPlaylist : isSongInPlaylist){
            if(isThisSongInPlaylist){
                count++;
            }
        }
        return count;
    }
    public static boolean doPlaylistContainsAnySong(@NonNull final Context context, final long playlistId, final int[] songIds) {
        if (playlistId != -1) {
            int[] songIdsSorted = songIds.clone();
            java.util.Arrays.sort(songIdsSorted);

            boolean result = false;

            try {
                Cursor playlistSongs = context.getContentResolver().query(
                        MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                        new String[]{MediaStore.Audio.Playlists.Members.AUDIO_ID}, null, new String[]{}, MediaStore.Audio.Playlists.Members.AUDIO_ID + " ASC");

                if (playlistSongs != null && playlistSongs.getCount() > 0) {
                    playlistSongs.moveToNext(); //goes to first element

                    int songIndex = 0;
                    int playlistIndex = 0;
                    while (true)
                    {
                        if(songIndex >= songIdsSorted.length){
                            break;
                        }
                        int playlistSong = playlistSongs.getInt(0);
                        if(songIdsSorted[songIndex] > playlistSong)
                        {
                            playlistIndex++;
                            if(playlistIndex < playlistSongs.getCount())
                            {
                                playlistSongs.moveToNext();
                            }
                            else
                            {
                                break;
                            }
                        }
                        else if (songIdsSorted[songIndex] < playlistSong)
                        {
                            songIndex++;
                        }
                        else if(songIdsSorted[songIndex] == playlistSong)
                        {
                            result = true;
                            songIndex++;
                            break;
                        }
                    }
                    playlistSongs.close();
                }
            } catch (SecurityException ignored) {
            }

            return result;
        }
        else{
            throw new IllegalArgumentException("Must be a non-negative integer");
        }
    }
    public static boolean playlistContainsAnySong(@NonNull final Context context, boolean[] isSongInPlaylist) {
        for(boolean isThisSongInPlaylist : isSongInPlaylist){
            if(isThisSongInPlaylist){
                return true;
            }
        }
        return false;
    }
    public static boolean doPlaylistContainsAllSongs(@NonNull final Context context, final long playlistId, final int[] songIds) {
        if (playlistId != -1) {
            int[] songIdsSorted = songIds.clone();
            java.util.Arrays.sort(songIdsSorted);

            boolean result = true;

            try {
                Cursor playlistSongs = context.getContentResolver().query(
                        MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                        new String[]{MediaStore.Audio.Playlists.Members.AUDIO_ID}, null, new String[]{}, MediaStore.Audio.Playlists.Members.AUDIO_ID + " ASC");

                if (playlistSongs != null && playlistSongs.getCount() > 0) {
                    playlistSongs.moveToNext(); //goes to first element

                    int songIndex = 0;
                    int playlistIndex = 0;
                    while (true)
                    {
                        if(songIndex >= songIdsSorted.length){
                            break; //all songs were in the playlist
                        }
                        int playlistSong = playlistSongs.getInt(0);
                        if(songIdsSorted[songIndex] > playlistSong)
                        {
                            playlistIndex++;
                            if(playlistIndex < playlistSongs.getCount())
                            {
                                playlistSongs.moveToNext();
                            }
                            else
                            {
                                result = false;
                                break; //there are songs missing, but the playlist has no songs left.
                            }
                        }
                        else if (songIdsSorted[songIndex] < playlistSong)
                        {
                            result = false;
                            songIndex++;
                            break; //at least one song was not in the playlist
                        }
                        else if(songIdsSorted[songIndex] == playlistSong)
                        {
                            songIndex++;
                        }
                    }
                    playlistSongs.close();
                }
                else {
                    result = false;
                }
            } catch (SecurityException ignored) {
            }

            return result;
        }
        else{
            throw new IllegalArgumentException("Must be a non-negative integer");
        }
    }
    public static boolean playlistContainsAllSongs(@NonNull final Context context, boolean[] isSongInPlaylist) {
        for(boolean isThisSongInPlaylist : isSongInPlaylist){
            if(!isThisSongInPlaylist){
                return false;
            }
        }
        return true;
    }

    public static boolean moveItem(@NonNull final Context context, int playlistId, int from, int to) {
        return MediaStore.Audio.Playlists.Members.moveItem(context.getContentResolver(),
                playlistId, from, to);
    }

    public static void renamePlaylist(@NonNull final Context context, final long id, final String newName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Audio.PlaylistsColumns.NAME, newName);
        try {
            context.getContentResolver().update(EXTERNAL_CONTENT_URI,
                    contentValues,
                    MediaStore.Audio.Playlists._ID + "=?",
                    new String[]{String.valueOf(id)});
            context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
        } catch (SecurityException ignored) {
        }
    }

    public static String getNameForPlaylist(@NonNull final Context context, final long id) {
        try {
            Cursor cursor = context.getContentResolver().query(EXTERNAL_CONTENT_URI,
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
        } catch (SecurityException ignored) {
        }
        return "";
    }

    public static File savePlaylist(Context context, Playlist playlist) throws IOException {
        return M3UWriter.write(context, new File(Environment.getExternalStorageDirectory(), "Playlists"), playlist);
    }

    private static boolean doesPlaylistExist(@NonNull Context context, @NonNull final String selection, @NonNull final String[] values) {
        Cursor cursor = context.getContentResolver().query(EXTERNAL_CONTENT_URI,
                new String[]{}, selection, values, null);

        boolean exists = false;
        if (cursor != null) {
            exists = cursor.getCount() != 0;
            cursor.close();
        }
        return exists;
    }
}