package com.kabouzeid.gramophone.prefs;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.ThemeSingleton;
import com.kabouzeid.gramophone.R;

/**
 * Uses the theme's primary color as the text color of the category.
 *
 * @author Aidan Follestad (afollestad)
 */
public class DynamicPreferenceCategory extends PreferenceCategory {

    public DynamicPreferenceCategory(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DynamicPreferenceCategory(Context context) {
        this(context, null, 0);
    }

    public DynamicPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.preference_category_custom);
        setSelectable(false);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        ((TextView) view.findViewById(android.R.id.title)).setTextColor(ThemeSingleton.get().positiveColor);
    }
}
