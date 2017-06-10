package com.kabouzeid.gramophone.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.bumptech.glide.signature.ObjectKey;
import com.kabouzeid.gramophone.App;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistSignatureUtil {
    private static final String ARTIST_SIGNATURE_PREFS = "artist_signatures";

    private static ArtistSignatureUtil sInstance;

    private final SharedPreferences mPreferences;

    private ArtistSignatureUtil() {
        mPreferences = App.getStaticContext().getSharedPreferences(ARTIST_SIGNATURE_PREFS, Context.MODE_PRIVATE);
    }

    public static ArtistSignatureUtil getInstance() {
        if (sInstance == null) {
            sInstance = new ArtistSignatureUtil();
        }
        return sInstance;
    }

    @SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
    public void updateArtistSignature(String artistName) {
        // commit() is necessary
        mPreferences.edit().putLong(artistName, System.currentTimeMillis()).commit();
    }

    public long getArtistSignatureRaw(String artistName) {
        return mPreferences.getLong(artistName, 0);
    }

    public ObjectKey getArtistSignature(String artistName) {
        return new ObjectKey(getArtistSignatureRaw(artistName));
    }
}
