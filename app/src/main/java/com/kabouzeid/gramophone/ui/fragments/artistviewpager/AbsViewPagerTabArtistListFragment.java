package com.kabouzeid.gramophone.ui.fragments.artistviewpager;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.ui.activities.ArtistDetailActivity;
import com.kabouzeid.gramophone.util.Util;

public abstract class AbsViewPagerTabArtistListFragment extends Fragment implements ObservableScrollViewCallbacks, KabViewsDisableAble {
    public static final String TAG = AbsViewPagerTabArtistListFragment.class.getSimpleName();
    protected App app;
    private ObservableRecyclerView observableRecyclerView;
    private int artistId = -1;
    private String artistName = "";
    private int recyclerViewPaddingTop;
    private boolean areViewsEnabled;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        app = (App) getActivity().getApplicationContext();
        getArgs();

        View view = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        observableRecyclerView = (ObservableRecyclerView) view.findViewById(R.id.scroll);
        setRecyclerViewPadding();
        setNumColumns(getNumColumns());
        RecyclerView.Adapter adapter = getAdapter();
        if (adapter != null) {
            observableRecyclerView.setAdapter(adapter);
        }
        observableRecyclerView.setScrollViewCallbacks(this);
        return view;
    }

    private void setRecyclerViewPadding() {
        final int artistImageViewHeight = getResources().getDimensionPixelSize(R.dimen.header_image_height);
        final int titleViewHeight = getResources().getDimensionPixelSize(R.dimen.title_view_height);
        final int tabHeight = getResources().getDimensionPixelSize(R.dimen.tab_height);

        recyclerViewPaddingTop = artistImageViewHeight + titleViewHeight + tabHeight;

        if (Util.isInPortraitMode(getActivity()) || Util.isTablet(getActivity())) {
            observableRecyclerView.setPadding(0, recyclerViewPaddingTop, 0, Util.getNavigationBarHeight(getActivity()));
        } else {
            observableRecyclerView.setPadding(0, recyclerViewPaddingTop, 0, 0);
        }
    }

    private void getArgs() {
        Bundle args = getArguments();
        if (args != null) {
            artistId = args.getInt(ArtistDetailActivity.ARG_ARTIST_ID, -1);
            artistName = args.getString(ArtistDetailActivity.ARG_ARTIST_NAME, "");
        }
    }

    /*
    *
    * IMPORTANT:
    *
    * You CAN return null here and use setAdapter(ListAdapter adapter) inside getAdapter() to manually set the adapter.
    *
    * (use only in case you must set the adapter async).
    *
    * */
    protected abstract RecyclerView.Adapter getAdapter();

    protected abstract int getNumColumns();

    protected void setNumColumns(int columns) {
        observableRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columns));
    }

    protected void setAdapter(RecyclerView.Adapter adapter) {
        observableRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        enableViews();
    }

    @Override
    public void enableViews() {
        areViewsEnabled = true;
        observableRecyclerView.setEnabled(true);
    }

    @Override
    public void disableViews() {
        areViewsEnabled = false;
        observableRecyclerView.setEnabled(false);
    }

    @Override
    public boolean areViewsEnabled() {
        return areViewsEnabled;
    }

    public int getY() {
        return observableRecyclerView.getCurrentScrollY() + recyclerViewPaddingTop;
    }

    protected int getArtistId() {
        return artistId;
    }

    protected String getArtistName() {
        return artistName;
    }

    @Override
    public void onScrollChanged(int scrollY, boolean b, boolean b2) {
        if (getActivity() instanceof ObservableScrollViewCallbacks) {
            if (getUserVisibleHint()) {
                ((ObservableScrollViewCallbacks) getActivity()).onScrollChanged(scrollY + recyclerViewPaddingTop, b, b2);
            }
        }
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }
}
