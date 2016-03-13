package com.kabouzeid.gramophone.ui.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.ui.activities.MainActivity;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import butterknife.ButterKnife;

public class FolderFragment extends AbsMainActivityFragment implements MainActivity.MainActivityFragmentCallbacks {
    public static final String TAG = FolderFragment.class.getSimpleName();

    public FolderFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folder, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        PreferenceUtil.getInstance(getActivity()).setLastPage(-2);
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}