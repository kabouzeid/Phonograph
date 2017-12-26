package com.kabouzeid.gramophone.model;

import com.kabouzeid.gramophone.R;

public class CategoryInfo {
    public Category category;
    public boolean visible;

    public CategoryInfo(Category category, boolean visible) {
        this.category = category;
        this.visible = visible;
    }

    public enum Category {
        SONGS(R.string.songs),
        ALBUMS(R.string.albums),
        ARTISTS(R.string.artists),
        GENRES(R.string.genres),
        PLAYLISTS(R.string.playlists);

        public final int stringRes;

        Category(int stringRes) {
            this.stringRes = stringRes;
        }
    }
}
