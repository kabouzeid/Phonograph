package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

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

    protected int getItemLayout() {
        switch (getLayoutMode()) {
            case PreferenceUtil.LAYOUT_MODE_LIST:
                return R.layout.item_list;
            case PreferenceUtil.LAYOUT_MODE_GRID:
                return R.layout.item_grid;
            default:
                return R.layout.item_list;
        }
    }

    protected int getColumnNumber() {
        switch (getLayoutMode()) {
            case PreferenceUtil.LAYOUT_MODE_LIST:
                return getDefaultListColumnNumber();
            case PreferenceUtil.LAYOUT_MODE_GRID:
                return getDefaultGridColumnNumber();
            default:
                return R.layout.item_list;
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
