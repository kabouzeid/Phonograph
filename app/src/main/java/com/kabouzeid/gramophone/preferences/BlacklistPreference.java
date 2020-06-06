package com.kabouzeid.gramophone.preferences;

import android.content.Context;
import android.util.AttributeSet;

import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEDialogPreference;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class BlocklistPreference extends ATEDialogPreference {
    public BlocklistPreference(Context context) {
        super(context);
    }

    public BlocklistPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BlocklistPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BlocklistPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}