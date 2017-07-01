package com.kabouzeid.gramophone.helper;

import android.content.Context;
import android.databinding.BaseObservable;
import android.util.Log;

import com.android.databinding.library.baseAdapters.BR;
import com.kabouzeid.gramophone.util.PreferenceUtil;

/**
 * Created by lincoln on 6/18/17.
 */

public class UseScrollableTitles extends BaseObservable {

    private boolean useScrollableTitle;

    public boolean getUseScrollableTitles(Context context){
        Log.d("Scrollable?",Boolean.toString(useScrollableTitle));
        this.useScrollableTitle = PreferenceUtil.getInstance(context).classicNotification();
        return this.useScrollableTitle;
    }

    public void setUseScrollableTitles(boolean useScrollableTitle) {
        Log.d("Scrollable?",Boolean.toString(useScrollableTitle));
        this.useScrollableTitle = useScrollableTitle;
        notifyPropertyChanged(BR.useScrollableTitle);
    }
}
