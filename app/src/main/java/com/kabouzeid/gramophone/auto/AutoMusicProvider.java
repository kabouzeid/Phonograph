package com.kabouzeid.gramophone.auto;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;

import com.kabouzeid.appthemehelper.ThemeStore;
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
import com.kabouzeid.gramophone.util.Util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by Beesham Sarendranauth (Beesham)
 */
public class AutoMusicProvider {

    private static final String TAG = AutoMusicProvider.class.getName();

    private static final String BASE_URI = "androidauto://phonograph";
    private static final int PATH_SEGMENT_ID = 0;
    private static final int PATH_SEGMENT_TITLE = 1;
    private static final int PATH_SEGMENT_ARTIST = 2;
    private static final int PATH_SEGMENT_ALBUM_ID = 3;

    private WeakReference<MusicService> mMusicService;

    // Categorized caches for music data
    private ConcurrentMap<Integer, Uri> mMusicListByHistory;
    private ConcurrentMap<Integer, Uri> mMusicListByTopTracks;
    private ConcurrentMap<Integer, Uri> mMusicListByPlaylist;
    private ConcurrentMap<Integer, Uri> mMusicListByAlbum;
    private ConcurrentMap<Integer, Uri> mMusicListByArtist;

    private Uri defaultAlbumArtUri;

    private Context mContext;
    private volatile State mCurrentState = State.NON_INITIALIZED;

    public AutoMusicProvider(Context context) {
        mContext = context;

        mMusicListByHistory = new ConcurrentSkipListMap<>();
        mMusicListByTopTracks = new ConcurrentSkipListMap<>();
        mMusicListByPlaylist = new ConcurrentSkipListMap<>();
        mMusicListByAlbum = new ConcurrentSkipListMap<>();
        mMusicListByArtist = new ConcurrentSkipListMap<>();

        defaultAlbumArtUri = Uri.parse("android.resource://" +
                mContext.getPackageName() + "/drawable/" +
                mContext.getResources().getResourceEntryName(R.drawable.default_album_art));
    }

    public void setMusicService(MusicService service) {
        mMusicService = new WeakReference<>(service);
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

    public Iterable<Uri> getPlaylists() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByPlaylist.values();
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

    private synchronized void retrieveMedia() {
        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;

                buildListsByHistory();
                buildListsByTopTracks();
                buildListsByPlaylist();
                buildListsByAlbum();
                buildListsByArtist();
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

        if (!AutoMediaIDHelper.isBrowseable(mediaId)) {
            return mediaItems;
        }

        switch (mediaId) {
            case AutoMediaIDHelper.MEDIA_ID_ROOT:
                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY, resources.getString(R.string.history_label), R.drawable.ic_access_time_white_24dp));
                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS, resources.getString(R.string.top_tracks_label), R.drawable.ic_trending_up_white_24dp));
                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST, resources.getString(R.string.playlists_label), R.drawable.ic_queue_music_white_24dp));
                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM, resources.getString(R.string.albums_label), R.drawable.ic_album_white_24dp));
                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST, resources.getString(R.string.artists_label), R.drawable.ic_people_white_24dp));
                mediaItems.add(createPlayableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_SHUFFLE, resources.getString(R.string.action_shuffle_all), null));
                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE, resources.getString(R.string.queue_label), R.drawable.ic_playlist_play_white_24dp));
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY:
                for (final Uri uri : getHistory()) {
                    final String albumId = uri.getPathSegments().get(PATH_SEGMENT_ALBUM_ID);
                    final Bitmap bitmap = MusicUtil.getAlbumArtForAlbum(mContext, Integer.parseInt(albumId));
                    mediaItems.add(createPlayableMediaItem(mediaId, uri, uri.getPathSegments().get(PATH_SEGMENT_TITLE), uri.getPathSegments().get(PATH_SEGMENT_ARTIST), bitmap, resources));
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS:
                for (final Uri uri : getTopTracks()) {
                    final String albumId = uri.getPathSegments().get(PATH_SEGMENT_ALBUM_ID);
                    final Bitmap bitmap = MusicUtil.getAlbumArtForAlbum(mContext, Integer.parseInt(albumId));
                    mediaItems.add(createPlayableMediaItem(mediaId, uri, uri.getPathSegments().get(PATH_SEGMENT_TITLE), uri.getPathSegments().get(PATH_SEGMENT_ARTIST), bitmap, resources));
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST:
                for (final Uri uri : getPlaylists()) {
                    mediaItems.add(createPlayableMediaItem(mediaId, uri, uri.getPathSegments().get(PATH_SEGMENT_TITLE), null));
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM:
                for (final Uri uri : getAlbums()) {
                    final String albumId = uri.getPathSegments().get(PATH_SEGMENT_ALBUM_ID);
                    final Bitmap bitmap = MusicUtil.getAlbumArtForAlbum(mContext, Integer.parseInt(albumId));
                    mediaItems.add(createPlayableMediaItem(mediaId, uri, uri.getPathSegments().get(PATH_SEGMENT_TITLE), uri.getPathSegments().get(PATH_SEGMENT_ARTIST), bitmap, resources));
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST:
                for (final Uri uri : getArtists()) {
                    mediaItems.add(createPlayableMediaItem(mediaId, uri, uri.getPathSegments().get(PATH_SEGMENT_ARTIST), null));
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE:
                // TODO: auto scroll to current track, indicate that it's playing
                for (final Uri uri : getQueue()) {
                    final String albumId = uri.getPathSegments().get(PATH_SEGMENT_ALBUM_ID);
                    final Bitmap bitmap = MusicUtil.getAlbumArtForAlbum(mContext, Integer.parseInt(albumId));
                    mediaItems.add(createPlayableMediaItem(mediaId, uri, uri.getPathSegments().get(PATH_SEGMENT_TITLE), uri.getPathSegments().get(PATH_SEGMENT_ARTIST), bitmap, resources));
                }
                break;
        }

        return mediaItems;
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItem(String mediaId, String title, int iconDrawableId) {
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder();
        builder.setMediaId(mediaId)
                .setTitle(title)
                .setIconBitmap(Util.createBitmap(Util.getTintedVectorDrawable(mContext, iconDrawableId, ThemeStore.textColorSecondary(mContext))));

        return new MediaBrowserCompat.MediaItem(builder.build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createPlayableMediaItem(String mediaId, Uri musicSelection,
                                                                 String title, @Nullable String subtitle) {
        return createPlayableMediaItem(mediaId, musicSelection, title, subtitle, null, null);
    }

    private MediaBrowserCompat.MediaItem createPlayableMediaItem(String mediaId, Uri musicSelection,
                                                                 String title, @Nullable String subtitle,
                                                                 @Nullable Bitmap albumArt, @Nullable Resources resources) {
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder();
        builder.setMediaId(AutoMediaIDHelper.createMediaID(musicSelection.getPathSegments().get(PATH_SEGMENT_ID), mediaId))
                .setTitle(title);

        if (subtitle != null) {
            builder.setSubtitle(subtitle);
        }

        if (resources != null) {
            if (albumArt != null) {
                builder.setIconBitmap(albumArt);
            } else {
                builder.setIconUri(defaultAlbumArtUri);
            }
        }

        return new MediaBrowserCompat.MediaItem(builder.build(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    private MediaBrowserCompat.MediaItem createPlayableMediaItem(String mediaId, String title, @Nullable String subtitle) {
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder()
                .setMediaId(mediaId)
                .setTitle(title);

        if (subtitle != null) {
            builder.setSubtitle(subtitle);
        }

        return new MediaBrowserCompat.MediaItem(builder.build(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    private enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }
}
