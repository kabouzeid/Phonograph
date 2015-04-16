package com.kabouzeid.gramophone.model;

import android.content.Context;
import android.widget.ImageView;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public interface SearchEntry {
    String getTitle();

    String getSubTitle();

    void loadImage(Context context, ImageView imageView);
}
