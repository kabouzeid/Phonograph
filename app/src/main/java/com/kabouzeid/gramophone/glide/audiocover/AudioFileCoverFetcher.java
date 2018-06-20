package com.kabouzeid.gramophone.glide.audiocover;

import android.media.MediaMetadataRetriever;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

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
    public String getId() {
        // makes sure we never ever return null here
        return String.valueOf(model.filePath);
    }

    @Override
    public InputStream loadData(Priority priority) throws Exception {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(model.filePath);
            byte[] picture = retriever.getEmbeddedPicture();
            if (picture != null) {
                return new ByteArrayInputStream(picture);
            } else {
                return fallback(model.filePath);
            }
        } finally {
            retriever.release();
        }
    }

    private static final String[] FALLBACKS = {"cover.jpg", "album.jpg", "folder.jpg", "cover.png", "album.png", "folder.png"};

    private InputStream fallback(String path) throws FileNotFoundException {
        // Method 1: use embedded high resolution album art if there is any
        try {
            Mp3File mp3File = new Mp3File(path);
            if (mp3File.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3File.getId3v2Tag();
                byte[] imageData = id3v2Tag.getAlbumImage();
                if (imageData != null) {
                    return new ByteArrayInputStream(imageData);
                }
            }
        // If there are any exceptions, we ignore them and continue to the other fallback method
        } catch (IOException ignored) {
        } catch (InvalidDataException ignored) {
        } catch (UnsupportedTagException ignored) {
        }

        // Method 2: look for album art in external files
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
