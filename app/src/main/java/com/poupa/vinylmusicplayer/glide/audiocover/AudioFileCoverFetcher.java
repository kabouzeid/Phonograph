package com.poupa.vinylmusicplayer.glide.audiocover;

import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AudioFileCoverFetcher implements DataFetcher<InputStream> {
    private final AudioFileCover model;
    private FileInputStream stream;

    public AudioFileCoverFetcher(AudioFileCover model) {
        this.model = model;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        InputStream data;
        try {
            retriever.setDataSource(model.filePath);
            byte[] picture = retriever.getEmbeddedPicture();
            if (picture != null) {
                data = new ByteArrayInputStream(picture);
            } else {
                data = fallback(model.filePath);
            }
            callback.onDataReady(data);
        } catch (FileNotFoundException e) {
            callback.onLoadFailed(e);
        } finally {
            retriever.release();
        }
    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }

    private static final String[] FALLBACKS = {"cover.jpg", "album.jpg", "folder.jpg"};

    private InputStream fallback(String path) throws FileNotFoundException {
        File parent = new File(path).getParentFile();
        for (String fallback : FALLBACKS) {
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
