package com.mobeta.android.demodslv;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;


public class TestBedDSLV extends FragmentActivity implements RemoveModeDialog.RemoveOkListener,
        DragInitModeDialog.DragOkListener, EnablesDialog.EnabledOkListener {

    private static final String TAG_DSLV_FRAGMENT = "dslv_fragment";

    private int mNumHeaders = 0;
    private int mNumFooters = 0;

    private int mDragStartMode = DragSortController.ON_DRAG;
    private boolean mRemoveEnabled = true;
    private int mRemoveMode = DragSortController.FLING_REMOVE;
    private boolean mSortEnabled = true;
    private boolean mDragEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_bed_main);

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(TAG_DSLV_FRAGMENT) == null) {
            fm.beginTransaction()
                    .add(R.id.test_bed, getNewDslvFragment(), TAG_DSLV_FRAGMENT)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mode_menu, menu);
        return true;
    }

    @Override
    public void onRemoveOkClick(int removeMode) {
        if (removeMode != mRemoveMode) {
            mRemoveMode = removeMode;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.test_bed, getNewDslvFragment(), TAG_DSLV_FRAGMENT)
                    .commit();
        }
    }

    @Override
    public void onDragOkClick(int dragStartMode) {
        mDragStartMode = dragStartMode;
        DSLVFragment f =
                (DSLVFragment) getSupportFragmentManager().findFragmentByTag(TAG_DSLV_FRAGMENT);
        f.getController().setDragInitMode(dragStartMode);
    }

    @Override
    public void onEnabledOkClick(boolean drag, boolean sort, boolean remove) {
        mSortEnabled = sort;
        mRemoveEnabled = remove;
        mDragEnabled = drag;
        DSLVFragment f =
                (DSLVFragment) getSupportFragmentManager().findFragmentByTag(TAG_DSLV_FRAGMENT);
        DragSortListView dslv = (DragSortListView) f.getListView();
        f.getController().setRemoveEnabled(remove);
        f.getController().setSortEnabled(sort);
        dslv.setDragEnabled(drag);
    }

    private Fragment getNewDslvFragment() {
        DSLVFragmentClicks f = DSLVFragmentClicks.newInstance(mNumHeaders, mNumFooters);
        f.removeMode = mRemoveMode;
        f.removeEnabled = mRemoveEnabled;
        f.dragStartMode = mDragStartMode;
        f.sortEnabled = mSortEnabled;
        f.dragEnabled = mDragEnabled;
        return f;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.select_remove_mode:
            RemoveModeDialog rdialog = RemoveModeDialog.newInstance(mRemoveMode);
            rdialog.setRemoveOkListener(this);
            rdialog.show(getSupportFragmentManager(), "RemoveMode");
            return true;
        case R.id.select_drag_init_mode:
            DragInitModeDialog ddialog = DragInitModeDialog.newInstance(mDragStartMode);
            ddialog.setDragOkListener(this);
            ddialog.show(getSupportFragmentManager(), "DragInitMode");
            return true;
        case R.id.select_enables:
            EnablesDialog edialog =
                    EnablesDialog.newInstance(mDragEnabled, mSortEnabled, mRemoveEnabled);
            edialog.setEnabledOkListener(this);
            edialog.show(getSupportFragmentManager(), "Enables");
            return true;
        case R.id.add_header:
            mNumHeaders++;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.test_bed, getNewDslvFragment(), TAG_DSLV_FRAGMENT)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
            return true;
        case R.id.add_footer:
            mNumFooters++;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.test_bed, getNewDslvFragment(), TAG_DSLV_FRAGMENT)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
