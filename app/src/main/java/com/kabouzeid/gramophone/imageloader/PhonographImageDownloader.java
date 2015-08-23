package com.kabouzeid.gramophone.imageloader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kabouzeid.gramophone.lastfm.rest.LastFMRestClient;
import com.kabouzeid.gramophone.lastfm.rest.model.artistinfo.ArtistInfo;
import com.kabouzeid.gramophone.loader.AlbumSongLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.LastFMUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.images.Artwork;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PhonographImageDownloader extends BaseImageDownloader {
    public static final String SCHEME_ALBUM = "album://";
    public static final String SCHEME_SONG = "song://";
    public static final String SCHEME_ARTIST = Scheme.HTTP.wrap("artist://");

    private final LastFMRestClient lastFMRestClient;

    public PhonographImageDownloader(@NonNull Context context) {
        super(context);
        lastFMRestClient = new LastFMRestClient(context);
    }

    @Nullable
    @Override
    public InputStream getStream(@NonNull String imageUri, Object extra) throws IOException {
        if (imageUri.toLowerCase(Locale.US).startsWith(SCHEME_ALBUM)) {
            return getStreamFromAlbum(imageUri);
        }
        if (imageUri.toLowerCase(Locale.US).startsWith(SCHEME_SONG)) {
            return getStreamFromSong(imageUri);
        }
        if (imageUri.toLowerCase(Locale.US).startsWith(SCHEME_ARTIST)) {
            return getStreamFromArtist(imageUri, extra);
        }
        return super.getStream(imageUri, extra);
    }

    protected InputStream getStreamFromArtist(@NonNull String imageUri, @NonNull Object extra) throws IOException {
        String[] data = imageUri.substring(SCHEME_ARTIST.length()).split("#", 2);
        String artistName = data[1];

        if (MusicUtil.isArtistNameUnknown(artistName)) {
            return super.getStream("", extra);
        }

        ArtistInfo artistInfo = lastFMRestClient.getApiService().getArtistInfo(artistName, data[0].equals("") ? null : data[0]);
        return super.getStream(LastFMUtil.getLargestArtistImageUrl(artistInfo.getArtist().getImage()), extra);
    }

    @Nullable
    protected InputStream getStreamFromAlbum(@NonNull String imageUri) throws IOException {
        int albumId = Integer.valueOf(imageUri.substring(SCHEME_ALBUM.length()));

        if (PreferenceUtil.getInstance(context).ignoreMediaStoreArtwork()) {
            ArrayList<Song> songs = AlbumSongLoader.getAlbumSongList(context, albumId);
            for (Song song : songs) {
                byte[] albumCover = getAlbumCoverBinaryData(new File(song.data));
                if (albumCover != null) {
                    return new ByteArrayInputStream(albumCover);
                }
            }
        }
        return getMediaProviderAlbumArtInputStream(albumId);
    }

    @Nullable
    protected InputStream getStreamFromSong(@NonNull String imageUri) throws IOException {
        String[] data = imageUri.substring(SCHEME_SONG.length()).split("#", 2);

        if (PreferenceUtil.getInstance(context).ignoreMediaStoreArtwork()) {
            byte[] albumCover = getAlbumCoverBinaryData(new File(data[1]));
            if (albumCover != null) {
                return new ByteArrayInputStream(albumCover);
            }
        }

        int id = Integer.parseInt(data[0]);
        return getMediaProviderAlbumArtInputStream(id);
    }

    @Nullable
    private static byte[] getAlbumCoverBinaryData(File song) {
        try {
            AudioFile audioFile = AudioFileIO.read(song);
            Artwork artwork = audioFile.getTagOrCreateAndSetDefault().getFirstArtwork();
            if (artwork != null) {
                return artwork.getBinaryData();
            }
        } catch (@NonNull Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    private InputStream getMediaProviderAlbumArtInputStream(int albumId) throws
            FileNotFoundException {
        return context.getContentResolver().openInputStream(MusicUtil.getAlbumArtUri(albumId));
    }
}