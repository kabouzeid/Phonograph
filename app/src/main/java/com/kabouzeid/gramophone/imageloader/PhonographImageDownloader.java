package com.kabouzeid.gramophone.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kabouzeid.gramophone.loader.AlbumSongLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.ImageUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PhonographImageDownloader extends BaseImageDownloader {
    public static final String SCHEME_ALBUM = "album://";
    public static final String SCHEME_SONG = "song://";

    public PhonographImageDownloader(@NonNull Context context) {
        super(context);
    }

    @Nullable
    @Override
    public InputStream getStream(@NonNull String imageUri, Object extra) throws IOException {
        if (imageUri.startsWith(SCHEME_ALBUM)) {
            return getStreamFromAlbum(imageUri);
        }
        if (imageUri.startsWith(SCHEME_SONG)) {
            return getStreamFromSong(imageUri);
        }
        return super.getStream(imageUri, extra);
    }

    @Nullable
    protected InputStream getStreamFromAlbum(@NonNull String imageUri) throws IOException {
        int albumId = Integer.valueOf(imageUri.substring(SCHEME_ALBUM.length()));

        if (PreferenceUtil.getInstance(context).ignoreMediaStoreArtwork()) {
            ArrayList<Song> songs = AlbumSongLoader.getAlbumSongList(context, albumId);
            for (Song song : songs) {
                Bitmap bitmap = ImageUtil.getEmbeddedSongArt(new File(song.data), context);
                if (bitmap != null) {
                    return getBitmapInputStream(bitmap);
                }
            }
            return null;
        }
        return getMediaProviderAlbumArtInputStream(albumId);
    }

    @Nullable
    protected InputStream getStreamFromSong(@NonNull String imageUri) throws IOException {
        String[] data = imageUri.split("#", 2);

        if (PreferenceUtil.getInstance(context).ignoreMediaStoreArtwork()) {
            Bitmap bitmap = ImageUtil.getEmbeddedSongArt(new File(data[1]), context);
            if (bitmap != null) {
                return getBitmapInputStream(bitmap);
            }
            return null;
        }

        int id = Integer.parseInt(data[0].substring(SCHEME_SONG.length()));
        return getMediaProviderAlbumArtInputStream(id);
    }

    @NonNull
    private static ByteArrayInputStream getBitmapInputStream(@NonNull Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        return new ByteArrayInputStream(bos.toByteArray());
    }

    @NonNull
    private InputStream getMediaProviderAlbumArtInputStream(int albumId) throws FileNotFoundException {
        return context.getContentResolver().openInputStream(MusicUtil.getAlbumArtUri(albumId));
    }
}