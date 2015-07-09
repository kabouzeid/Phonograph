package com.kabouzeid.gramophone.imageloader;

import android.content.Context;

import com.kabouzeid.gramophone.util.MusicUtil;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.IOException;
import java.io.InputStream;

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
            try {
                int id = Integer.valueOf(imageUri.substring(SCHEME_ALBUM.length()));
                return getStream(MusicUtil.getAlbumArtUri(id).toString(), extra);
            } catch (NumberFormatException e) {
                return super.getStreamFromOtherSource(imageUri, extra);
            }
        } else {
            return super.getStreamFromOtherSource(imageUri, extra);
        }
    }
}
