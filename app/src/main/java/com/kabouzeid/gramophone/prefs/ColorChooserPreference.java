package com.kabouzeid.gramophone.prefs;

import android.content.Context;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.views.ColorView;

public class ColorChooserPreference extends Preference {

    private View mView;
    private int color;

    public ColorChooserPreference(@NonNull Context context, @NonNull AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorChooserPreference(@NonNull Context context) {
        this(context, null, 0);
    }

    public ColorChooserPreference(@NonNull Context context, @NonNull AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.preference_custom);
        setWidgetLayoutResource(R.layout.preference_color_widget);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        mView = view;
        invalidateColor();
    }

    public void setColor(int color) {
        this.color = color;
        invalidateColor();
    }

    private void invalidateColor() {
        if (mView != null) {
            ColorView colorView = (ColorView) mView.findViewById(R.id.circle);
            if (this.color != 0) {
                colorView.setVisibility(View.VISIBLE);
                colorView.setBackgroundColor(color);
            } else {
                colorView.setVisibility(View.GONE);
            }
        }
    }
}
