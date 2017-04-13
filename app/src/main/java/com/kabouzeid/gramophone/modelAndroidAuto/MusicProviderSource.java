package com.kabouzeid.gramophone.modelAndroidAuto;

import android.support.v4.media.MediaMetadataCompat;

import java.util.Iterator;

/**
 * Created by Beesham on 3/28/2017.
 */

public interface MusicProviderSource {
    String CUSTOM_METADATA_TRACK_SOURCE = "__SOURCE__";
    Iterator<MediaMetadataCompat> iterator();
}
