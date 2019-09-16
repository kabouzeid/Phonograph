package com.kabouzeid.gramophone.glide.audiocover;

import androidx.annotation.Nullable;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Karim Abou Zeid (kabouzeid), modified by Christians Martínez Alvarado (mardous)
 */
public class AudioFileCoverFallback {
    public static final String[] FALLBACKS = {"cover.jpg", "album.jpg", "folder.jpg", "cover.png", "album.png", "folder.png"};

    @Nullable
    public static InputStream getFallback(String path, boolean useFolderFallback) {
        // Method 1: use embedded high resolution album art if there is any
        InputStream stream = getEmbeddedFallback(path);
        if (stream == null && useFolderFallback) {
            // Method 2: look for album art in external files
            stream = getFolderFallback(path);
            // if even is null, we can't do much about it
        }
        return null;
    }

    @Nullable
    public static InputStream getEmbeddedFallback(String path) {
        try {
            MP3File mp3File = new MP3File(path);
            if (mp3File.hasID3v2Tag()) {
                Artwork art = mp3File.getTag().getFirstArtwork();
                if (art != null) {
                    byte[] imageData = art.getBinaryData();
                    return new ByteArrayInputStream(imageData);
                }
            }
            // If there are any exceptions, we ignore them and continue to the other fallback method
        } catch (ReadOnlyFileException ignored) {
        } catch (InvalidAudioFrameException ignored) {
        } catch (TagException ignored) {
        } catch (IOException ignored) {
        }
        return null;
    }

    @Nullable
    public static InputStream getFolderFallback(String path) {
        try {
            final File parent = new File(path).getParentFile();
            for (String fallback : FALLBACKS) {
                File cover = new File(parent, fallback);
                if (cover.exists()) {
                    return new FileInputStream(cover);
                }
            }
        } catch (FileNotFoundException ignored) {}
        return null;
    }
}
