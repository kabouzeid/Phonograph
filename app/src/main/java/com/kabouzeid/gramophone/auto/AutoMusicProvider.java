package com.kabouzeid.gramophone.auto;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.loader.AlbumLoader;
import com.kabouzeid.gramophone.loader.ArtistLoader;
import com.kabouzeid.gramophone.loader.PlaylistLoader;
import com.kabouzeid.gramophone.loader.TopAndRecentlyPlayedTracksLoader;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.provider.MusicPlaybackQueueStore;
import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.PhonographColorUtil;
import com.kabouzeid.gramophone.util.Util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by Beesham on 3/28/2017.
 */
public class AutoMusicProvider {

    public static final int PATH_SEGMENT_ID = 0;
    public static final int PATH_SEGMENT_TITLE = 1;
    public static final int PATH_SEGMENT_ARTIST = 2;
    public static final int PATH_SEGMENT_ALBUM_ID = 3;

    private static String TAG = AutoMusicProvider.class.getName();
    private static final String BASE_URI = "androidauto://phonograph";

    private MusicProviderSource mSource;
    private WeakReference<MusicService> mMusicService;

    // Categorized caches for music data
    private ConcurrentMap<Integer, Uri> mMusicListByAlbum;
    private ConcurrentMap<Integer, Uri> mMusicListByArtist;
    private ConcurrentMap<Integer, Uri> mMusicListByPlaylist;
    private ConcurrentMap<Integer, Uri> mMusicListByHistory;
    private ConcurrentMap<Integer, Uri> mMusicListByTopTracks;

    private Context mContext;
    private volatile State mCurrentState = State.NON_INITIALIZED;

    public AutoMusicProvider(Context context) {
        this(new AutoMusicSource(context));
        mContext = context;
    }

    public AutoMusicProvider(AutoMusicSource source) {
        mSource = source;

        mMusicListByAlbum = new ConcurrentSkipListMap<>();
        mMusicListByArtist= new ConcurrentSkipListMap<>();
        mMusicListByPlaylist = new ConcurrentSkipListMap<>();
        mMusicListByHistory = new ConcurrentSkipListMap<>();
        mMusicListByTopTracks = new ConcurrentSkipListMap<>();
    }

    public void setMusicService(MusicService service) {
        mMusicService = new WeakReference<>(service);
    }

    public Iterable<Uri> getAlbums() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByAlbum.values();
    }

    public Iterable<Uri> getArtists() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByArtist.values();
    }

    public Iterable<Uri> getPlaylists() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByPlaylist.values();
    }

    public Iterable<Uri> getHistory() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByHistory.values();
    }

    public Iterable<Uri> getTopTracks() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByTopTracks.values();
    }

    public Iterable<Uri> getQueue() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }

        // Re-built every time since the queue updates often
        ConcurrentMap<Integer, Uri> queueList = new ConcurrentSkipListMap<>();

        final MusicService service = mMusicService.get();
        if (service != null) {
            final List<Song> songs = MusicPlaybackQueueStore.getInstance(service).getSavedOriginalPlayingQueue();
            for (int i = 0; i < songs.size(); i++) {
                final Song s = songs.get(i);
                Uri.Builder topTracksData = Uri.parse(BASE_URI).buildUpon();
                topTracksData.appendPath(String.valueOf(s.id))
                        .appendPath(s.title)
                        .appendPath(s.artistName)
                        .appendPath(String.valueOf(s.albumId));
                queueList.putIfAbsent(i, topTracksData.build());
            }
        }

        return queueList.values();
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
        ConcurrentMap<Integer, Uri> newMusicListByAlbum = new ConcurrentSkipListMap<>();

        final List<Album> albums = AlbumLoader.getAllAlbums(mContext);
        for (int i = 0; i < albums.size(); i++) {
            final Album a = albums.get(i);
            Uri.Builder albumData = Uri.parse(BASE_URI).buildUpon();
            albumData.appendPath(String.valueOf(a.getId()))
                    .appendPath(a.getTitle())
                    .appendPath(a.getArtistName())
                    .appendPath(String.valueOf(a.getId()));
            newMusicListByAlbum.putIfAbsent(i, albumData.build());
        }

        mMusicListByAlbum = newMusicListByAlbum;
    }

    private synchronized void buildListsByArtist() {
        ConcurrentMap<Integer, Uri> newMusicListByArtist = new ConcurrentSkipListMap<>();

        final List<Artist> artists = ArtistLoader.getAllArtists(mContext);
        for (int i = 0; i < artists.size(); i++) {
            final Artist a = artists.get(i);
            Uri.Builder artistData = Uri.parse(BASE_URI).buildUpon();
            artistData.appendPath(String.valueOf(a.getId()))
                    .appendPath(a.getName())
                    .appendPath(a.getName());
            newMusicListByArtist.putIfAbsent(i, artistData.build());
        }

        mMusicListByArtist = newMusicListByArtist;
    }

    private synchronized void buildListsByPlaylist() {
        ConcurrentMap<Integer, Uri> newMusicListByPlaylist = new ConcurrentSkipListMap<>();

        final List<Playlist> playlists = PlaylistLoader.getAllPlaylists(mContext);
        for (int i = 0; i < playlists.size(); i++) {
            final Playlist p = playlists.get(i);
            Uri.Builder playlistData = Uri.parse(BASE_URI).buildUpon();
            playlistData.appendPath(String.valueOf(p.id))
                    .appendPath(p.name);
            newMusicListByPlaylist.putIfAbsent(i, playlistData.build());
        }

        mMusicListByPlaylist = newMusicListByPlaylist;
    }

    private synchronized void buildListsByHistory() {
        ConcurrentMap<Integer, Uri> newMusicListByHistory = new ConcurrentSkipListMap<>();

        final List<Song> songs = TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(mContext);
        for (int i = 0; i < songs.size(); i++) {
            final Song s = songs.get(i);
            Uri.Builder topTracksData = Uri.parse(BASE_URI).buildUpon();
            topTracksData.appendPath(String.valueOf(s.id))
                    .appendPath(s.title)
                    .appendPath(s.artistName)
                    .appendPath(String.valueOf(s.albumId));
            newMusicListByHistory.putIfAbsent(i, topTracksData.build());
        }

        mMusicListByHistory = newMusicListByHistory;
    }

    private synchronized void buildListsByTopTracks() {
        ConcurrentMap<Integer, Uri> newMusicListByTopTracks = new ConcurrentHashMap<>();

        final List<Song> songs = TopAndRecentlyPlayedTracksLoader.getTopTracks(mContext);
        for (int i = 0; i < songs.size(); i++) {
            final Song s = songs.get(i);
            Uri.Builder topTracksData = Uri.parse(BASE_URI).buildUpon();
            topTracksData.appendPath(String.valueOf(s.id))
                    .appendPath(s.title)
                    .appendPath(s.artistName)
                    .appendPath(String.valueOf(s.albumId));
            newMusicListByTopTracks.putIfAbsent(i, topTracksData.build());
        }

        mMusicListByTopTracks = newMusicListByTopTracks;
    }

    private synchronized void retrieveMedia() {
        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;

                buildListsByAlbum();
                buildListsByArtist();
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
            case MediaIDHelper.MEDIA_ID_ROOT:
                mediaItems.add(createBrowsableMediaItemForRoot(MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM, resources));
                mediaItems.add(createBrowsableMediaItemForRoot(MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST, resources));
                mediaItems.add(createBrowsableMediaItemForRoot(MediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST, resources));
                mediaItems.add(createBrowsableMediaItemForRoot(MediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY, resources));
                mediaItems.add(createBrowsableMediaItemForRoot(MediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS, resources));
                mediaItems.add(createBrowsableMediaItemForRoot(MediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE, resources));
                break;

            case MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM:
                for (final Uri album : getAlbums()) {
                    final String albumId = album.getPathSegments().get(PATH_SEGMENT_ALBUM_ID);
                    final Bitmap bitmap = MusicUtil.getAlbumArtForAlbum(mContext, Integer.parseInt(albumId));
                    mediaItems.add(createBrowsableMediaItem(mediaId, album, bitmap, resources));
                }
                break;

            case MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST:
                for (Uri artist : getArtists()) {
                    mediaItems.add(createBrowsableMediaItem(mediaId, artist, null, resources));
                }
                break;

            case MediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST:
                for (Uri playlist : getPlaylists()) {
                    mediaItems.add(createBrowsableMediaItem(mediaId, playlist, null, resources));
                }
                break;

            case MediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY:
                for (Uri song : getHistory()) {
                    final String albumId = song.getPathSegments().get(PATH_SEGMENT_ALBUM_ID);
                    final Bitmap bitmap = MusicUtil.getAlbumArtForAlbum(mContext, Integer.parseInt(albumId));
                    mediaItems.add(createBrowsableMediaItem(mediaId, song, bitmap, resources));
                }
                break;

            case MediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS:
                for (Uri song : getTopTracks()) {
                    final String albumId = song.getPathSegments().get(PATH_SEGMENT_ALBUM_ID);
                    final Bitmap bitmap = MusicUtil.getAlbumArtForAlbum(mContext, Integer.parseInt(albumId));
                    mediaItems.add(createBrowsableMediaItem(mediaId, song, bitmap, resources));
                }
                break;

            case MediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE:
                // TODO: auto scroll to current track, indicate that it's playing
                for (Uri song : getQueue()) {
                    final String albumId = song.getPathSegments().get(PATH_SEGMENT_ALBUM_ID);
                    final Bitmap bitmap = MusicUtil.getAlbumArtForAlbum(mContext, Integer.parseInt(albumId));
                    mediaItems.add(createBrowsableMediaItem(mediaId, song, bitmap, resources));
                }
                break;
        }

        return mediaItems;
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForRoot(String mediaId, Resources resources) {
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder();
        builder.setMediaId(mediaId);

        switch (mediaId) {
            case MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM:
                builder.setTitle(resources.getString(R.string.albums_label))
                        .setIconBitmap(Util.createBitmap(Util.getTintedVectorDrawable(mContext, R.drawable.ic_album_white_24dp, PhonographColorUtil.getColorById(mContext, android.R.color.black))));
                break;

            case MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST:
                builder.setTitle(resources.getString(R.string.artists_label))
                        .setIconBitmap(Util.createBitmap(Util.getTintedVectorDrawable(mContext, R.drawable.ic_people_white_24dp, PhonographColorUtil.getColorById(mContext, android.R.color.black))));
                break;

            case MediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST:
                builder.setTitle(resources.getString(R.string.playlists_label))
                        .setIconBitmap(Util.createBitmap(Util.getTintedVectorDrawable(mContext, R.drawable.ic_queue_music_white_24dp, PhonographColorUtil.getColorById(mContext, android.R.color.black))));
                break;

            case MediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY:
                builder.setTitle(resources.getString(R.string.history_label))
                        .setIconBitmap(Util.createBitmap(Util.getTintedVectorDrawable(mContext, R.drawable.ic_access_time_white_24dp, PhonographColorUtil.getColorById(mContext, android.R.color.black))));
                break;

            case MediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS:
                builder.setTitle(resources.getString(R.string.top_tracks_label))
                        .setIconBitmap(Util.createBitmap(Util.getTintedVectorDrawable(mContext, R.drawable.ic_trending_up_white_24dp, PhonographColorUtil.getColorById(mContext, android.R.color.black))));
                break;

            case MediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE:
                builder.setTitle(resources.getString(R.string.queue_label))
                        .setIconBitmap(Util.createBitmap(Util.getTintedVectorDrawable(mContext, R.drawable.ic_playlist_play_white_24dp, PhonographColorUtil.getColorById(mContext, android.R.color.black))));
                break;
        }

        return new MediaBrowserCompat.MediaItem(builder.build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItem(String mediaId, Uri musicSelection, @Nullable Bitmap albumArt,
                                                                  Resources resources) {
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder();
        builder.setMediaId(MediaIDHelper.createMediaID(null, mediaId, musicSelection.getPathSegments().get(PATH_SEGMENT_ID)));

        final String title = musicSelection.getPathSegments().get(PATH_SEGMENT_TITLE);

        switch (mediaId) {
            case MediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST:
                final int playlistIcon = MusicUtil.isFavoritePlaylist(mContext, title) ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_queue_music_white_24dp;
                builder.setTitle(title)
                        .setIconBitmap(Util.createBitmap(Util.getTintedVectorDrawable(mContext, playlistIcon, PhonographColorUtil.getColorById(mContext, android.R.color.black))));
                break;

            case MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST:
                builder.setTitle(musicSelection.getPathSegments().get(PATH_SEGMENT_ARTIST))
                        .setIconUri(Uri.parse("android.resource://" +
                                mContext.getPackageName() + "/drawable/" +
                                resources.getResourceEntryName(R.drawable.default_artist_image)));
                break;

            case MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM:
            case MediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY:
            case MediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS:
            case MediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE:
                builder.setTitle(title)
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
