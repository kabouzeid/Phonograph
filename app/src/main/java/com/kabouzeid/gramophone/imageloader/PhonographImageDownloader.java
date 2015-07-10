package com.kabouzeid.gramophone.imageloader;

import android.content.Context;
import android.graphics.Bitmap;

import com.kabouzeid.gramophone.loader.AlbumSongLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.ImageUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PhonographImageDownloader extends BaseImageDownloader {
    public static final String SCHEME_ALBUM = "album://";
    public static final String SCHEME_SONG = "song://";

    public PhonographImageDownloader(Context context) {
        super(context);
    }

    @Override
    protected InputStream getStreamFromOtherSource(String imageUri, Object extra) throws IOException {
        if (imageUri.startsWith(SCHEME_ALBUM)) {
            return getStreamFromAlbum(imageUri, extra);
        } else if (imageUri.startsWith(SCHEME_SONG)) {
            return getStreamFromSong(imageUri, extra);
        } else {
            return super.getStreamFromOtherSource(imageUri, extra);
        }
    }

    protected InputStream getStreamFromAlbum(String imageUri, Object extra) throws IOException {
        int albumId = Integer.valueOf(imageUri.substring(SCHEME_ALBUM.length()));

        if (PreferenceUtils.getInstance(context).ignoreMediaStoreArtwork()) {
            ArrayList<Song> songs = AlbumSongLoader.getAlbumSongList(context, albumId);
            for (Song song : songs) {
                Bitmap bitmap = ImageUtil.getEmbeddedSongArt(new File(song.data), context);
                if (bitmap != null) {
                    return getBitmapInputStream(bitmap);
                }
            }
            return null;
        }
        return getStream(MusicUtil.getAlbumArtUri(albumId).toString(), extra);
    }

    protected InputStream getStreamFromSong(String imageUri, Object extra) throws IOException {
        String[] data = imageUri.split("#", 2);

        if (PreferenceUtils.getInstance(context).ignoreMediaStoreArtwork()) {
            Bitmap bitmap = ImageUtil.getEmbeddedSongArt(new File(data[1]), context);
            if (bitmap != null) {
                return getBitmapInputStream(bitmap);
            }
            return null;
        }

        int id = Integer.valueOf(data[0].substring(SCHEME_SONG.length()));
        return getStream(MusicUtil.getAlbumArtUri(id).toString(), extra);
    }

    private static ByteArrayInputStream getBitmapInputStream(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        return new ByteArrayInputStream(bos.toByteArray());
    }
}
