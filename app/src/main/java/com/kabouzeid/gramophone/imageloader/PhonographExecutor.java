package com.kabouzeid.gramophone.imageloader;

import android.support.annotation.NonNull;

import com.nostra13.universalimageloader.core.DefaultConfigurationFactory;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.LoadAndDisplayImageTask;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A custom {@link Executor} meant for kabouzeid's fork of nostra13's Android Universal Image Loader (https://github.com/kabouzeid/Android-Universal-Image-Loader).
 * This {@link Executor} separates network and disk loading tasks into different executors so the network image loading doesn't block the disk image loading which is in most cases much faster.
 * <p/>
 * Maybe there is a better solution for this with a single (ThreadPool-)Executor, but I'm lacking experience here.
 *
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PhonographExecutor implements Executor {
    public static final String TAG = PhonographExecutor.class.getSimpleName();

    private static final int SECONDARY_THREAD_COUNT = 1; // must be more than ImageLoaderConfiguration.Builder.DEFAULT_THREAD_POOL_SIZE

    private ThreadPoolExecutor primaryExecutor;
    private ThreadPoolExecutor secondaryExecutor;

    public PhonographExecutor() {
        primaryExecutor = (ThreadPoolExecutor) DefaultConfigurationFactory.createExecutor(
                ImageLoaderConfiguration.Builder.DEFAULT_THREAD_POOL_SIZE - SECONDARY_THREAD_COUNT,
                ImageLoaderConfiguration.Builder.DEFAULT_THREAD_PRIORITY,
                ImageLoaderConfiguration.Builder.DEFAULT_TASK_PROCESSING_TYPE
        );

        secondaryExecutor = (ThreadPoolExecutor) DefaultConfigurationFactory.createExecutor(
                SECONDARY_THREAD_COUNT,
                ImageLoaderConfiguration.Builder.DEFAULT_THREAD_PRIORITY,
                QueueProcessingType.FIFO
        );
    }

    @Override
    public void execute(@NonNull Runnable command) {
        if (command instanceof LoadAndDisplayImageTask) {
            String uri = ((LoadAndDisplayImageTask) command).getLoadingUri();
            switch (ImageDownloader.Scheme.ofUri(uri)) {
                case HTTP:
                case HTTPS:
                    File imageFile = ImageLoader.getInstance().getDiskCache().get(uri);
                    if (imageFile == null || !imageFile.exists() || imageFile.length() <= 0) {
                        // the image is not yet in the disk cache
                        secondaryExecutor.execute(command);
                        return;
                    }
            }
        }
        if (secondaryExecutor.getActiveCount() < secondaryExecutor.getPoolSize()) {
            // if the secondary executor got unused threads left, use them!
            secondaryExecutor.execute(command);
        } else {
            primaryExecutor.execute(command);
        }
    }
}
