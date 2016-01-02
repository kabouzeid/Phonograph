package com.kabouzeid.gramophone.glide.audiocover;

import android.content.Context;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.MusicUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AudioFileCoverFetcher implements DataFetcher<InputStream> {
    private final Song model;
    private FileInputStream stream;
    private Context context;

    public AudioFileCoverFetcher(Context context, Song model) {
        this.context = context;
        this.model = model;
    }

    @Override
    public String getId() {
        return model.data;
    }

    @Override
    public InputStream loadData(Priority priority) throws Exception {
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        try {
//            retriever.setDataSource(model.data);
//            byte[] picture = retriever.getEmbeddedPicture();
//            if (picture != null) {
//                return new ByteArrayInputStream(picture);
//            } else {
//                return fallback(model.data);
//            }
//        } finally {
//            retriever.release();
//        }
        return context.getContentResolver().openInputStream(MusicUtil.getAlbumArtUri(model.albumId));
    }

    private static final String[] FALLBACKS = {"cover.jpg", "album.jpg", "folder.jpg"};

    private InputStream fallback(String path) throws FileNotFoundException {
        File parent = new File(path).getParentFile();
        for (String fallback : FALLBACKS) {
            // TODO make it smarter by enumerating folder contents and filtering for files
            // example algorithm for that: http://askubuntu.com/questions/123612/how-do-i-set-album-artwork
            File cover = new File(parent, fallback);
            if (cover.exists()) {
                return stream = new FileInputStream(cover);
            }
        }
        return null;
    }

    @Override
    public void cleanup() {
        // already cleaned up in loadData and ByteArrayInputStream will be GC'd
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignore) {
                // can't do much about it
            }
        }
    }

    @Override
    public void cancel() {
        // cannot cancel
    }
}
