package com.poupa.vinylmusicplayer.preferences;

import android.app.Dialog;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import java.util.Locale;

public class PreAmpPreferenceDialog extends DialogFragment implements SeekBar.OnSeekBarChangeListener {
  public static PreAmpPreferenceDialog newInstance() {
    return new PreAmpPreferenceDialog();
  }

  private float withRgValue;
  private float withoutRgValue;

  private TextView labelWithRg;
  private TextView labelWithoutRg;


  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    View view = getActivity().getLayoutInflater().inflate(R.layout.preference_dialog_rg_preamp, null);

    withRgValue = PreferenceUtil.getInstance().getRgPreampWithTag();
    withoutRgValue = PreferenceUtil.getInstance().getRgPreampWithoutTag();

    labelWithRg = view.findViewById(R.id.label_with_rg);
    labelWithoutRg = view.findViewById(R.id.label_without_rg);
    updateLabelWithRg();
    updateLabelWitouthRg();

    int color = ThemeStore.accentColor(getContext());

    SeekBar seekbarWithRg = view.findViewById(R.id.seekbar_with_rg);
    seekbarWithRg.setOnSeekBarChangeListener(this);
    seekbarWithRg.setProgress((int) ((withRgValue + 15) / 0.2f));
    seekbarWithRg.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    seekbarWithRg.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);

    SeekBar seekbarWithoutRg = view.findViewById(R.id.seekbar_without_rg);
    seekbarWithoutRg.setOnSeekBarChangeListener(this);
    seekbarWithoutRg.setProgress((int) ((withoutRgValue + 15) / 0.2f));
    seekbarWithoutRg.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    seekbarWithoutRg.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);

    return new MaterialDialog.Builder(getContext())
            .title(R.string.pref_title_rg_preamp)
            .positiveText(android.R.string.ok)
            .negativeText(android.R.string.cancel)
            .autoDismiss(false)
            .onNegative((dialog, which) -> dismiss())
            .onPositive((dialog, which) -> updateAndClose())
            .customView(view, false)
            .build();

  }

  private void updateAndClose() {
    PreferenceUtil.getInstance().setReplayGainPreamp(withRgValue, withoutRgValue);
    dismiss();
  }

  private void updateLabelWithRg() {
    labelWithRg.setText(String.format(Locale.getDefault(), "%+.1f%s", withRgValue, "dB"));
  }

  private void updateLabelWitouthRg() {
    labelWithoutRg.setText(String.format(Locale.getDefault(), "%+.1f%s", withoutRgValue, "dB"));
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    if (fromUser) {
      if (seekBar.getId() == R.id.seekbar_with_rg) {
        withRgValue = progress * 0.2f - 15.0f;
        updateLabelWithRg();
      } else if (seekBar.getId() == R.id.seekbar_without_rg) {
        withoutRgValue = progress * 0.2f - 15.0f;
        updateLabelWitouthRg();
      }
    }
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {

  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {

  }
}
