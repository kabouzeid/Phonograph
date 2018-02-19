package com.poupa.vinylmusicplayer.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.glide.audiocover.AudioFileCover;
import com.poupa.vinylmusicplayer.glide.palette.BitmapPaletteWrapper;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongGlideRequest {

    public static final DiskCacheStrategy DEFAULT_DISK_CACHE_STRATEGY = DiskCacheStrategy.NONE;
    public static final int DEFAULT_ERROR_IMAGE = R.drawable.default_album_art;
    public static final int DEFAULT_ANIMATION = android.R.anim.fade_in;

    public static class Builder {
        final RequestManager requestManager;
        final Song song;
        boolean ignoreMediaStore;

        public static Builder from(@NonNull RequestManager requestManager, Song song) {
            return new Builder(requestManager, song);
        }

        private Builder(@NonNull RequestManager requestManager, Song song) {
            this.requestManager = requestManager;
            this.song = song;
        }

        public PaletteBuilder generatePalette(Context context) {
            return new PaletteBuilder(this, context);
        }

        public BitmapBuilder asBitmap() {
            return new BitmapBuilder(this);
        }

        public Builder checkIgnoreMediaStore(Context context) {
            return ignoreMediaStore(PreferenceUtil.getInstance(context).ignoreMediaStoreArtwork());
        }

        public Builder ignoreMediaStore(boolean ignoreMediaStore) {
            this.ignoreMediaStore = ignoreMediaStore;
            return this;
        }

        public RequestBuilder<Drawable> build() {
            //noinspection unchecked
            return createBaseRequestDrawable(requestManager, song, ignoreMediaStore)
                    .transition(GenericTransitionOptions.with(DEFAULT_ANIMATION))
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                            .error(DEFAULT_ERROR_IMAGE)
                            .signature(createSignature(song)));
        }
    }

    public static class BitmapBuilder {
        private final Builder builder;

        public BitmapBuilder(Builder builder) {
            this.builder = builder;
        }

        public RequestBuilder<Bitmap> build() {
            //noinspection unchecked
            return createBaseRequestBitmap(builder.requestManager, builder.song, builder.ignoreMediaStore)
                    .transition(GenericTransitionOptions.with(DEFAULT_ANIMATION))
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                            .error(DEFAULT_ERROR_IMAGE)
                            .signature(createSignature(builder.song)));
        }
    }

    public static class PaletteBuilder {
        final Context context;
        private final Builder builder;

        public PaletteBuilder(Builder builder, Context context) {
            this.builder = builder;
            this.context = context;
        }

        public RequestBuilder<BitmapPaletteWrapper> buildAsBitmapPaletteWrapper() {
            //noinspection unchecked
            return createBaseRequestBitmapPaletteWrapper(builder.requestManager, builder.song, builder.ignoreMediaStore)
                    //.transition(GenericTransitionOptions.with(new BitmapPaletteTranscoder(context), BitmapPaletteWrapper.class))
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                            .error(DEFAULT_ERROR_IMAGE)
                            .centerCrop()
                            .signature(createSignature(builder.song)));
        }

        public RequestBuilder<BitmapPaletteWrapper> buildAsBitmapPaletteWrapperDontAnimate() {
            //noinspection unchecked
            return createBaseRequestBitmapPaletteWrapper(builder.requestManager, builder.song, builder.ignoreMediaStore)
                    //.transition(GenericTransitionOptions.with(new BitmapPaletteTranscoder(context), BitmapPaletteWrapper.class))
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                            .error(DEFAULT_ERROR_IMAGE)
                            .dontAnimate()
                            .signature(createSignature(builder.song)));
        }
    }

    public static RequestBuilder<Bitmap> createBaseRequestBitmap(RequestManager requestManager, Song song, boolean ignoreMediaStore) {
        if (ignoreMediaStore) {
            return requestManager.asBitmap().load(new AudioFileCover(song.data));
        } else {
            return requestManager.asBitmap().load(MusicUtil.getMediaStoreAlbumCoverUri(song.albumId));
        }
    }

    public static RequestBuilder<BitmapPaletteWrapper> createBaseRequestBitmapPaletteWrapper(RequestManager requestManager, Song song, boolean ignoreMediaStore) {
        if (ignoreMediaStore) {
            return requestManager.as(BitmapPaletteWrapper.class).load(new AudioFileCover(song.data));
        } else {
            return requestManager.as(BitmapPaletteWrapper.class).load(MusicUtil.getMediaStoreAlbumCoverUri(song.albumId)).listener(new RequestListener() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                    // Log the GlideException here (locally or with a remote logging framework):
                    Log.e("Glide", "Load failed SongGlideRequest createBaseRequestBitmapPaletteWrapper else, " +
                            "trying to load:"+MusicUtil.getMediaStoreAlbumCoverUri(song.albumId), e);

                    // You can also log the individual causes:
                    for (Throwable t : e.getRootCauses()) {
                        Log.e("Glide", "Caused by SongGlideRequest createBaseRequestBitmapPaletteWrapper else", t);
                    }
                    // Or, to log all root causes locally, you can use the built in helper method:
                    e.logRootCauses("Glide");

                    return false; // Allow calling onLoadFailed on the Target.
                }

                @Override
                public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            });
        }
    }

    public static RequestBuilder<Drawable> createBaseRequestDrawable(RequestManager requestManager, Song song, boolean ignoreMediaStore) {
        if (ignoreMediaStore) {
            return requestManager.load(new AudioFileCover(song.data));
        } else {
            return requestManager.load(MusicUtil.getMediaStoreAlbumCoverUri(song.albumId));
        }
    }

    public static Key createSignature(Song song) {
        return new MediaStoreSignature("", song.dateModified, 0);
    }
}
