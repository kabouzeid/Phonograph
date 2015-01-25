package com.mobeta.android.demodslv;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class EnablesDialog extends DialogFragment {

    private static final String EXTRA_ENABLED_FLAGS = "enabled_flags";

    private boolean[] mEnabled;

    private EnabledOkListener mListener;

    public static EnablesDialog newInstance(boolean drag, boolean sort, boolean remove) {
        Bundle args = new Bundle();
        args.putBooleanArray(EXTRA_ENABLED_FLAGS, new boolean[] { drag, sort, remove });

        EnablesDialog frag = new EnablesDialog();
        frag.setArguments(args);
        return frag;
    }

    public EnablesDialog() {
        super();
    }

    public interface EnabledOkListener {
        void onEnabledOkClick(boolean drag, boolean sort, boolean remove);
    }

    public void setEnabledOkListener(EnabledOkListener l) {
        mListener = l;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mEnabled = getArguments().getBooleanArray(EXTRA_ENABLED_FLAGS);
        if (mEnabled == null) {
            mEnabled = new boolean[] { true, true, false };
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle(R.string.select_remove_mode)
                .setMultiChoiceItems(R.array.enables_labels, mEnabled,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                mEnabled[which] = isChecked;
                            }
                        })
                // Set the action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null) {
                            mListener.onEnabledOkClick(mEnabled[0], mEnabled[1], mEnabled[2]);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        return builder.create();
    }
}
