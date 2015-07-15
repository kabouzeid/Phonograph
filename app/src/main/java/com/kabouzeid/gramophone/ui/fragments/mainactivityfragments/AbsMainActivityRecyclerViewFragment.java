package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.AppBarLayout.OnOffsetChangedListener;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.interfaces.MusicStateListener;
import com.kabouzeid.gramophone.views.FastScroller;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsMainActivityRecyclerViewFragment<A extends RecyclerView.Adapter, LM extends RecyclerView.LayoutManager> extends AbsMainActivityFragment implements OnOffsetChangedListener, MusicStateListener {

    public static final String TAG = AbsMainActivityRecyclerViewFragment.class.getSimpleName();

    @InjectView(R.id.recycler_view)
    RecyclerView recyclerView;
    @Nullable
    @Optional
    @InjectView(android.R.id.empty)
    TextView empty;
    @Nullable
    @Optional
    @InjectView(R.id.fast_scroller)
    FastScroller fastScroller;

    private A adapter;
    private LM layoutManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (fastScroller != null) {
            fastScroller.setRecyclerView(recyclerView);
        }

        getMainActivity().addOnAppBarOffsetChangedListener(this);
        getMainActivity().addMusicStateListenerListener(this);

        setUpRecyclerView();

        showEmptyMessageIfEmpty();
    }

    private void setUpRecyclerView() {
        invalidateLayoutManager();
        invalidateAdapter();
    }

    protected void invalidateAdapter() {
        adapter = createAdapter();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                showEmptyMessageIfEmpty();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    protected void invalidateLayoutManager() {
        layoutManager = createLayoutManager();
        recyclerView.setLayoutManager(layoutManager);
    }

    protected A getAdapter() {
        return adapter;
    }

    protected LM getLayoutManager() {
        return layoutManager;
    }

    protected RecyclerView getRecyclerView() {
        return recyclerView;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (fastScroller != null) {
            fastScroller.setPadding(
                    fastScroller.getPaddingLeft(),
                    fastScroller.getPaddingTop(),
                    fastScroller.getPaddingRight(),
                    getMainActivity().getTotalAppBarScrollingRange() + i
            );
            fastScroller.updateHandlePosition();
        }
    }

    @Override
    public void onPlayingMetaChanged() {

    }

    @Override
    public void onPlayStateChanged() {

    }

    @Override
    public void enableViews() {
        super.enableViews();
        recyclerView.setEnabled(true);
    }

    @Override
    public void disableViews() {
        super.disableViews();
        recyclerView.setEnabled(false);
    }

    private void showEmptyMessageIfEmpty() {
        if (empty != null) {
            RecyclerView.Adapter adapter = getAdapter();
            if (adapter != null) {
                empty.setText(getEmptyMessage());
                empty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        }
    }

    @StringRes
    protected int getEmptyMessage() {
        return R.string.empty;
    }

    @LayoutRes
    protected int getLayoutRes() {
        return R.layout.fragment_main_activity_recycler_view;
    }

    protected abstract LM createLayoutManager();

    protected abstract A createAdapter();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getMainActivity().removeOnAppBarOffsetChangedListener(this);
        getMainActivity().removeMusicStateListenerListener(this);
        ButterKnife.reset(this);
    }

    protected int getDefaultGridColumnNumber() {
        return getActivity().getResources().getInteger(R.integer.grid_num_columns);
    }

    protected int getDefaultListColumnNumber() {
        return getActivity().getResources().getInteger(R.integer.list_num_columns);
    }
}
