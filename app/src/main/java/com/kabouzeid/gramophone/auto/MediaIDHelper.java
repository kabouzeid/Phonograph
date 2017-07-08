package com.kabouzeid.gramophone.auto;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by Beesham on 3/28/2017.
 */
public class MediaIDHelper {

    // Media IDs used on browseable items of MediaBrowser
    public static final String MEDIA_ID_EMPTY_ROOT = "__EMPTY_ROOT__";
    public static final String MEDIA_ID_ROOT = "__ROOT__";
    public static final String MEDIA_ID_MUSICS_BY_SEARCH = "__BY_SEARCH__";  // TODO
    public static final String MEDIA_ID_MUSICS_BY_HISTORY = "__BY_HISTORY__";
    public static final String MEDIA_ID_MUSICS_BY_TOP_TRACKS = "__BY_TOP_TRACKS__";
    public static final String MEDIA_ID_MUSICS_BY_PLAYLIST = "__BY_PLAYLIST__";
    public static final String MEDIA_ID_MUSICS_BY_ALBUM = "__BY_ALBUM__";
    public static final String MEDIA_ID_MUSICS_BY_ARTIST = "__BY_ARTIST__";
    public static final String MEDIA_ID_MUSICS_BY_QUEUE = "__BY_QUEUE__";

    private static final String CATEGORY_SEPARATOR = "__/__";
    private static final String LEAF_SEPARATOR = "__|__";

    /**
     * Create a String value that represents a playable or a browsable media.
     * <p/>
     * Encode the media browseable categories, if any, and the unique music ID, if any,
     * into a single String mediaID.
     * <p/>
     * MediaIDs are of the form <categoryType>__/__<categoryValue>__|__<musicUniqueId>, to make it
     * easy to find the category (like genre) that a music was selected from, so we
     * can correctly build the playing queue. This is specially useful when
     * one music can appear in more than one list, like "by genre -> genre_1"
     * and "by artist -> artist_1".
     *
     * @param musicID    Unique music ID for playable items, or null for browseable items.
     * @param categories Hierarchy of categories representing this item's browsing parents.
     * @return A hierarchy-aware media ID.
     */
    public static String createMediaID(String musicID, String... categories) {
        StringBuilder sb = new StringBuilder();
        if (categories != null) {
            for (int i = 0; i < categories.length; i++) {
                if (!isValidCategory(categories[i])) {
                    throw new IllegalArgumentException("Invalid category: " + categories[i]);
                }
                sb.append(categories[i]);
                if (i < categories.length - 1) {
                    sb.append(CATEGORY_SEPARATOR);
                }
            }
        }
        if (musicID != null) {
            sb.append(LEAF_SEPARATOR).append(musicID);
        }
        return sb.toString();
    }

    /**
     * Extracts unique musicID from the mediaID. mediaID is concatenation of category
     * (eg "by_genre"), categoryValue (eg "Classical") and unique musicID. This is necessary so we
     * know where the user selected the music from, when the music exists in more than one music
     * list, and thus we are able to correctly build the playing queue.
     *
     * @param mediaID that contains the musicID
     * @return musicID
     */
    public static String extractMusicIDFromMediaID(@NonNull String mediaID) {
        int pos = mediaID.indexOf(LEAF_SEPARATOR);
        if (pos >= 0) {
            return mediaID.substring(pos + LEAF_SEPARATOR.length());
        }
        return null;
    }

    public static boolean isBrowseable(@NonNull String mediaID) {
        return !mediaID.contains(LEAF_SEPARATOR);
    }

    private static boolean isValidCategory(String category) {
        return category == null ||
                (!category.contains(CATEGORY_SEPARATOR) && !category.contains(LEAF_SEPARATOR));
    }
}
