package com.kabouzeid.materialmusic.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.kabouzeid.materialmusic.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.L;

/**
 * Created by karim on 28.12.14.
 */
public class ImageLoaderUtil {
    public static void initImageLoader(Context context) {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(defaultOptions)
                        //.memoryCache(new LRULimitedMemoryCache(1024*1024*CACHE_SIZE_MB))
                .build();
        ImageLoader.getInstance().init(config);

        L.writeLogs(false);
    }

    public static DisplayImageOptions getCacheOnDiskOptions() {
        return new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
    }

    public static DisplayImageOptions getCacheInMemoryOptions() {
        return new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .build();
    }

    public static class defaultAlbumArtOnFailed implements ImageLoadingListener {
        @Override
        public void onLoadingStarted(String imageUri, View view) {
            if (view != null) ((ImageView) view).setImageResource(R.drawable.default_album_art);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            if (view != null) ((ImageView) view).setImageResource(R.drawable.default_album_art);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            if (view != null) ((ImageView) view).setImageResource(R.drawable.default_album_art);
        }
    }

    public static class defaultArtistArtOnFailed implements ImageLoadingListener {

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            if (view != null) ((ImageView) view).setImageResource(R.drawable.default_artist_image);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            if (view != null) ((ImageView) view).setImageResource(R.drawable.default_artist_image);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            if (view != null) ((ImageView) view).setImageResource(R.drawable.default_artist_image);
        }
    }
}
