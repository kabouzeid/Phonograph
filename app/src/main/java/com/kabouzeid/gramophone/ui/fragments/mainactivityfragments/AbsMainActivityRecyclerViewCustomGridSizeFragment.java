package com.kabouzeid.gramophone.ui.fragments.mainactivityfragments;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.Util;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsMainActivityRecyclerViewCustomGridSizeFragment<A extends RecyclerView.Adapter, LM extends RecyclerView.LayoutManager> extends AbsMainActivityRecyclerViewFragment<A, LM> {
    private int gridSize;

    private boolean usePaletteInitialized;
    private boolean usePalette;

    public final int getGridSize() {
        if (gridSize == 0) {
            if (isLandscape()) {
                gridSize = loadGridSizeLand();
            } else {
                gridSize = loadGridSize();
            }
        }
        return gridSize;
    }

    /**
     * @return whether the palette should be used at all or not
     */
    public final boolean usePalette() {
        if (!usePaletteInitialized) {
            usePalette = loadUsePalette();
            usePaletteInitialized = true;
        }
        return usePalette;
    }

    public void setAndSaveGridSize(final int gridSize) {
        int oldLayoutRes = getItemLayoutRes();
        this.gridSize = gridSize;
        if (isLandscape()) {
            saveGridSizeLand(gridSize);
        } else {
            saveGridSize(gridSize);
        }
        // only recreate the adapter and layout manager if the layout res has changed
        if (oldLayoutRes != getItemLayoutRes()) {
            invalidateLayoutManager();
            invalidateAdapter();
        } else {
            setGridSize(gridSize);
        }
    }

    public void setAndSaveUsePalette(final boolean usePalette) {
        this.usePalette = usePalette;
        saveUsePalette(usePalette);
        setUsePalette(usePalette);
    }

    /**
     * @return whether the palette option should be available for the current item layout or not
     */
    public boolean canUsePalette() {
        return getItemLayoutRes() == R.layout.item_grid;
    }

    /**
     * Override to customize which item layout res should be used. You might also want to override {@link #canUsePalette()} then.
     *
     * @see #getGridSize()
     */
    @LayoutRes
    protected int getItemLayoutRes() {
        if (getGridSize() > getMaxGridSizeForList()) {
            return R.layout.item_grid;
        }
        return R.layout.item_list;
    }

    protected abstract int loadGridSize();

    protected abstract void saveGridSize(int gridColumns);

    protected abstract int loadGridSizeLand();

    protected abstract void saveGridSizeLand(int gridColumns);

    protected abstract void saveUsePalette(boolean usePalette);

    protected abstract boolean loadUsePalette();

    protected abstract void setUsePalette(boolean usePalette);

    protected abstract void setGridSize(int gridSize);

    protected int getMaxGridSizeForList() {
        if (isLandscape()) {
            return getActivity().getResources().getInteger(R.integer.default_list_columns_land);
        }
        return getActivity().getResources().getInteger(R.integer.default_list_columns);
    }

    protected final boolean isLandscape() {
        return Util.isLandscape(getActivity());
    }
}
