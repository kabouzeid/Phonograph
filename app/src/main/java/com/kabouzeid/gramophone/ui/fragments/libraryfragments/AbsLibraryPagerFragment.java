package com.kabouzeid.gramophone.ui.fragments.libraryfragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.kabouzeid.gramophone.ui.fragments.LibraryFragment;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AbsLibraryPagerFragment extends Fragment {

    public LibraryFragment getLibraryFragment() {
        return (LibraryFragment) getParentFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }
}
