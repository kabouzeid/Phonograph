package com.poupa.vinylmusicplayer.glide.audiocover;

import android.support.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public class AudioFileCoverLoader implements ModelLoader<AudioFileCover, InputStream> {

    @Override
    public LoadData<InputStream> buildLoadData(@NonNull AudioFileCover model, int width, int height,
                                               @NonNull Options options) {
        return new LoadData<>(new ObjectKey(model.filePath), new AudioFileCoverFetcher(model));
    }

    @Override
    public boolean handles(@NonNull AudioFileCover model) {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<AudioFileCover, InputStream> {
        @Override
        @NonNull
        public ModelLoader<AudioFileCover, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new AudioFileCoverLoader();
        }

        @Override
        public void teardown() {
        }
    }
}

