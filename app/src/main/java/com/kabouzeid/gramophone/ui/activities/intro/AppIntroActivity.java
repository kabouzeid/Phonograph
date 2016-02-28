package com.kabouzeid.gramophone.ui.activities.intro;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.kabouzeid.gramophone.R;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AppIntroActivity extends com.heinrichreimersoftware.materialintro.app.IntroActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setFullscreen(false);
        super.onCreate(savedInstanceState);

        addSlide(new PhonographSimpleSlide.Builder()
                .title(R.string.app_name)
                .description(R.string.welcome_to_phonograph)
                .image(R.drawable.icon_web)
                .background(R.color.md_blue_grey_500)
                .backgroundDark(R.color.md_blue_grey_600)
                .build());
        addSlide(new PhonographSimpleSlide.Builder()
                .title(R.string.label_playing_queue)
                .description(R.string.open_playing_queue_instruction)
                .image(R.drawable.tutorial_queue_swipe_up)
                .background(R.color.md_purple_500)
                .backgroundDark(R.color.md_purple_600)
                .build());
        addSlide(new PhonographSimpleSlide.Builder()
                .title(R.string.label_playing_queue)
                .description(R.string.rearrange_playing_queue_instruction)
                .image(R.drawable.tutorial_rearrange_queue)
                .background(R.color.md_deep_purple_500)
                .backgroundDark(R.color.md_deep_purple_600)
                .build());

        if (!hasExternalStoragePermission()) {
            addSlide(new PhonographSimpleSlide.Builder()
                    .title(R.string.label_storage)
                    .description(R.string.storage_permission_explaination)
                    .image(R.drawable.ic_folder_white_24dp)
                    .background(R.color.md_indigo_500)
                    .backgroundDark(R.color.md_indigo_600)
                    .build());
        }
    }

    private boolean hasExternalStoragePermission() {
        //noinspection SimplifiableIfStatement
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
}
