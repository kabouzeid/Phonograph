package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.interfaces.OnUpdatedListener;
import com.kabouzeid.gramophone.interfaces.SelfUpdating;
import com.kabouzeid.gramophone.views.FastScroller;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsMainActivityRecyclerViewFragment extends AbsMainActivityFragment implements OnUpdatedListener {

    public static final String TAG = AbsMainActivityRecyclerViewFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_activity_recycler_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        final FastScroller fastScroller = (FastScroller) view.findViewById(R.id.fast_scroller);
        fastScroller.setRecyclerView(recyclerView);
        fastScroller.setOnHandleTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        fastScroller.setPressedHandleColor(getMainActivity().getThemeColorPrimary());

        setUpRecyclerView();

        checkAndProcessAdapterSize();
    }

    private void setUpRecyclerView() {
        mAdapter = createAdapter();
        if (mAdapter instanceof SelfUpdating) ((SelfUpdating) mAdapter).setOnUpdatedListener(this);

        recyclerView.setLayoutManager(createLayoutManager());
        recyclerView.setAdapter(mAdapter);
    }

    public RecyclerView.Adapter getAdapter() {
        return mAdapter;
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

    private void checkAndProcessAdapterSize() {
        final View v = getView();
        RecyclerView.Adapter adapter = getAdapter();
        if (adapter != null && v != null) {
            final TextView emptyTextView = (TextView) v.findViewById(android.R.id.empty);

            emptyTextView.setText(getEmptyMessage());
            emptyTextView.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @StringRes
    protected int getEmptyMessage() {
        return R.string.nothing_here;
    }

    protected abstract RecyclerView.LayoutManager createLayoutManager();

    protected abstract RecyclerView.Adapter createAdapter();

    @Override
    public void onUpdated(SelfUpdating selfUpdating) {
        checkAndProcessAdapterSize();
    }
}
