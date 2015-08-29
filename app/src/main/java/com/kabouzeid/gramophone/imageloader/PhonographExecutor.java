package com.kabouzeid.gramophone.imageloader;

import android.support.annotation.NonNull;

import com.nostra13.universalimageloader.core.DefaultConfigurationFactory;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.LoadAndDisplayImageTask;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import java.io.File;
import java.util.concurrent.Executor;

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

    private Executor localTaskExecutor;
    private Executor networkTaskExecutor;

    // The thread pool size here needs further testing. Maybe the 2 additional Threads of the networkTaskExecutor are to much for lower end devices.
    public PhonographExecutor() {
        localTaskExecutor = DefaultConfigurationFactory.createExecutor(
                ImageLoaderConfiguration.Builder.DEFAULT_THREAD_POOL_SIZE,
                ImageLoaderConfiguration.Builder.DEFAULT_THREAD_PRIORITY,
                ImageLoaderConfiguration.Builder.DEFAULT_TASK_PROCESSING_TYPE
        );

        networkTaskExecutor = DefaultConfigurationFactory.createExecutor(
                2,
                ImageLoaderConfiguration.Builder.DEFAULT_THREAD_PRIORITY,
                ImageLoaderConfiguration.Builder.DEFAULT_TASK_PROCESSING_TYPE
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
                        networkTaskExecutor.execute(command);
                        return;
                    }
            }
        }
        localTaskExecutor.execute(command);
    }
}
