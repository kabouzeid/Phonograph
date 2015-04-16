package com.kabouzeid.gramophone.prefs;

import android.content.Context;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.views.CircleView;

public class ColorChooserPreference extends Preference {

    private View mView;
    private int color;
    private int border;

    public ColorChooserPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorChooserPreference(Context context) {
        this(context, null, 0);
    }

    public ColorChooserPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.preference_custom);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        mView = view;
        invalidateColor();
    }

    public void setColor(int color, int border) {
        this.color = color;
        this.border = border;
        invalidateColor();
    }

    private void invalidateColor() {
        if (mView != null) {
            CircleView circle = (CircleView) mView.findViewById(R.id.circle);
            if (this.color != 0) {
                circle.setVisibility(View.VISIBLE);
                circle.setBackgroundColor(color);
                circle.setBorderColor(border);
            } else {
                circle.setVisibility(View.GONE);
            }
        }
    }
}
