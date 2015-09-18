package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.afollestad.materialdialogs.internal.ThemeSingleton;

/**
 * @author Aidan Follestad (afollestad)
 */
public class DynamicEditText extends AppCompatEditText {

    public DynamicEditText(Context context) {
        super(context);
        init();
    }

    public DynamicEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DynamicEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        final int color = ThemeSingleton.get().positiveColor.getDefaultColor();
        MDTintHelper.setTint(this, color);
    }
}