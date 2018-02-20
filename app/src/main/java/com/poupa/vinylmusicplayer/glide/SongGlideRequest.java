package com.poupa.vinylmusicplayer.glide;

import android.support.annotation.NonNull;

import com.poupa.vinylmusicplayer.glide.audiocover.AudioFileCover;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongGlideRequest {

    private GlideRequest requestManager;
    private final Song song;
    private boolean ignoreMediaStore;

    public static SongGlideRequest from(@NonNull GlideRequest requestManager, Song song) {
        return new SongGlideRequest(requestManager, song);
    }

    private SongGlideRequest(@NonNull GlideRequest requestManager, Song song) {
        this.requestManager = requestManager;
        this.song = song;
    }

    public SongGlideRequest checkIgnoreMediaStore() {
        return ignoreMediaStore(PreferenceUtil.getInstance().ignoreMediaStoreArtwork());
    }

    private SongGlideRequest ignoreMediaStore(boolean ignoreMediaStore) {
        this.ignoreMediaStore = ignoreMediaStore;
        return this;
    }

    public GlideRequest build() {
        if (ignoreMediaStore) {
            requestManager = requestManager.load(new AudioFileCover(song.data));
        } else {
            requestManager = requestManager.load(MusicUtil.getMediaStoreAlbumCoverUri(song.albumId));
        }

        //noinspection unchecked
        return requestManager.transition(VinylGlideExtension.getDefaultTransition()).songOptions(song);
    }
}
