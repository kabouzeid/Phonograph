package com.kabouzeid.materialmusic.ui.fragments.artistviewpager;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.github.ksoichiro.android.observablescrollview.ObservableGridView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.kabouzeid.materialmusic.App;
import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.interfaces.KabViewsDisableAble;
import com.kabouzeid.materialmusic.ui.activities.ArtistDetailActivity;
import com.kabouzeid.materialmusic.util.Util;

public abstract class AbsViewPagerTabArtistListFragment extends Fragment implements ObservableScrollViewCallbacks, KabViewsDisableAble {
    public static final String TAG = AbsViewPagerTabArtistListFragment.class.getSimpleName();
    protected App app;
    private ObservableGridView observableGridView;
    private Activity parentActivity;
    private int artistId = -1;
    private String artistName = "";
    private int paddingViewHeight;
    private boolean areViewsEnabled;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        app = (App) getActivity().getApplicationContext();
        parentActivity = getActivity();
        getArgs();

        View view = inflater.inflate(R.layout.fragment_gridview, container, false);
        observableGridView = (ObservableGridView) view.findViewById(R.id.scroll);
        setGridViewPadding();
        observableGridView.setScrollViewCallbacks(this);
        ListAdapter adapter = getAdapter();
        if (adapter != null) {
            observableGridView.setAdapter(adapter);
        }

        return view;
    }

    private void setGridViewPadding() {
        final int artistImageViewHeight = getResources().getDimensionPixelSize(R.dimen.header_image_height);
        final int titleViewHeight = getResources().getDimensionPixelSize(R.dimen.title_view_height);
        final int tabHeight = getResources().getDimensionPixelSize(R.dimen.tab_height);

        paddingViewHeight = artistImageViewHeight + titleViewHeight + tabHeight;

        if (app.isInPortraitMode() || app.isTablet()) {
            observableGridView.setPadding(0, paddingViewHeight, 0, Util.getNavigationBarHeight(getActivity()));
        } else {
            observableGridView.setPadding(0, paddingViewHeight, 0, 0);
        }
    }

    private void getArgs() {
        Bundle args = getArguments();
        if (args != null) {
            artistId = args.getInt(ArtistDetailActivity.ARG_ARTIST_ID, -1);
            artistName = args.getString(ArtistDetailActivity.ARG_ARTIST_NAME, "");
        }
    }

    public int getY() {
        return observableGridView.getCurrentScrollY() + paddingViewHeight;
    }

    protected int getArtistId() {
        return artistId;
    }

    protected String getArtistName() {
        return artistName;
    }

    public Activity getParentActivity() {
        return parentActivity;
    }

    protected void setAdapter(ListAdapter adapter) {
        observableGridView.setAdapter(adapter);
    }

    protected void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        observableGridView.setOnItemClickListener(onItemClickListener);
    }

    protected void setColumns(int columns) {
        observableGridView.setNumColumns(columns);
    }

    /*
    *
    * IMPORTANT:
    *
    * You CAN return null here and use setAdapter(ListAdapter adapter) inside getAdapter() to manually set the adapter.
    *
    * (i.e. if you must set the adapter async).
    *
    * */
    protected abstract ListAdapter getAdapter();

    @Override
    public void onScrollChanged(int scrollY, boolean b, boolean b2) {
        if (parentActivity instanceof ObservableScrollViewCallbacks) {
            if (getUserVisibleHint()) {
                ((ObservableScrollViewCallbacks) parentActivity).onScrollChanged(scrollY + paddingViewHeight, b, b2);
            }
        }
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    @Override
    public void disableViews() {
        areViewsEnabled = false;
        observableGridView.setEnabled(false);
    }

    @Override
    public boolean areViewsEnabled() {
        return areViewsEnabled;
    }

    @Override
    public void enableViews() {
        areViewsEnabled = true;
        observableGridView.setEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        enableViews();
    }
}
