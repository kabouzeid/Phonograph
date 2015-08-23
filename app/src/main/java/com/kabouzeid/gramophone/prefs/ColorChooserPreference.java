package com.kabouzeid.gramophone.prefs;

import android.content.Context;
import android.preference.Preference;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.views.ColorView;

public class ColorChooserPreference extends Preference {

    @ColorInt
    private int color = -1;
    private ColorView colorView;

    public ColorChooserPreference(@NonNull Context context) {
        this(context, null);
    }

    public ColorChooserPreference(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorChooserPreference(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.preference_custom);
        setWidgetLayoutResource(R.layout.preference_color_widget);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        colorView = (ColorView) view.findViewById(R.id.circle);
        invalidateColor();
    }

    public void setColor(int color) {
        this.color = color;
        invalidateColor();
    }

    private void invalidateColor() {
        if (this.color >= 0) {
            colorView.setVisibility(View.VISIBLE);
            colorView.setBackgroundColor(color);
        } else {
            colorView.setVisibility(View.GONE);
        }
    }
}
