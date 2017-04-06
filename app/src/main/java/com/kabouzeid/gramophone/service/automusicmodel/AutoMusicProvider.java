package com.kabouzeid.gramophone.service.automusicmodel;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;



import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


import static com.kabouzeid.gramophone.service.automusicmodel.MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM;
import static com.kabouzeid.gramophone.service.automusicmodel.MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE;
import static com.kabouzeid.gramophone.service.automusicmodel.MediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST;
import static com.kabouzeid.gramophone.service.automusicmodel.MediaIDHelper.MEDIA_ID_ROOT;
import static com.kabouzeid.gramophone.service.automusicmodel.MediaIDHelper.createMediaID;

/**
 * Created by Beesham on 3/28/2017.
 */

public class AutoMusicProvider {
    private static String TAG = AutoMusicProvider.class.getName();

    private MusicProviderSource mSource;

    //Categorized caches for music track data
    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListByGenre;
    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListByPlaylist;
    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListByAlbum;

    private final ConcurrentMap<String, MutableMediaMetadata> mMusicListById;


    enum State{
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private volatile State mCurrentState = State.NON_INITIALIZED;

    public interface Callback{
        void onMusicCatalogReady(boolean success);
    }

    public AutoMusicProvider(Context context){
        this(new AutoMusicSource(context));
    }

    public AutoMusicProvider(AutoMusicSource source){
        mSource = source;

        mMusicListByGenre = new ConcurrentHashMap<>();
        mMusicListByPlaylist = new ConcurrentHashMap<>();
        mMusicListByAlbum = new ConcurrentHashMap<>();
        mMusicListById = new ConcurrentHashMap<>();
    }

    /**
     * Get an iterator over the list of genres
     *
     * @return genres
     */
    public Iterable<String> getGenres() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByGenre.keySet();
    }

    /**
     * Get music tracks of the given genre
     *
     */
    public Iterable<MediaMetadataCompat> getMusicsByGenre(String genre) {
        if (mCurrentState != State.INITIALIZED || !mMusicListByGenre.containsKey(genre)) {
            return Collections.emptyList();
        }
        return mMusicListByGenre.get(genre);
    }

    /**
     * Return the MediaMetadataCompat for the given musicID.
     *
     * @param musicId The unique, non-hierarchical music ID.
     */
    public MediaMetadataCompat getMusic(String musicId) {
        return mMusicListById.containsKey(musicId) ? mMusicListById.get(musicId).metadata : null;
    }

    public boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }

    /**
     * Get the list of music tracks from a server and caches the track information
     * for future reference, keying tracks by musicId and grouping by genre.
     */
    public void retrieveMediaAsync(final Callback callback) {
        Log.d(TAG, "retrieveMediaAsync called");
        if (mCurrentState == State.INITIALIZED) {
            if (callback != null) {
                // Nothing to do, execute callback immediately
                callback.onMusicCatalogReady(true);
            }
            return;
        }

        // Asynchronously load the music catalog in a separate thread
        new AsyncTask<Void, Void, State>() {
            @Override
            protected State doInBackground(Void... params) {
                retrieveMedia();
                return mCurrentState;
            }

            @Override
            protected void onPostExecute(State current) {
                if (callback != null) {
                    callback.onMusicCatalogReady(current == State.INITIALIZED);
                }
            }
        }.execute();
    }

    private synchronized void buildListsByGenre() {
        ConcurrentMap<String, List<MediaMetadataCompat>> newMusicListByGenre = new ConcurrentHashMap<>();

        for (MutableMediaMetadata m : mMusicListById.values()) {
            String genre = m.metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
            List<MediaMetadataCompat> list = newMusicListByGenre.get(genre);
            if (list == null) {
                list = new ArrayList<>();
                newMusicListByGenre.put(genre, list);
            }
            list.add(m.metadata);
        }
        mMusicListByGenre = newMusicListByGenre;
    }

    private synchronized void retrieveMedia() {
        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;

                Iterator<MediaMetadataCompat> tracks = mSource.iterator();
                while (tracks.hasNext()) {
                    MediaMetadataCompat item = tracks.next();
                    String musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                    mMusicListById.put(musicId, new MutableMediaMetadata(musicId, item));
                }
                buildListsByGenre();
                mCurrentState = State.INITIALIZED;
            }
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                // Something bad happened, so we reset state to NON_INITIALIZED to allow
                // retries (eg if the network connection is temporary unavailable)
                mCurrentState = State.NON_INITIALIZED;
            }
        }
    }


    public List<MediaBrowserCompat.MediaItem> getChildren(String mediaId, Resources resources) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        if (!MediaIDHelper.isBrowseable(mediaId)) {
            return mediaItems;
        }

        if (MEDIA_ID_ROOT.equals(mediaId)) {
            mediaItems.add(createBrowsableMediaItemForRoot(MEDIA_ID_MUSICS_BY_GENRE, resources));
            mediaItems.add(createBrowsableMediaItemForRoot(MEDIA_ID_MUSICS_BY_PLAYLIST, resources));
            mediaItems.add(createBrowsableMediaItemForRoot(MEDIA_ID_MUSICS_BY_ALBUM, resources));


        } else if (MEDIA_ID_MUSICS_BY_GENRE.equals(mediaId)) {
            for (String genre : getGenres()) {
                mediaItems.add(createBrowsableMediaItemForGenre(genre, resources));
            }

        } else if (mediaId.startsWith(MEDIA_ID_MUSICS_BY_GENRE)) {
            String genre = MediaIDHelper.getHierarchy(mediaId)[1];
            for (MediaMetadataCompat metadata : getMusicsByGenre(genre)) {
                mediaItems.add(createMediaItem(metadata));
            }

        } else {
            Log.w(TAG, "Skipping unmatched mediaId: " + mediaId);
        }
        return mediaItems;
    }

    //TODO: move hardcoded strings; add iconUri
    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForRoot(String mediaId, Resources resources) {
        MediaDescriptionCompat description;
        switch (mediaId){
            case (MEDIA_ID_MUSICS_BY_GENRE):
                description = new MediaDescriptionCompat.Builder()
                        .setMediaId(mediaId)//MEDIA_ID_MUSICS_BY_GENRE)
                        .setTitle("Genres") // .setTitle(resources.getString(R.string.browse_genres))
                        .setSubtitle("Browse genres")
                        //.setIconUri(Uri.parse("android.resource://" +
                        //  "com.example.android.uamp/drawable/ic_by_genre"))
                        .build();

                return new MediaBrowserCompat.MediaItem(description,
                        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);

            case (MEDIA_ID_MUSICS_BY_PLAYLIST):
                description = new MediaDescriptionCompat.Builder()
                    .setMediaId(mediaId)//MEDIA_ID_MUSICS_BY_PLAYLIST
                    .setTitle("Playlists") // .setTitle(resources.getString(R.string.browse_genres))
                    .setSubtitle("Browse playlists")
                    //.setIconUri(Uri.parse("android.resource://" +
                    //  "com.example.android.uamp/drawable/ic_by_genre"))
                    .build();

                return new MediaBrowserCompat.MediaItem(description,
                        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);

            case (MEDIA_ID_MUSICS_BY_ALBUM):
                description = new MediaDescriptionCompat.Builder()
                        .setMediaId(mediaId)//MEDIA_ID_MUSICS_BY_ALBUM
                        .setTitle("Albums") // .setTitle(resources.getString(R.string.browse_genres))
                        .setSubtitle("Browse albums")
                        //.setIconUri(Uri.parse("android.resource://" +
                        //  "com.example.android.uamp/drawable/ic_by_genre"))
                        .build();

                return new MediaBrowserCompat.MediaItem(description,
                        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
        }
        return null;
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForGenre(String genre,
                                                                          Resources resources) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(createMediaID(null, MEDIA_ID_MUSICS_BY_GENRE, genre))
                .setTitle(genre)
                .setSubtitle(genre)//resources.getString(R.string.browse_musics_by_genre_subtitle, genre))
                .build();
        return new MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata) {
        // Since mediaMetadata fields are immutable, we need to create a copy, so we
        // can set a hierarchy-aware mediaID. We will need to know the media hierarchy
        // when we get a onPlayFromMusicID call, so we can create the proper queue based
        // on where the music was selected from (by artist, by genre, random, etc)
        String genre = metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
        String hierarchyAwareMediaID = createMediaID(
                metadata.getDescription().getMediaId(), MEDIA_ID_MUSICS_BY_GENRE, genre);
        MediaMetadataCompat copy = new MediaMetadataCompat.Builder(metadata)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, hierarchyAwareMediaID)
                .build();
        return new MediaBrowserCompat.MediaItem(copy.getDescription(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);

    }
}
