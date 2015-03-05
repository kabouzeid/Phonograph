package com.kabouzeid.gramophone.model;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by karim on 27.02.15.
 */
public interface SearchEntry {
    public String getTitle();

    public String getSubTitle();

    public void loadImage(Context context, ImageView imageView);
}
