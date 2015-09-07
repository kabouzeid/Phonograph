package com.kabouzeid.gramophone.misc;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.support.design.widget.FloatingActionButton;
import android.util.Property;

import com.kabouzeid.gramophone.util.ColorUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class FloatingActionButtonProperties {
    public static final Property<FloatingActionButton, Integer> COLOR = new Property<FloatingActionButton, Integer>(Integer.class, "color") {
        @Override
        public void set(FloatingActionButton object, Integer value) {
            object.setBackgroundTintList(ColorStateList.valueOf(value));
            object.getDrawable().setColorFilter(ColorUtil.getPrimaryTextColorForBackground(object.getContext(), value), PorterDuff.Mode.SRC_IN);
        }

        @Override
        public Integer get(FloatingActionButton object) {
            return object.getBackgroundTintList() != null ? object.getBackgroundTintList().getDefaultColor() : 0;
        }
    };
}
