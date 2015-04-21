package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import com.afollestad.materialdialogs.ThemeSingleton;
import com.afollestad.materialdialogs.internal.MDTintHelper;

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
        final int color = ThemeSingleton.get().positiveColor;
        MDTintHelper.setTint(this, color);
    }
}