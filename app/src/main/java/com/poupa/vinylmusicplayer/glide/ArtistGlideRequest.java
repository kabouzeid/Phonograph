package com.poupa.vinylmusicplayer.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.glide.artistimage.ArtistImage;
import com.poupa.vinylmusicplayer.glide.palette.BitmapPaletteTranscoder;
import com.poupa.vinylmusicplayer.glide.palette.BitmapPaletteWrapper;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.util.ArtistSignatureUtil;
import com.poupa.vinylmusicplayer.util.CustomArtistImageUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistGlideRequest {

    private static final DiskCacheStrategy DEFAULT_DISK_CACHE_STRATEGY = DiskCacheStrategy.RESOURCE;
    private static final int DEFAULT_ERROR_IMAGE = R.drawable.default_artist_image;
    public static final int DEFAULT_ANIMATION = android.R.anim.fade_in;

    public static class Builder {
        final RequestManager requestManager;
        final Artist artist;
        boolean noCustomImage;
        boolean forceDownload;

        public static Builder from(@NonNull RequestManager requestManager, Artist artist) {
            return new Builder(requestManager, artist);
        }

        private Builder(@NonNull RequestManager requestManager, Artist artist) {
            this.requestManager = requestManager;
            this.artist = artist;
        }

        public PaletteBuilder generatePalette(Context context) {
            return new PaletteBuilder(this, context);
        }

        public BitmapBuilder asBitmap() {
            return new BitmapBuilder(this);
        }

        public Builder noCustomImage(boolean noCustomImage) {
            this.noCustomImage = noCustomImage;
            return this;
        }

        public Builder forceDownload(boolean forceDownload) {
            this.forceDownload = forceDownload;
            return this;
        }

        public RequestBuilder<Drawable> build() {
            //noinspection unchecked
            return createBaseRequestDrawable(requestManager, artist, noCustomImage, forceDownload)
                    .transition(GenericTransitionOptions.with(DEFAULT_ANIMATION))
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                            .error(DEFAULT_ERROR_IMAGE)
                            .priority(Priority.LOW)
                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .signature(createSignature(artist)));
        }
    }

    public static class BitmapBuilder {
        private final Builder builder;

        public BitmapBuilder(Builder builder) {
            this.builder = builder;
        }

        public RequestBuilder<Bitmap> build() {
            //noinspection unchecked
            return createBaseRequestBitmap(builder.requestManager, builder.artist, builder.noCustomImage, builder.forceDownload)
                    .transition(GenericTransitionOptions.with(DEFAULT_ANIMATION))
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                            .error(DEFAULT_ERROR_IMAGE)
                            .priority(Priority.LOW)
                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .signature(createSignature(builder.artist)));
        }
    }

    public static class PaletteBuilder {
        final Context context;
        private final Builder builder;

        public PaletteBuilder(Builder builder, Context context) {
            this.builder = builder;
            this.context = context;
        }

        public RequestBuilder<BitmapPaletteWrapper> build() {
            //noinspection unchecked
            return createBaseRequestBitmapPaletteWrapper(builder.requestManager, builder.artist, builder.noCustomImage, builder.forceDownload)
                    .transition(GenericTransitionOptions.with(DEFAULT_ANIMATION))
                    //.transition(GenericTransitionOptions.with(new BitmapPaletteTranscoder(context), BitmapPaletteWrapper.class))
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                            .error(DEFAULT_ERROR_IMAGE)
                            .priority(Priority.LOW)
                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .signature(createSignature(builder.artist)));
        }

        public RequestBuilder<BitmapPaletteWrapper> buildDontAnimate() {
            //noinspection unchecked
            return createBaseRequestBitmapPaletteWrapper(builder.requestManager, builder.artist, builder.noCustomImage, builder.forceDownload)
                    .transition(GenericTransitionOptions.with(DEFAULT_ANIMATION))
                    //.transition(GenericTransitionOptions.with(new BitmapPaletteTranscoder(context), BitmapPaletteWrapper.class))
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                            .error(DEFAULT_ERROR_IMAGE)
                            .priority(Priority.LOW)
                            .dontAnimate()
                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .signature(createSignature(builder.artist)));
        }
    }

    public static RequestBuilder<Bitmap> createBaseRequestBitmap(RequestManager requestManager, Artist artist, boolean noCustomImage, boolean forceDownload) {
        boolean hasCustomImage = CustomArtistImageUtil.getInstance(App.getInstance()).hasCustomArtistImage(artist);
        if (noCustomImage || !hasCustomImage) {
            return requestManager.asBitmap().load(new ArtistImage(artist.getName(), forceDownload));
        } else {
            return requestManager.asBitmap().load(CustomArtistImageUtil.getFile(artist));
        }
    }

    public static RequestBuilder<BitmapPaletteWrapper> createBaseRequestBitmapPaletteWrapper(RequestManager requestManager, Artist artist, boolean noCustomImage, boolean forceDownload) {
        boolean hasCustomImage = CustomArtistImageUtil.getInstance(App.getInstance()).hasCustomArtistImage(artist);
        if (noCustomImage || !hasCustomImage) {
            return requestManager.as(BitmapPaletteWrapper.class).load(new ArtistImage(artist.getName(), forceDownload));
        } else {
            return requestManager.as(BitmapPaletteWrapper.class).load(CustomArtistImageUtil.getFile(artist));
        }
    }

    public static RequestBuilder<Drawable> createBaseRequestDrawable(RequestManager requestManager, Artist artist, boolean noCustomImage, boolean forceDownload) {
        boolean hasCustomImage = CustomArtistImageUtil.getInstance(App.getInstance()).hasCustomArtistImage(artist);
        if (noCustomImage || !hasCustomImage) {
            return requestManager.load(new ArtistImage(artist.getName(), forceDownload));
        } else {
            return requestManager.load(CustomArtistImageUtil.getFile(artist));
        }
    }

    public static Key createSignature(Artist artist) {
        return ArtistSignatureUtil.getInstance(App.getInstance()).getArtistSignature(artist.getName());
    }
}
