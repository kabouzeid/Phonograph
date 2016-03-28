package com.kabouzeid.gramophone.ui.activities.intro;

import android.os.Bundle;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.kabouzeid.gramophone.R;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AppIntroActivity extends IntroActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(new PhonographSimpleSlide.Builder()
                .title(R.string.app_name)
                .description(R.string.welcome_to_phonograph)
                .image(R.drawable.icon_web)
                .background(R.color.md_amber_500)
                .backgroundDark(R.color.md_amber_600)
                .build());
        addSlide(new PhonographSimpleSlide.Builder()
                .title(R.string.label_playing_queue)
                .description(R.string.open_playing_queue_instruction)
                .image(R.drawable.tutorial_queue_swipe_up)
                .background(R.color.md_deep_purple_500)
                .backgroundDark(R.color.md_deep_purple_600)
                .build());
        addSlide(new PhonographSimpleSlide.Builder()
                .title(R.string.label_playing_queue)
                .description(R.string.rearrange_playing_queue_instruction)
                .image(R.drawable.tutorial_rearrange_queue)
                .background(R.color.md_indigo_500)
                .backgroundDark(R.color.md_indigo_600)
                .build());
    }
}
