package com.kabouzeid.gramophone.glide.palette;

import android.support.annotation.ColorInt;
import android.support.v7.graphics.Palette;

import com.github.florent37.glidepalette.common.BaseColorGenerator;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class CustomTextColorGenerator extends BaseColorGenerator {
    private final int titleDark;
    private final int titleLight;
    private final int bodyDark;
    private final int bodyLight;

    public CustomTextColorGenerator(@ColorInt int titleDark, @ColorInt int titleLight, @ColorInt int bodyDark, @ColorInt int bodyLight) {
        this.titleDark = titleDark;
        this.titleLight = titleLight;
        this.bodyDark = bodyDark;
        this.bodyLight = bodyLight;
    }

    @Override
    public int getTitleTextColor(Palette.Swatch swatch) {
        return super.getTitleTextColor(swatch);
    }

    @Override
    public int getBodyTextColor(Palette.Swatch swatch) {
        return super.getBodyTextColor(swatch);
    }
}
