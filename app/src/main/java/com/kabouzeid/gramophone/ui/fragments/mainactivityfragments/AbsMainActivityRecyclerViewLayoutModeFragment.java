package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.PreferenceUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsMainActivityRecyclerViewLayoutModeFragment<A extends RecyclerView.Adapter, LM extends RecyclerView.LayoutManager> extends AbsMainActivityRecyclerViewFragment<A, LM> {
    public static final int NO_LAYOUT_MODE = -1;

    private int layoutMode = NO_LAYOUT_MODE;

    public int getLayoutMode() {
        if (layoutMode == NO_LAYOUT_MODE) {
            layoutMode = loadLayoutMode();
        }
        return layoutMode;
    }

    @LayoutRes
    protected int getItemLayoutRes() {
        switch (getLayoutMode()) {
            case PreferenceUtil.LAYOUT_MODE_LIST:
                return R.layout.item_list;
            case PreferenceUtil.LAYOUT_MODE_GRID:
                return R.layout.item_grid;
            default:
                return R.layout.item_list;
        }
    }

    protected int getColumnCount() {
        switch (getLayoutMode()) {
            case PreferenceUtil.LAYOUT_MODE_LIST:
                return getDefaultListColumnCount();
            case PreferenceUtil.LAYOUT_MODE_GRID:
                return getDefaultGridColumnCount();
            default:
                return getDefaultListColumnCount();
        }
    }

    public void setLayoutModeAndSaveValue(int layoutMode) {
        this.layoutMode = layoutMode;
        saveLayoutMode(layoutMode);
        invalidateLayoutManager();
        invalidateAdapter();
    }

    protected abstract int loadLayoutMode();

    protected abstract void saveLayoutMode(int layoutMode);

    public abstract void setUsePaletteAndSaveValue(boolean usePalette);

    public abstract boolean loadUsePalette();
}
