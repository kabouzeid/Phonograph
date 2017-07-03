package com.kabouzeid.gramophone.modelAndroidAuto;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.util.Log;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.loader.AlbumLoader;
import com.kabouzeid.gramophone.loader.PlaylistLoader;
import com.kabouzeid.gramophone.loader.PlaylistSongLoader;
import com.kabouzeid.gramophone.loader.TopAndRecentlyPlayedTracksLoader;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.PlaylistSong;
import com.kabouzeid.gramophone.model.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.kabouzeid.gramophone.modelAndroidAuto.MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM;
import static com.kabouzeid.gramophone.modelAndroidAuto.MediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY;
import static com.kabouzeid.gramophone.modelAndroidAuto.MediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST;
import static com.kabouzeid.gramophone.modelAndroidAuto.MediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS;
import static com.kabouzeid.gramophone.modelAndroidAuto.MediaIDHelper.MEDIA_ID_ROOT;
import static com.kabouzeid.gramophone.modelAndroidAuto.MediaIDHelper.createMediaID;


/**
 * Created by Beesham on 3/28/2017.
 */
public class AutoMusicProvider {

    private static String TAG = AutoMusicProvider.class.getName();
    private static final String BASE_URI = "androidauto://phonograph";
    private static final int PATH_SEGMENT_TITLE = 0;
    private static final int PATH_SEGMENT_ID = 1;
    private static final int PATH_SEGMENT_ARTIST = 2;

    private MusicProviderSource mSource;

    // Categorized caches for music data
    private ConcurrentMap<Uri, List<Song>> mMusicListByAlbum;
    private ConcurrentMap<Uri, List<PlaylistSong>> mMusicListByPlaylist;
    private ConcurrentMap<Uri, Song> mMusicListByHistory;
    private ConcurrentMap<Uri, Song> mMusicListByTopTracks;

    private Context mContext;
    private volatile State mCurrentState = State.NON_INITIALIZED;

    public AutoMusicProvider(Context context) {
        this(new AutoMusicSource(context));
        mContext = context;
    }

    public AutoMusicProvider(AutoMusicSource source) {
        mSource = source;

        mMusicListByAlbum = new ConcurrentHashMap<>();
        mMusicListByPlaylist = new ConcurrentHashMap<>();
        mMusicListByHistory = new ConcurrentHashMap<>();
        mMusicListByTopTracks = new ConcurrentHashMap<>();
    }

    public Iterable<Uri> getAlbums() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByAlbum.keySet();
    }

    public Iterable<Uri> getPlaylists() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByPlaylist.keySet();
    }

    public Iterable<Uri> getHistory() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByHistory.keySet();
    }

    public Iterable<Uri> getTopTracks() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByTopTracks.keySet();
    }

    public boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }

    /**
     * Get the list of music tracks from a server and caches the track information
     * for future reference, keying tracks by musicId
     */
    public void retrieveMediaAsync(final Callback callback) {
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

    private synchronized void buildListsByAlbum() {
        ConcurrentMap<Uri, List<Song>> newMusicListByAlbum = new ConcurrentHashMap<>();

        for (Album a : AlbumLoader.getAllAlbums(mContext)) {
            Uri.Builder albumData = Uri.parse(BASE_URI).buildUpon();
            albumData.appendPath(a.getTitle())
                    .appendPath(String.valueOf(a.getId()))
                    .appendPath(a.getArtistName());
            newMusicListByAlbum.putIfAbsent(albumData.build(), a.songs);
        }
        mMusicListByAlbum = newMusicListByAlbum;
    }

    private synchronized void buildListsByPlaylist() {
        ConcurrentMap<Uri, List<PlaylistSong>> newMusicListByPlaylist = new ConcurrentHashMap<>();

        for (Playlist p : PlaylistLoader.getAllPlaylists(mContext)) {
            Uri.Builder playlistData = Uri.parse(BASE_URI).buildUpon();
            playlistData.appendPath(p.name);
            newMusicListByPlaylist.putIfAbsent(playlistData.build(), PlaylistSongLoader.getPlaylistSongList(mContext, p.id));
        }

        mMusicListByPlaylist = newMusicListByPlaylist;
    }

    private synchronized void buildListsByHistory() {
        ConcurrentMap<Uri, Song> newMusicListByHistory = new ConcurrentHashMap<>();

        for (Song s : TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(mContext)) {
            String songName = s.title;
            Uri.Builder topTracksData = Uri.parse(BASE_URI).buildUpon();
            topTracksData.appendPath(songName)
                    .appendPath(String.valueOf(s.id))
                    .appendPath(s.artistName);
            newMusicListByHistory.put(topTracksData.build(), s);
        }

        mMusicListByHistory = newMusicListByHistory;
    }

    private synchronized void buildListsByTopTracks() {
        ConcurrentMap<Uri, Song> newMusicListByTopTracks = new ConcurrentHashMap<>();

        for (Song s : TopAndRecentlyPlayedTracksLoader.getTopTracks(mContext)) {
            String songName = s.title;
            Uri.Builder topTracksData = Uri.parse(BASE_URI).buildUpon();
            topTracksData.appendPath(songName)
                    .appendPath(String.valueOf(s.id))
                    .appendPath(s.artistName);
            newMusicListByTopTracks.put(topTracksData.build(), s);
        }

        mMusicListByTopTracks = newMusicListByTopTracks;
    }

    private synchronized void retrieveMedia() {
        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;

                buildListsByAlbum();
                buildListsByPlaylist();
                buildListsByHistory();
                buildListsByTopTracks();
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

        switch (mediaId) {
            case MEDIA_ID_ROOT:
                mediaItems.add(createBrowsableMediaItemForRoot(MEDIA_ID_MUSICS_BY_ALBUM, resources));
                mediaItems.add(createBrowsableMediaItemForRoot(MEDIA_ID_MUSICS_BY_PLAYLIST, resources));
                mediaItems.add(createBrowsableMediaItemForRoot(MEDIA_ID_MUSICS_BY_HISTORY, resources));
                mediaItems.add(createBrowsableMediaItemForRoot(MEDIA_ID_MUSICS_BY_TOP_TRACKS, resources));
                break;

            case MEDIA_ID_MUSICS_BY_ALBUM:
                for (Uri album : getAlbums()) {
                    String albumId = album.getPathSegments().get(PATH_SEGMENT_ID);
                    // TODO: Loading image takes too long, need to find better, faster way
                    //Bitmap bitmap = MusicUtil.getAlbumArtForAlbum(mContext, Integer.parseInt(albumId));
                    mediaItems.add(createBrowsableMediaItem(mediaId, album, null, resources));
                }
                break;

            case MEDIA_ID_MUSICS_BY_PLAYLIST:
                for (Uri playlist : getPlaylists()) {
                    mediaItems.add(createBrowsableMediaItem(mediaId, playlist, null, resources));
                }
                break;

            case MEDIA_ID_MUSICS_BY_HISTORY:
                for (Uri song : getHistory()) {
                    mediaItems.add(createBrowsableMediaItem(mediaId, song, null, resources));
                }
                break;

            case MEDIA_ID_MUSICS_BY_TOP_TRACKS:
                for (Uri song : getTopTracks()) {
                    mediaItems.add(createBrowsableMediaItem(mediaId, song, null, resources));
                }
                break;
        }

        return mediaItems;
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForRoot(String mediaId, Resources resources) {
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder();

        switch (mediaId) {
            case MEDIA_ID_MUSICS_BY_ALBUM:
                builder.setMediaId(mediaId)
                        .setTitle(resources.getString(R.string.albums_label))
                        .setIconUri(Uri.parse("android.resource://" +
                                mContext.getPackageName() + "/drawable/" +
                                resources.getResourceEntryName(R.drawable.ic_album_black_24dp)));
                break;


            case MEDIA_ID_MUSICS_BY_PLAYLIST:
                builder.setMediaId(mediaId)
                        .setTitle(resources.getString(R.string.playlists_label))
                        .setIconUri(Uri.parse("android.resource://" +
                                mContext.getPackageName() + "/drawable/" +
                                resources.getResourceEntryName(R.drawable.ic_playlist_play_black_24dp)));
                break;

            case MEDIA_ID_MUSICS_BY_HISTORY:
                builder.setMediaId(mediaId)
                        .setTitle(resources.getString(R.string.history_label))
                        .setIconUri(Uri.parse("android.resource://" +
                                mContext.getPackageName() + "/drawable/" +
                                resources.getResourceEntryName(R.drawable.ic_access_time_black_24dp)));
                break;

            case MEDIA_ID_MUSICS_BY_TOP_TRACKS:
                builder.setMediaId(mediaId)
                        .setTitle(resources.getString(R.string.top_tracks_label))
                        .setIconUri(Uri.parse("android.resource://" +
                                mContext.getPackageName() + "/drawable/" +
                                resources.getResourceEntryName(R.drawable.ic_trending_up_black_24dp)));
                break;
        }

        return new MediaBrowserCompat.MediaItem(builder.build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItem(String mediaId, Uri musicSelection, @Nullable Bitmap albumArt,
                                                                  Resources resources) {
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder();

        switch (mediaId) {
            case MEDIA_ID_MUSICS_BY_PLAYLIST:
                builder.setMediaId(createMediaID(null, MEDIA_ID_MUSICS_BY_PLAYLIST, musicSelection.getPathSegments().get(PATH_SEGMENT_TITLE)))
                        .setTitle(musicSelection.getPathSegments().get(PATH_SEGMENT_TITLE))
                        .setIconUri(Uri.parse("android.resource://" +
                                mContext.getPackageName() + "/drawable/" +
                                resources.getResourceEntryName(R.drawable.ic_playlist_play_black_24dp)));
                break;

            case MEDIA_ID_MUSICS_BY_ALBUM:
            case MEDIA_ID_MUSICS_BY_HISTORY:
            case MEDIA_ID_MUSICS_BY_TOP_TRACKS:
                builder.setMediaId(createMediaID(null, mediaId, musicSelection.getPathSegments().get(PATH_SEGMENT_TITLE)))
                        .setTitle(musicSelection.getPathSegments().get(PATH_SEGMENT_TITLE))
                        .setSubtitle(musicSelection.getPathSegments().get(PATH_SEGMENT_ARTIST));

                if (albumArt != null) {
                    builder.setIconBitmap(albumArt);
                } else {
                    builder.setIconUri(Uri.parse("android.resource://" +
                            mContext.getPackageName() + "/drawable/" +
                            resources.getResourceEntryName(R.drawable.default_album_art)));
                }
                break;
        }

        return new MediaBrowserCompat.MediaItem(builder.build(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }
}
