package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

import android.app.Fragment;
import android.os.Build;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.ui.activities.MainActivity;
import com.kabouzeid.gramophone.util.Util;

/**
 * Created by karim on 27.02.15.
 */
public abstract class AbsMainActivityFragment extends Fragment implements KabViewsDisableAble {
    private boolean areViewsEnabled;

    protected int getTopPadding() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Util.isInPortraitMode(getActivity()) || Util.isTablet(getActivity())) {
                return Util.getActionBarSize(getActivity()) + getResources().getDimensionPixelSize(R.dimen.tab_height) + Util.getStatusBarHeight(getActivity());
            }
            return Util.getActionBarSize(getActivity()) + getResources().getDimensionPixelSize(R.dimen.tab_height) + Util.getStatusBarHeight(getActivity());
        }
        return Util.getActionBarSize(getActivity()) + getResources().getDimensionPixelSize(R.dimen.tab_height);
    }

    protected int getBottomPadding() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Util.isInPortraitMode(getActivity()) || Util.isTablet(getActivity())) {
                return Util.getNavigationBarHeight(getActivity());
            }
        }
        return 0;
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

    protected MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }
}
