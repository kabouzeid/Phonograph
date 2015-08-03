package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;

import com.afollestad.materialdialogs.ThemeSingleton;
import com.afollestad.materialdialogs.internal.MDTintHelper;

/**
 * @author Aidan Follestad (afollestad)
 */
public class DynamicCheckBox extends AppCompatCheckBox {

    public DynamicCheckBox(Context context) {
        super(context);
        init();
    }

    public DynamicCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DynamicCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        final int color = ThemeSingleton.get().positiveColor.getDefaultColor();
        MDTintHelper.setTint(this, color);
    }
}