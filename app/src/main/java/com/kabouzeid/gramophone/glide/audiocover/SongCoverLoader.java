package com.kabouzeid.gramophone.glide.audiocover;

import android.content.Context;

import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.stream.StreamModelLoader;
import com.kabouzeid.gramophone.model.Song;

import java.io.InputStream;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public class SongCoverLoader implements StreamModelLoader<Song> {
    private Context context;

    public SongCoverLoader(Context context) {
        this.context = context;
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(Song model, int width, int height) {
        return new AudioFileCoverFetcher(context, model);
    }

    public static class Factory implements ModelLoaderFactory<Song, InputStream> {
        @Override
        public ModelLoader<Song, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new SongCoverLoader(context);
        }

        @Override
        public void teardown() {
        }
    }
}

