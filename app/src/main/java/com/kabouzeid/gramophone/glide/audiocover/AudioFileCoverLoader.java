package com.kabouzeid.gramophone.glide.audiocover;

import android.support.annotation.Nullable;

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

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(AudioFileCover audioFileCover, int width, int height, Options options) {
        return new LoadData<>(new ObjectKey(audioFileCover.filePath), new AudioFileCoverFetcher(audioFileCover));
    }

    @Override
    public boolean handles(AudioFileCover audioFileCover) {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<AudioFileCover, InputStream> {
        @Override
        public ModelLoader<AudioFileCover, InputStream> build(MultiModelLoaderFactory multiModelLoaderFactory) {
            return new AudioFileCoverLoader();
        }

        @Override
        public void teardown() {
        }
    }
}

