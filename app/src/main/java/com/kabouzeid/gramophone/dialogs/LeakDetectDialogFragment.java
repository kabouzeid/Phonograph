package com.kabouzeid.gramophone.dialogs;


import android.support.v4.app.DialogFragment;

import com.kabouzeid.gramophone.App;
import com.squareup.leakcanary.RefWatcher;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class LeakDetectDialogFragment extends DialogFragment {
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity() != null) {
            RefWatcher refWatcher = App.getRefWatcher(getActivity());
            refWatcher.watch(this);
        }
    }
}
