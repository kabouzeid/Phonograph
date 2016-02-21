package com.kabouzeid.gramophone.ui.activities.intro;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.heinrichreimersoftware.materialintro.slide.Slide;
import com.kabouzeid.gramophone.R;

public class PhonographSimpleSlide extends Slide {

    private final Fragment fragment;
    @ColorRes
    private final int background;
    @ColorRes
    private final int backgroundDark;

    private PhonographSimpleSlide(Builder builder) {
        fragment = Fragment.newInstance(builder.title, builder.description, builder.image, builder.background);
        background = builder.background;
        backgroundDark = builder.backgroundDark;
    }

    @Override
    public Fragment getFragment() {
        return fragment;
    }

    @Override
    public int getBackground() {
        return background;
    }

    @Override
    public int getBackgroundDark() {
        return backgroundDark;
    }

    public static class Builder {
        @ColorRes
        private int background = 0;
        @ColorRes
        private int backgroundDark = 0;
        @StringRes
        private int title = 0;
        @StringRes
        private int description = 0;
        @DrawableRes
        private int image = 0;

        public Builder background(int background) {
            this.background = background;
            return this;
        }

        public Builder backgroundDark(int backgroundDark) {
            this.backgroundDark = backgroundDark;
            return this;
        }

        public Builder title(int title) {
            this.title = title;
            return this;
        }

        public Builder description(int description) {
            this.description = description;
            return this;
        }

        public Builder image(int image) {
            this.image = image;
            return this;
        }

        public PhonographSimpleSlide build() {
            if (background == 0 || title == 0)
                throw new IllegalArgumentException("You must set at least a title and background.");
            return new PhonographSimpleSlide(this);
        }
    }

    public static class Fragment extends android.support.v4.app.Fragment {
        private static final String ARGUMENT_TITLE_RES =
                "com.kabouzeid.gramophone.SimpleFragment.ARGUMENT_TITLE_RES";
        private static final String ARGUMENT_DESCRIPTION_RES =
                "com.kabouzeid.gramophone.SimpleFragment.ARGUMENT_DESCRIPTION_RES";
        private static final String ARGUMENT_IMAGE_RES =
                "com.kabouzeid.gramophone.SimpleFragment.ARGUMENT_IMAGE_RES";
        private static final String ARGUMENT_BACKGROUND_RES =
                "com.kabouzeid.gramophone.SimpleFragment.ARGUMENT_BACKGROUND_RES";

        public static Fragment newInstance(@StringRes int title, @StringRes int description, @DrawableRes int image, @ColorRes int background) {
            Fragment fragment = new Fragment();

            Bundle arguments = new Bundle();
            arguments.putInt(ARGUMENT_TITLE_RES, title);
            arguments.putInt(ARGUMENT_DESCRIPTION_RES, description);
            arguments.putInt(ARGUMENT_IMAGE_RES, image);
            arguments.putInt(ARGUMENT_BACKGROUND_RES, background);
            fragment.setArguments(arguments);

            return fragment;
        }

        public Fragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View fragment = inflater.inflate(R.layout.fragment_intro_slide, container, false);

            TextView title = (TextView) fragment.findViewById(R.id.mi_title);
            TextView description = (TextView) fragment.findViewById(R.id.mi_description);
            ImageView image = (ImageView) fragment.findViewById(R.id.mi_image);

            Bundle arguments = getArguments();

            title.setText(arguments.getInt(ARGUMENT_TITLE_RES));
            description.setText(arguments.getInt(ARGUMENT_DESCRIPTION_RES));
            image.setImageResource(arguments.getInt(ARGUMENT_IMAGE_RES));

            int background = ContextCompat.getColor(getContext(),
                    arguments.getInt(ARGUMENT_BACKGROUND_RES));
            if (ColorUtils.calculateLuminance(background) > 0.4) {
                //Use dark text color
                title.setTextColor(ContextCompat.getColor(getContext(),
                        R.color.mi_text_color_primary_light));
                description.setTextColor(ContextCompat.getColor(getContext(),
                        R.color.mi_text_color_secondary_light));
            } else {
                //Use light text color
                title.setTextColor(ContextCompat.getColor(getContext(),
                        R.color.mi_text_color_primary_dark));
                description.setTextColor(ContextCompat.getColor(getContext(),
                        R.color.mi_text_color_secondary_dark));
            }

            return fragment;
        }
    }
}
