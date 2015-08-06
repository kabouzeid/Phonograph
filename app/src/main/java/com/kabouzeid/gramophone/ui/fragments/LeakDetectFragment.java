package com.kabouzeid.gramophone.ui.fragments;

import android.support.v4.app.Fragment;

import com.kabouzeid.gramophone.App;
import com.squareup.leakcanary.RefWatcher;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class LeakDetectFragment extends Fragment {
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity() != null) {
            RefWatcher refWatcher = App.getRefWatcher(getActivity());
            refWatcher.watch(this);
        }
    }
}
