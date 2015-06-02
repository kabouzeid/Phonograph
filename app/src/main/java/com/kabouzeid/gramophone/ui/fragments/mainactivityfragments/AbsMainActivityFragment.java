package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.ui.activities.MainActivity;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsMainActivityFragment extends Fragment implements KabViewsDisableAble {
    private boolean areViewsEnabled;

    protected MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    @Override
    public void enableViews() {
        areViewsEnabled = true;
    }

    @Override
    public void disableViews() {
        areViewsEnabled = false;
    }

    @Override
    public boolean areViewsEnabled() {
        return areViewsEnabled;
    }

    @Override
    public void onResume() {
        super.onResume();
        enableViews();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }
}
