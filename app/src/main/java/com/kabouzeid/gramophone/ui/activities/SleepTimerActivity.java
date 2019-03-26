package com.kabouzeid.gramophone.ui.activities;

import android.os.Bundle;

import android.support.annotation.Nullable;

import com.afollestad.materialdialogs.Theme;
import com.kabouzeid.appthemehelper.ATHActivity;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.SleepTimerDialog;
import com.kabouzeid.gramophone.util.PreferenceUtil;


public class SleepTimerActivity extends ATHActivity implements SleepTimerDialog.DismissListener {

    @Override
    public void onDismissed() {
        finish();
    }

    @Override
    protected int getThemeRes() {
        // the SleepTimerDialog theme is chosen by the primary text color of the context. We
        // therefore have to match the overall theme
        return (getDialogTheme() == Theme.LIGHT)
                ? R.style.Activity_Light_Dialog
                : R.style.Activity_Dark_Dialog;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SleepTimerDialog().show(getSupportFragmentManager(), "fragment_dialog");
    }

    private Theme getDialogTheme() {
        switch (PreferenceUtil.getInstance(this).getTheme()) {
            case "dark":
            case "black":
                return Theme.DARK;
            default:
                return Theme.LIGHT;
        }
    }
}
