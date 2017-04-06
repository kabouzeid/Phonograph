package com.kabouzeid.gramophone.service.automusicmodel;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.provider.MediaStore.Audio.AudioColumns;

import java.util.ArrayList;
import java.util.Iterator;

import static android.R.attr.duration;

/**
 * Created by Beesham on 3/28/2017.
 */

public class AutoMusicSource implements MusicProviderSource{

    private static final String TAG = AutoMusicSource.class.getName();
    private Context mContext;

    public AutoMusicSource(Context context) {
        mContext = context;
    }

    @Override
    public Iterator<MediaMetadataCompat> iterator() {

        //All songs
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uriSongs = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uriSongs, null, null, null, null);

        //Playlists
        Uri uriPlaylists = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        Cursor playlistCursor = contentResolver.query(uriPlaylists, null, null, null, null);
        Log.v(TAG, "playlist cur size: " + playlistCursor.getCount());

        playlistCursor.moveToFirst();
        while (playlistCursor.moveToNext()){
            Log.v(TAG, "playlist cur content: " +
                    playlistCursor.getString(playlistCursor.getColumnIndex(MediaStore.Audio.PlaylistsColumns.NAME)));
        }


        ArrayList<MediaMetadataCompat> tracks = new ArrayList<>();

        if(cursor == null){
            return null;
        }else if(!cursor.moveToFirst()){
            //Cursor empty, no media
        }else{

            for(int i = 0;i<cursor.getCount();i++){
                tracks.add(buildSongsMediaMetadata(cursor));
                cursor.moveToNext();
            }
        }

        return tracks.iterator();
    }

    private MediaMetadataCompat buildSongsMediaMetadata(Cursor c){
        String _ID = c.getString(c.getColumnIndex(MediaStore.Audio.Media._ID));
        String title = c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE));
        String album =c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM));
        String artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        String genre = "<Unknown>";//c.getString(c.getColumnIndex(android.provider.MediaStore.Audio.Media.));
        String source = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
        //String iconUrl = "";
        int trackNumber = c.getInt(c.getColumnIndex(MediaStore.Audio.Media.TRACK));
        //int totalTrackCount = c.getInt(c.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE));
        int duration = c.getInt(c.getColumnIndex(MediaStore.Audio.Media.DURATION)) * 1000; // ms

        /*if(!TextUtils.isEmpty(c.getString(c.getColumnIndex(MediaStore.Audio.Genres.NAME)))){
            //iconUrl = c.getString(c.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            genre = c.getString(c.getColumnIndex(MediaStore.Audio.Genres.NAME));
        }*/

        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, _ID)
                .putString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE, source)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                //.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, iconUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
                //.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                .build();
    }
}
