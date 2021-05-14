package com.kabouzeid.gramophone.preferences;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.PreferenceUtil;

public class LastAddedLimitPreferenceDialog extends DialogFragment {

    public static LastAddedLimitPreferenceDialog newInstance() {
        return new LastAddedLimitPreferenceDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = requireActivity().getLayoutInflater()
                .inflate(R.layout.preference_dailog_last_added_limit ,null);

        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(requireContext());

        SwitchMaterial enableLimit = view.findViewById(R.id.last_added_limit_switch);
        TextInputEditText limitText = view.findViewById(R.id.last_added_limit_text);

        enableLimit.setChecked(preferenceUtil.isLastAddedItemShowLimitEnable());
        limitText.setEnabled(enableLimit.isChecked());

        enableLimit.setOnCheckedChangeListener((compoundButton, b) -> limitText.setEnabled(b));

        limitText.setText(String.valueOf(preferenceUtil.getLastAddedItemShowLimit()));

        return new MaterialDialog.Builder(requireContext())
                .title(getResources().getString(R.string.last_added_items_show_limit))
                .autoDismiss(false)
                .customView(view, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive((dialog, which) -> {
                    if (enableLimit.isChecked() && limitText.getText().toString().equals("")) {
                        Toast.makeText(requireContext(), R.string.invalid_last_added_limit,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        PreferenceUtil.getInstance(requireContext()).setLastAddedItemShowLimitEnable
                                (enableLimit.isChecked());
                        PreferenceUtil.getInstance(requireContext()).setLastAddedItemShowLimit(
                                toIntOrDefault(limitText.getText().toString(), 100));
                        dismiss();
                    }
                })
                .onNegative((dialog, which) -> dismiss())
                .build();
    }

    public int toIntOrDefault(String s, int d) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return d;
        }
    }
}
