package com.kabouzeid.gramophone.ui.activities.saf;

import android.os.Build;
import android.os.Bundle;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;
import com.kabouzeid.gramophone.R;


public class SAFGuideActivity extends IntroActivity {

    public static final int REQUEST_CODE_SAF_GUIDE = 98;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setButtonCtaVisible(false);
        setButtonNextVisible(false);
        setButtonBackVisible(false);

        setButtonCtaTintMode(BUTTON_CTA_TINT_MODE_TEXT);

        String title = String.format(getString(R.string.saf_guide_slide1_title), getString(R.string.app_name));

        addSlide(new SimpleSlide.Builder()
                .title(title)
                .description(Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1 ? R.string.saf_guide_slide1_description_before_o : R.string.saf_guide_slide1_description)
                .image(R.drawable.saf_guide_1)
                .background(R.color.md_indigo_300)
                .backgroundDark(R.color.md_indigo_400)
                .layout(R.layout.fragment_simple_slide_large_image)
                .build());
        addSlide(new SimpleSlide.Builder()
                .title(R.string.saf_guide_slide2_title)
                .description(R.string.saf_guide_slide2_description)
                .image(R.drawable.saf_guide_2)
                .background(R.color.md_indigo_500)
                .backgroundDark(R.color.md_indigo_600)
                .layout(R.layout.fragment_simple_slide_large_image)
                .build());
        addSlide(new SimpleSlide.Builder()
                .title(R.string.saf_guide_slide3_title)
                .description(R.string.saf_guide_slide3_description)
                .image(R.drawable.saf_guide_3)
                .background(R.color.md_indigo_700)
                .backgroundDark(R.color.md_indigo_800)
                .layout(R.layout.fragment_simple_slide_large_image)
                .build());
    }
}
